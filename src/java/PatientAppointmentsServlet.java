import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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


@WebServlet("/PatientAppointmentsServlet")
public class PatientAppointmentsServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("username") == null
                || !"Patient".equalsIgnoreCase(
                        (String) session.getAttribute("role"))) {

            response.sendRedirect("login.jsp");
            return;
        }

        int userId =
                (Integer) session.getAttribute("user_id");

        try {

            loadPatientAppointments(
                    userId,
                    request
            );
            loadNotifications(userId, request);

        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load your appointments."
            );
        }

        request.getRequestDispatcher(
                "/patientAppointments.jsp"
        ).forward(
                request,
                response
        );
    }


    private void loadPatientAppointments(
            int userId,
            HttpServletRequest request)
            throws SQLException {

        List<Map<String, Object>> appointments =
                new ArrayList<>();


        String sql =
                "SELECT "
                + "a.appointment_id, "
                + "a.appointment_number, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "a.status, a.cancellation_reason, "
                + "t.treatment_name, "
                + "d.dentist_name, b.payment_status, "
                + "b.refunded_amount, b.refund_reference "
                + "FROM appointments a "
                + "LEFT JOIN treatments t "
                + "ON a.treatment_id = t.treatment_id "
                + "LEFT JOIN dentists d "
                + "ON a.dentist_id = d.dentist_id "
                + "LEFT JOIN bills b ON a.appointment_id=b.appointment_id "
                + "WHERE a.patient_user_id = ? "
                + "ORDER BY a.appointment_date DESC, "
                + "a.appointment_time DESC";


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


            while (rs.next()) {

                Map<String, Object> row =
                        new HashMap<>();


                row.put(
                        "appointmentId",
                        rs.getInt("appointment_id")
                );

                row.put(
                        "appointmentNumber",
                        rs.getString("appointment_number")
                );

                row.put(
                        "appointmentDate",
                        rs.getDate("appointment_date")
                );

                row.put(
                        "appointmentTime",
                        rs.getTime("appointment_time")
                );

                row.put(
                        "status",
                        rs.getString("status")
                );

                row.put(
                        "treatmentName",
                        rs.getString("treatment_name")
                );

                row.put(
                        "dentistName",
                        rs.getString("dentist_name")
                );

                row.put("cancellationReason", html(rs.getString("cancellation_reason")));
                row.put("paymentStatus", rs.getString("payment_status"));
                row.put("refundedAmount", rs.getBigDecimal("refunded_amount"));
                row.put("refundReference", html(rs.getString("refund_reference")));


                appointments.add(row);
            }
        }


        request.setAttribute(
                "appointments",
                appointments
        );
    }

    /** Loads persistent in-app messages created by clinic administration. */
    private void loadNotifications(int userId, HttpServletRequest request)
            throws SQLException {
        List<Map<String, Object>> notifications = new ArrayList<>();
        String sql = "SELECT notification_id,title,message,is_read,created_at "
                + "FROM patient_notifications WHERE recipient_user_id=? "
                + "ORDER BY created_at DESC,notification_id DESC LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("notification_id"));
                    row.put("title", html(rs.getString("title")));
                    row.put("message", html(rs.getString("message")));
                    row.put("read", rs.getBoolean("is_read"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    notifications.add(row);
                }
            }
            try (PreparedStatement read = conn.prepareStatement(
                    "UPDATE patient_notifications SET is_read=1 "
                    + "WHERE recipient_user_id=? AND is_read=0")) {
                read.setInt(1, userId);
                read.executeUpdate();
            }
        }
        request.setAttribute("notifications", notifications);
    }

    private String html(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
