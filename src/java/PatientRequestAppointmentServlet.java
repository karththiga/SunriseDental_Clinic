import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.time.format.TextStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@WebServlet("/PatientRequestAppointmentServlet")
public class PatientRequestAppointmentServlet
        extends HttpServlet {


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isPatient(request)) {

            response.sendRedirect("login.jsp");
            return;
        }

        loadTreatments(request);

        request.getRequestDispatcher(
                "/patientRequestAppointment.jsp"
        ).forward(
                request,
                response
        );
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isPatient(request)) {

            response.sendRedirect("login.jsp");
            return;
        }


        String action =
                request.getParameter("action");

        String treatmentIdValue =
                request.getParameter("treatmentId");

        String appointmentDate =
                request.getParameter("appointmentDate");

        String appointmentTime =
                request.getParameter("appointmentTime");


        if (treatmentIdValue == null
                || treatmentIdValue.isEmpty()
                || appointmentDate == null
                || appointmentDate.isEmpty()
                || appointmentTime == null
                || appointmentTime.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Please select treatment, date and time."
            );

            loadTreatments(request);

            request.getRequestDispatcher(
                    "/patientRequestAppointment.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        int treatmentId;


        try {

            treatmentId =
                    Integer.parseInt(
                            treatmentIdValue
                    );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Invalid treatment."
            );

            loadTreatments(request);

            request.getRequestDispatcher(
                    "/patientRequestAppointment.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        /*
         * Validate date
         */
        LocalDate selectedDate;


        try {

            selectedDate =
                    LocalDate.parse(
                            appointmentDate
                    );

            if (selectedDate.isBefore(
                    LocalDate.now())) {

                request.setAttribute(
                        "error",
                        "Appointment date cannot be in the past."
                );

                loadTreatments(request);

                request.getRequestDispatcher(
                        "/patientRequestAppointment.jsp"
                ).forward(
                        request,
                        response
                );

                return;
            }


        } catch (Exception e) {

            request.setAttribute(
                    "error",
                    "Invalid appointment date."
            );

            loadTreatments(request);

            request.getRequestDispatcher(
                    "/patientRequestAppointment.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        /*
         * Keep selected values
         */
        request.setAttribute(
                "selectedTreatmentId",
                treatmentId
        );

        request.setAttribute(
                "selectedDate",
                appointmentDate
        );

        request.setAttribute(
                "selectedTime",
                appointmentTime
        );


        /*
         * CHECK AVAILABLE DENTISTS
         */
        if ("check".equalsIgnoreCase(action)) {

            try {

                loadAvailableDentists(
                        treatmentId,
                        selectedDate,
                        appointmentTime,
                        request
                );

            } catch (SQLException e) {

                e.printStackTrace();

                request.setAttribute(
                        "error",
                        "Unable to check dentist availability."
                );
            }


            loadTreatments(request);


            request.getRequestDispatcher(
                    "/patientRequestAppointment.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        /*
         * SUBMIT APPOINTMENT REQUEST
         */
        if ("submit".equalsIgnoreCase(action)) {

            String dentistIdValue =
                    request.getParameter("dentistId");


            if (dentistIdValue == null
                    || dentistIdValue.isEmpty()) {

                request.setAttribute(
                        "error",
                        "Please select an available dentist."
                );


                try {

                    loadAvailableDentists(
                            treatmentId,
                            selectedDate,
                            appointmentTime,
                            request
                    );

                } catch (SQLException e) {

                    e.printStackTrace();
                }


                loadTreatments(request);


                request.getRequestDispatcher(
                        "/patientRequestAppointment.jsp"
                ).forward(
                        request,
                        response
                );

                return;
            }


            int dentistId;


            try {

                dentistId =
                        Integer.parseInt(
                                dentistIdValue
                        );

            } catch (NumberFormatException e) {

                request.setAttribute(
                        "error",
                        "Invalid dentist."
                );

                loadTreatments(request);

                request.getRequestDispatcher(
                        "/patientRequestAppointment.jsp"
                ).forward(
                        request,
                        response
                );

                return;
            }


            HttpSession session =
                    request.getSession(false);


            int userId =
                    (Integer) session.getAttribute(
                            "user_id"
                    );


            try {

                /*
                 * Recheck availability before insert
                 */
                if (!isDentistAvailable(
                        dentistId,
                        treatmentId,
                        selectedDate,
                        appointmentTime)) {

                    request.setAttribute(
                            "error",
                            "The selected dentist is no longer available."
                    );

                    loadAvailableDentists(
                            treatmentId,
                            selectedDate,
                            appointmentTime,
                            request
                    );

                    loadTreatments(request);

                    request.getRequestDispatcher(
                            "/patientRequestAppointment.jsp"
                    ).forward(
                            request,
                            response
                    );

                    return;
                }


                Map<String, String> patient =
                        getPatientDetails(
                                userId
                        );


                if (patient == null) {

                    request.setAttribute(
                            "error",
                            "Patient account not found."
                    );

                } else {

                    String sql =
                            "INSERT INTO appointments "
                            + "(appointment_number, "
                            + "patient_user_id, "
                            + "patient_name, "
                            + "address, "
                            + "contact_number, "
                            + "dentist_id, "
                            + "treatment_id, "
                            + "appointment_date, "
                            + "appointment_time, "
                            + "status) "
                            + "VALUES "
                            + "(NULL, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending')";


                    try (
                        Connection conn =
                                DBConnection.getConnection();

                        PreparedStatement stmt =
                                conn.prepareStatement(sql)
                    ) {

                        stmt.setInt(
                                1,
                                userId
                        );

                        stmt.setString(
                                2,
                                patient.get("name")
                        );

                        stmt.setString(
                                3,
                                ""
                        );

                        stmt.setString(
                                4,
                                patient.get("phone")
                        );

                        stmt.setInt(
                                5,
                                dentistId
                        );

                        stmt.setInt(
                                6,
                                treatmentId
                        );

                        stmt.setString(
                                7,
                                appointmentDate
                        );

                        stmt.setString(
                                8,
                                appointmentTime
                        );

                        stmt.executeUpdate();
                    }


                    request.setAttribute(
                            "success",
                            "Appointment request submitted successfully. "
                            + "Status is Pending."
                    );
                }


            } catch (SQLException e) {

                e.printStackTrace();

                request.setAttribute(
                        "error",
                        "Unable to submit appointment request."
                );
            }


            loadTreatments(request);


            request.getRequestDispatcher(
                    "/patientRequestAppointment.jsp"
            ).forward(
                    request,
                    response
            );
        }
    }



    private boolean isPatient(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);


        return session != null
                && session.getAttribute("username") != null
                && "Patient".equalsIgnoreCase(
                        (String) session.getAttribute("role")
                );
    }



    private void loadAvailableDentists(
            int treatmentId,
            LocalDate date,
            String appointmentTime,
            HttpServletRequest request)
            throws SQLException {


        List<Map<String, Object>> dentists =
                new ArrayList<>();


        String dayName =
                date.getDayOfWeek()
                        .getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH
                        );


        String sql =
                "SELECT "
                + "d.dentist_id, "
                + "d.dentist_name, "
                + "d.specialization, "
                + "d.available_from, "
                + "d.available_to "
                + "FROM dentists d "
                + "INNER JOIN dentist_treatments dt "
                + "ON d.dentist_id = dt.dentist_id "
                + "WHERE dt.treatment_id = ? "
                + "AND d.status = 'Active' "
                + "AND d.available_day = ? "
                + "AND ? BETWEEN d.available_from "
                + "AND d.available_to "
                + "AND NOT EXISTS ( "
                + "SELECT 1 "
                + "FROM appointments a "
                + "WHERE a.dentist_id = d.dentist_id "
                + "AND a.appointment_date = ? "
                + "AND a.appointment_time = ? "
                + "AND a.status IN ('Pending','Confirmed') "
                + ") "
                + "ORDER BY d.dentist_name";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    treatmentId
            );

            stmt.setString(
                    2,
                    dayName
            );

            stmt.setString(
                    3,
                    appointmentTime
            );

            stmt.setDate(
                    4,
                    java.sql.Date.valueOf(date)
            );

            stmt.setString(
                    5,
                    appointmentTime
            );


            ResultSet rs =
                    stmt.executeQuery();


            while (rs.next()) {

                Map<String, Object> dentist =
                        new HashMap<>();


                dentist.put(
                        "dentistId",
                        rs.getInt("dentist_id")
                );

                dentist.put(
                        "dentistName",
                        rs.getString("dentist_name")
                );

                dentist.put(
                        "specialization",
                        rs.getString("specialization")
                );

                dentist.put(
                        "availableFrom",
                        rs.getTime("available_from")
                );

                dentist.put(
                        "availableTo",
                        rs.getTime("available_to")
                );


                dentists.add(dentist);
            }
        }


        request.setAttribute(
                "availableDentists",
                dentists
        );

        request.setAttribute(
                "availabilityChecked",
                true
        );
    }



    private boolean isDentistAvailable(
            int dentistId,
            int treatmentId,
            LocalDate date,
            String appointmentTime)
            throws SQLException {


        String dayName =
                date.getDayOfWeek()
                        .getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH
                        );


        String sql =
                "SELECT d.dentist_id "
                + "FROM dentists d "
                + "INNER JOIN dentist_treatments dt "
                + "ON d.dentist_id = dt.dentist_id "
                + "WHERE d.dentist_id = ? "
                + "AND dt.treatment_id = ? "
                + "AND d.status = 'Active' "
                + "AND d.available_day = ? "
                + "AND ? BETWEEN d.available_from "
                + "AND d.available_to "
                + "AND NOT EXISTS ( "
                + "SELECT 1 "
                + "FROM appointments a "
                + "WHERE a.dentist_id = d.dentist_id "
                + "AND a.appointment_date = ? "
                + "AND a.appointment_time = ? "
                + "AND a.status IN ('Pending','Confirmed') "
                + ")";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    dentistId
            );

            stmt.setInt(
                    2,
                    treatmentId
            );

            stmt.setString(
                    3,
                    dayName
            );

            stmt.setString(
                    4,
                    appointmentTime
            );

            stmt.setDate(
                    5,
                    java.sql.Date.valueOf(date)
            );

            stmt.setString(
                    6,
                    appointmentTime
            );


            ResultSet rs =
                    stmt.executeQuery();


            return rs.next();
        }
    }



    private Map<String, String> getPatientDetails(
            int userId)
            throws SQLException {


        String sql =
                "SELECT first_name, "
                + "last_name, "
                + "phone_number "
                + "FROM users "
                + "WHERE user_id = ? "
                + "AND role = 'Patient'";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    userId
            );


            ResultSet rs =
                    stmt.executeQuery();


            if (rs.next()) {

                Map<String, String> patient =
                        new HashMap<>();


                patient.put(
                        "name",
                        rs.getString("first_name")
                        + " "
                        + rs.getString("last_name")
                );


                patient.put(
                        "phone",
                        rs.getString("phone_number")
                );


                return patient;
            }
        }


        return null;
    }



    private void loadTreatments(
            HttpServletRequest request) {


        List<Map<String, Object>> treatments =
                new ArrayList<>();


        String sql =
                "SELECT treatment_id, "
                + "treatment_name, "
                + "treatment_cost "
                + "FROM treatments "
                + "ORDER BY treatment_name";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    stmt.executeQuery()
        ) {

            while (rs.next()) {

                Map<String, Object> treatment =
                        new HashMap<>();


                treatment.put(
                        "treatmentId",
                        rs.getInt("treatment_id")
                );

                treatment.put(
                        "treatmentName",
                        rs.getString("treatment_name")
                );

                treatment.put(
                        "treatmentCost",
                        rs.getBigDecimal("treatment_cost")
                );


                treatments.add(treatment);
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load treatments."
            );
        }


        request.setAttribute(
                "treatments",
                treatments
        );
    }
}