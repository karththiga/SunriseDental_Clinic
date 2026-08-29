import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@WebServlet("/PatientRequestAppointmentServlet")
public class PatientRequestAppointmentServlet extends HttpServlet {


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

        String treatmentIdValue =
                request.getParameter("treatmentId");

        if (treatmentIdValue != null
                && !treatmentIdValue.trim().isEmpty()) {

            try {

                int treatmentId =
                        Integer.parseInt(treatmentIdValue);

                loadDentistsByTreatment(
                        treatmentId,
                        request
                );

                request.setAttribute(
                        "selectedTreatmentId",
                        treatmentId
                );

            } catch (NumberFormatException e) {

                request.setAttribute(
                        "error",
                        "Invalid treatment selected."
                );
            }
        }

        request.getRequestDispatcher(
                "/patientRequestAppointment.jsp"
        ).forward(request, response);
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

        HttpSession session =
                request.getSession(false);

        int userId =
                (Integer) session.getAttribute("user_id");


        String treatmentIdValue =
                request.getParameter("treatmentId");

        String dentistIdValue =
                request.getParameter("dentistId");

        String appointmentDate =
                request.getParameter("appointmentDate");

        String appointmentTime =
                request.getParameter("appointmentTime");


        if (treatmentIdValue == null
                || treatmentIdValue.trim().isEmpty()
                || dentistIdValue == null
                || dentistIdValue.trim().isEmpty()
                || appointmentDate == null
                || appointmentDate.trim().isEmpty()
                || appointmentTime == null
                || appointmentTime.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Please complete all appointment fields."
            );

            reloadForm(
                    treatmentIdValue,
                    request,
                    response
            );

            return;
        }


        int treatmentId;
        int dentistId;

        try {

            treatmentId =
                    Integer.parseInt(
                            treatmentIdValue
                    );

            dentistId =
                    Integer.parseInt(
                            dentistIdValue
                    );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Invalid treatment or dentist."
            );

            reloadForm(
                    treatmentIdValue,
                    request,
                    response
            );

            return;
        }


        try {

            LocalDate selectedDate =
                    LocalDate.parse(
                            appointmentDate
                    );

            if (selectedDate.isBefore(
                    LocalDate.now())) {

                request.setAttribute(
                        "error",
                        "Appointment date cannot be in the past."
                );

                reloadForm(
                        treatmentIdValue,
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

            reloadForm(
                    treatmentIdValue,
                    request,
                    response
            );

            return;
        }


        try {

            /*
             * Validate that the selected dentist
             * really provides this treatment.
             */
            if (!dentistProvidesTreatment(
                    dentistId,
                    treatmentId)) {

                request.setAttribute(
                        "error",
                        "Selected dentist does not provide this treatment."
                );

                reloadForm(
                        treatmentIdValue,
                        request,
                        response
                );

                return;
            }


            Map<String, String> patient =
                    getPatientDetails(userId);


            if (patient == null) {

                request.setAttribute(
                        "error",
                        "Patient account not found."
                );

                reloadForm(
                        treatmentIdValue,
                        request,
                        response
                );

                return;
            }


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
                    + "The administrator will check dentist availability."
            );


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
        ).forward(request, response);
    }


    private void loadDentistsByTreatment(
            int treatmentId,
            HttpServletRequest request) {

        List<Map<String, Object>> dentists =
                new ArrayList<>();


        String sql =
                "SELECT "
                + "d.dentist_id, "
                + "d.dentist_name, "
                + "d.specialization "
                + "FROM dentists d "
                + "INNER JOIN dentist_treatments dt "
                + "ON d.dentist_id = dt.dentist_id "
                + "WHERE dt.treatment_id = ? "
                + "AND d.status = 'Active' "
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

                dentists.add(dentist);
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load dentists."
            );
        }


        request.setAttribute(
                "dentists",
                dentists
        );
    }


    private boolean dentistProvidesTreatment(
            int dentistId,
            int treatmentId)
            throws SQLException {

        String sql =
                "SELECT 1 "
                + "FROM dentist_treatments dt "
                + "INNER JOIN dentists d "
                + "ON dt.dentist_id = d.dentist_id "
                + "WHERE dt.dentist_id = ? "
                + "AND dt.treatment_id = ? "
                + "AND d.status = 'Active'";


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


    private void reloadForm(
            String treatmentIdValue,
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        loadTreatments(request);

        try {

            if (treatmentIdValue != null
                    && !treatmentIdValue.isEmpty()) {

                int treatmentId =
                        Integer.parseInt(
                                treatmentIdValue
                        );

                request.setAttribute(
                        "selectedTreatmentId",
                        treatmentId
                );

                loadDentistsByTreatment(
                        treatmentId,
                        request
                );
            }

        } catch (NumberFormatException e) {

            // Ignore invalid treatment ID
        }

        request.getRequestDispatcher(
                "/patientRequestAppointment.jsp"
        ).forward(request, response);
    }
}