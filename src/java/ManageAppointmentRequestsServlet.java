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


@WebServlet("/ManageAppointmentRequestsServlet")
public class ManageAppointmentRequestsServlet
        extends HttpServlet {


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {

            response.sendRedirect("login.jsp");
            return;
        }


        /*
         * Load all pending requests.
         */
        loadPendingRequests(request);


        /*
         * If Admin selected a request,
         * load that request and available dentists.
         */
        String appointmentIdValue =
                request.getParameter("appointmentId");


        if (appointmentIdValue != null
                && !appointmentIdValue.trim().isEmpty()) {

           try {

    int appointmentId =
            Integer.parseInt(
                    appointmentIdValue
            );

    loadSelectedRequest(
            appointmentId,
            request
    );

    loadTreatments(
            request
    );

} catch (NumberFormatException e) {

    request.setAttribute(
            "error",
            "Invalid appointment request."
    );

} catch (SQLException e) {

    e.printStackTrace();

    request.setAttribute(
            "error",
            "Unable to load appointment details."
    );
}
        }


        request.getRequestDispatcher(
                "manageAppointmentRequests.jsp"
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

        if (!isAdmin(request)) {

            response.sendRedirect("login.jsp");
            return;
        }


        String appointmentIdValue =
                request.getParameter("appointmentId");

        String dentistIdValue =
                request.getParameter("dentistId");

        String treatmentIdValue =
                request.getParameter("treatmentId");


        if (appointmentIdValue == null
                || dentistIdValue == null
                || treatmentIdValue == null
                || appointmentIdValue.isEmpty()
                || dentistIdValue.isEmpty()
                || treatmentIdValue.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Please select a dentist and treatment."
            );

            loadPendingRequests(request);

            request.getRequestDispatcher(
                    "manageAppointmentRequests.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        int appointmentId;
        int dentistId;
        int treatmentId;


        try {

            appointmentId =
                    Integer.parseInt(
                            appointmentIdValue
                    );

            dentistId =
                    Integer.parseInt(
                            dentistIdValue
                    );

            treatmentId =
                    Integer.parseInt(
                            treatmentIdValue
                    );


        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Invalid appointment information."
            );

            loadPendingRequests(request);

            request.getRequestDispatcher(
                    "manageAppointmentRequests.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        try {

            /*
             * Re-check dentist availability
             * before saving.
             */
            if (!isDentistAvailable(
                    appointmentId,
                    dentistId)) {

                request.setAttribute(
                        "error",
                        "The selected dentist is no longer "
                        + "available at this date and time."
                );

                loadPendingRequests(request);

                loadSelectedRequest(
                        appointmentId,
                        request
                );

                loadTreatments(request);

                request.getRequestDispatcher(
                        "manageAppointmentRequests.jsp"
                ).forward(
                        request,
                        response
                );

                return;
            }


            /*
             * Generate unique appointment number.
             *
             * Example:
             * appointment_id = 5
             * becomes APT0005
             */
            String appointmentNumber =
                    String.format(
                            "APT%04d",
                            appointmentId
                    );


            String sql =
                    "UPDATE appointments "
                    + "SET appointment_number = ?, "
                    + "dentist_id = ?, "
                    + "treatment_id = ?, "
                    + "status = 'Pending' "
                    + "WHERE appointment_id = ? "
                    + "AND status = 'Pending'";


            try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
            ) {

                stmt.setString(
                        1,
                        appointmentNumber
                );

                stmt.setInt(
                        2,
                        dentistId
                );

                stmt.setInt(
                        3,
                        treatmentId
                );

                stmt.setInt(
                        4,
                        appointmentId
                );


                int rows =
                        stmt.executeUpdate();


                if (rows > 0) {

                    request.setAttribute(
                            "success",
                            "Appointment assigned successfully. "
                            + "Appointment Number: "
                            + appointmentNumber
                    );

                } else {

                    request.setAttribute(
                            "error",
                            "Unable to assign appointment."
                    );
                }
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Database error while assigning appointment."
            );
        }


        loadPendingRequests(request);


        request.getRequestDispatcher(
                "manageAppointmentRequests.jsp"
        ).forward(
                request,
                response
        );
    }



    /*
     * Only Admin can access this servlet.
     */
    private boolean isAdmin(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);


        return session != null
                && session.getAttribute("username") != null
                && "Admin".equalsIgnoreCase(
                        (String) session.getAttribute("role")
                );
    }



    /*
     * Load requests that have not yet
     * been confirmed by a dentist.
     */
    private void loadPendingRequests(
            HttpServletRequest request) {


        List<Map<String, Object>> requests =
                new ArrayList<>();

String sql =
        "SELECT "
        + "a.appointment_id, "
        + "a.appointment_number, "
        + "a.patient_name, "
        + "a.contact_number, "
        + "a.appointment_date, "
        + "a.appointment_time, "
        + "a.dentist_id, "
        + "a.status, "
        + "t.treatment_name "
        + "FROM appointments a "
        + "LEFT JOIN treatments t "
        + "ON a.treatment_id = t.treatment_id "
        + "WHERE a.status = 'Pending' "
        + "ORDER BY a.appointment_date, "
        + "a.appointment_time";

        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    stmt.executeQuery()
        ) {


            while (rs.next()) {


                Map<String, Object> row =
                        new HashMap<>();


                row.put(
                        "appointmentId",
                        rs.getInt(
                                "appointment_id"
                        )
                );


                row.put(
                        "appointmentNumber",
                        rs.getString(
                                "appointment_number"
                        )
                );


                row.put(
                        "patientName",
                        rs.getString(
                                "patient_name"
                        )
                );


                row.put(
                        "contactNumber",
                        rs.getString(
                                "contact_number"
                        )
                );


                row.put(
                        "appointmentDate",
                        rs.getDate(
                                "appointment_date"
                        )
                );


                row.put(
                        "appointmentTime",
                        rs.getTime(
                                "appointment_time"
                        )
                );


                row.put(
                        "dentistId",
                        rs.getObject(
                                "dentist_id"
                        )
                );


                row.put(
                        "status",
                        rs.getString(
                                "status"
                        )
                );
                row.put(
                       "treatmentName",
                        rs.getString("treatment_name")
                );


                requests.add(row);
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load appointment requests."
            );
        }


        request.setAttribute(
                "requests",
                requests
        );
    }



    /*
     * Load the appointment selected
     * by Admin.
     */
    private void loadSelectedRequest(
            int appointmentId,
            HttpServletRequest request)
            throws SQLException {


        String sql =
        "SELECT "
        + "a.appointment_id, "
        + "a.appointment_number, "
        + "a.patient_name, "
        + "a.address, "
        + "a.contact_number, "
        + "a.treatment_id, "
        + "t.treatment_name, "
        + "t.treatment_cost, "
        + "a.appointment_date, "
        + "a.appointment_time, "
        + "a.status "
        + "FROM appointments a "
        + "LEFT JOIN treatments t "
        + "ON a.treatment_id = t.treatment_id "
        + "WHERE a.appointment_id = ?";

        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {


            stmt.setInt(
                    1,
                    appointmentId
            );


            ResultSet rs =
                    stmt.executeQuery();


            if (rs.next()) {


                request.setAttribute(
                        "selected",
                        true
                );


                request.setAttribute(
                        "appointmentId",
                        rs.getInt(
                                "appointment_id"
                        )
                );


                request.setAttribute(
                        "appointmentNumber",
                        rs.getString(
                                "appointment_number"
                        )
                );


                request.setAttribute(
                        "patientName",
                        rs.getString(
                                "patient_name"
                        )
                );


                request.setAttribute(
                        "address",
                        rs.getString(
                                "address"
                        )
                );


                request.setAttribute(
                        "contactNumber",
                        rs.getString(
                                "contact_number"
                        )
                );
                
                request.setAttribute(
        "treatmentName",
        rs.getString("treatment_name")
);

request.setAttribute(
        "treatmentCost",
        rs.getBigDecimal("treatment_cost")
);


                java.sql.Date date =
                        rs.getDate(
                                "appointment_date"
                        );


                java.sql.Time time =
                        rs.getTime(
                                "appointment_time"
                        );


                request.setAttribute(
                        "appointmentDate",
                        date
                );


                request.setAttribute(
                        "appointmentTime",
                        time
                );


                /*
                 * Load dentists available
                 * for requested date/time.
                 */
                if (date != null
                        && time != null) {

                    loadAvailableDentists(
                            appointmentId,
                            date.toLocalDate(),
                            time.toString(),
                            request
                    );
                }


            } else {

                request.setAttribute(
                        "error",
                        "Appointment request not found."
                );
            }
        }
    }



    /*
     * Find dentists available on the
     * requested day and time.
     */
    private void loadAvailableDentists(
            int appointmentId,
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
                + "d.user_id, "
                + "d.specialization, "
                + "d.available_from, "
                + "d.available_to, "
                + "u.first_name, "
                + "u.last_name "
                + "FROM dentists d "
                + "LEFT JOIN users u "
                + "ON d.user_id = u.user_id "
                + "WHERE d.status = 'Active' "
                + "AND d.available_day = ? "
                + "AND ? BETWEEN "
                + "d.available_from AND d.available_to "
                + "AND NOT EXISTS ( "
                + "SELECT 1 "
                + "FROM appointments a "
                + "WHERE a.dentist_id = d.dentist_id "
                + "AND a.appointment_date = ? "
                + "AND a.appointment_time = ? "
                + "AND a.appointment_id <> ? "
                + "AND a.status IN "
                + "('Pending', 'Confirmed') "
                + ")";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {


            stmt.setString(
                    1,
                    dayName
            );


            stmt.setString(
                    2,
                    appointmentTime
            );


            stmt.setDate(
                    3,
                    java.sql.Date.valueOf(date)
            );


            stmt.setString(
                    4,
                    appointmentTime
            );


            stmt.setInt(
                    5,
                    appointmentId
            );


            ResultSet rs =
                    stmt.executeQuery();


            while (rs.next()) {


                Map<String, Object> dentist =
                        new HashMap<>();


                dentist.put(
                        "dentistId",
                        rs.getInt(
                                "dentist_id"
                        )
                );


                String firstName =
                        rs.getString(
                                "first_name"
                        );


                String lastName =
                        rs.getString(
                                "last_name"
                        );


                String dentistName;


                if (firstName != null) {

                    dentistName =
                            firstName
                            + " "
                            + (lastName == null
                                ? ""
                                : lastName);

                } else {

                    dentistName =
                            rs.getString(
                                    "dentist_name"
                            );
                }


                dentist.put(
                        "dentistName",
                        dentistName
                );


                dentist.put(
                        "specialization",
                        rs.getString(
                                "specialization"
                        )
                );


                dentist.put(
                        "availableFrom",
                        rs.getTime(
                                "available_from"
                        )
                );


                dentist.put(
                        "availableTo",
                        rs.getTime(
                                "available_to"
                        )
                );


                dentists.add(dentist);
            }
        }


        request.setAttribute(
                "availableDentists",
                dentists
        );
    }



    /*
     * Final server-side availability check.
     */
    private boolean isDentistAvailable(
            int appointmentId,
            int dentistId)
            throws SQLException {


        String sql =
                "SELECT "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "d.available_day, "
                + "d.available_from, "
                + "d.available_to, "
                + "d.status "
                + "FROM appointments a "
                + "CROSS JOIN dentists d "
                + "WHERE a.appointment_id = ? "
                + "AND d.dentist_id = ?";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {


            stmt.setInt(
                    1,
                    appointmentId
            );

            stmt.setInt(
                    2,
                    dentistId
            );


            ResultSet rs =
                    stmt.executeQuery();


            if (!rs.next()) {
                return false;
            }


            java.sql.Date date =
                    rs.getDate(
                            "appointment_date"
                    );


            java.sql.Time time =
                    rs.getTime(
                            "appointment_time"
                    );


            String availableDay =
                    rs.getString(
                            "available_day"
                    );


            java.sql.Time availableFrom =
                    rs.getTime(
                            "available_from"
                    );


            java.sql.Time availableTo =
                    rs.getTime(
                            "available_to"
                    );


            String dentistStatus =
                    rs.getString(
                            "status"
                    );


            if (!"Active".equalsIgnoreCase(
                    dentistStatus)) {

                return false;
            }


            String requestedDay =
                    date.toLocalDate()
                            .getDayOfWeek()
                            .getDisplayName(
                                    TextStyle.FULL,
                                    Locale.ENGLISH
                            );


            if (availableDay == null
                    || !availableDay.equalsIgnoreCase(
                            requestedDay)) {

                return false;
            }


            if (availableFrom == null
                    || availableTo == null
                    || time.before(availableFrom)
                    || time.after(availableTo)) {

                return false;
            }


            /*
             * Check double booking.
             */
            String conflictSql =
                    "SELECT appointment_id "
                    + "FROM appointments "
                    + "WHERE dentist_id = ? "
                    + "AND appointment_date = ? "
                    + "AND appointment_time = ? "
                    + "AND appointment_id <> ? "
                    + "AND status IN "
                    + "('Pending', 'Confirmed')";


            try (
                PreparedStatement conflictStmt =
                        conn.prepareStatement(
                                conflictSql
                        )
            ) {


                conflictStmt.setInt(
                        1,
                        dentistId
                );

                conflictStmt.setDate(
                        2,
                        date
                );

                conflictStmt.setTime(
                        3,
                        time
                );

                conflictStmt.setInt(
                        4,
                        appointmentId
                );


                ResultSet conflictRs =
                        conflictStmt.executeQuery();


                return !conflictRs.next();
            }
        }
    }



    /*
     * Treatments for Admin selection.
     */
    private void loadTreatments(
            HttpServletRequest request)
            throws SQLException {


        List<Map<String, Object>> treatments =
                new ArrayList<>();


        String sql =
                "SELECT "
                + "treatment_id, "
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
                        rs.getInt(
                                "treatment_id"
                        )
                );


                treatment.put(
                        "treatmentName",
                        rs.getString(
                                "treatment_name"
                        )
                );


                treatment.put(
                        "treatmentCost",
                        rs.getBigDecimal(
                                "treatment_cost"
                        )
                );


                treatments.add(treatment);
            }
        }


        request.setAttribute(
                "treatments",
                treatments
        );
    }
}