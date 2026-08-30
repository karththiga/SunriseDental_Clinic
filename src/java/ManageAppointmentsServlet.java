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

@WebServlet("/ManageAppointmentsServlet")
public class ManageAppointmentsServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect("login.jsp");
            return;
        }

        String query = request.getParameter("query");
        query = query == null ? "" : query.trim();

        Object success = session.getAttribute("appointmentCancellationMessage");
        Object cancellationError = session.getAttribute("appointmentCancellationError");
        if (success != null) request.setAttribute("success", success.toString());
        if (cancellationError != null) request.setAttribute("error", cancellationError.toString());
        session.removeAttribute("appointmentCancellationMessage");
        session.removeAttribute("appointmentCancellationError");

        request.setAttribute("query", escapeHtml(query));
        loadAppointments(query, request);

        request.getRequestDispatcher("/manageAppointments.jsp")
                .forward(request, response);
    }

    /**
     * Loads every appointment when the query is blank. When a query is
     * supplied, the same prepared statement searches the three public
     * identifiers administrators commonly receive from patients.
     */
    private void loadAppointments(
            String query,
            HttpServletRequest request) {

        List<Map<String, Object>> appointments = new ArrayList<>();
        boolean searching = !query.isBlank();

        String sql =
                "SELECT a.appointment_id, a.appointment_number, "
                + "a.patient_name, a.contact_number, a.appointment_date, "
                + "a.appointment_time, a.status, a.cancellation_reason, "
                + "d.dentist_name, t.treatment_name, b.payment_status, "
                + "b.total_amount, b.refund_reference, b.refunded_amount "
                + "FROM appointments a "
                + "LEFT JOIN dentists d ON a.dentist_id=d.dentist_id "
                + "LEFT JOIN treatments t ON a.treatment_id=t.treatment_id "
                + "LEFT JOIN bills b ON a.appointment_id=b.appointment_id "
                + (searching
                    ? "WHERE a.appointment_number LIKE ? "
                      + "OR a.contact_number LIKE ? "
                      + "OR a.patient_name LIKE ? "
                    : "")
                + "ORDER BY a.appointment_date DESC, "
                + "a.appointment_time DESC, a.appointment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (searching) {
                String searchValue = "%" + query + "%";
                stmt.setString(1, searchValue);
                stmt.setString(2, searchValue);
                stmt.setString(3, searchValue);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("appointmentId", rs.getInt("appointment_id"));
                    row.put("appointmentNumber", safe(rs.getString("appointment_number")));
                    row.put("patientName", escapeHtml(rs.getString("patient_name")));
                    row.put("contactNumber", escapeHtml(rs.getString("contact_number")));
                    row.put("dentistName", safe(rs.getString("dentist_name")));
                    row.put("treatmentName", safe(rs.getString("treatment_name")));
                    row.put("appointmentDate", rs.getDate("appointment_date"));
                    row.put("appointmentTime", rs.getTime("appointment_time"));
                    row.put("status", safe(rs.getString("status")));
                    row.put("cancellationReason", escapeHtml(rs.getString("cancellation_reason")));
                    row.put("paymentStatus", safe(rs.getString("payment_status")));
                    row.put("totalAmount", rs.getBigDecimal("total_amount"));
                    row.put("refundReference", escapeHtml(rs.getString("refund_reference")));
                    row.put("refundedAmount", rs.getBigDecimal("refunded_amount"));
                    appointments.add(row);
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load appointments.", e);
            request.setAttribute(
                    "error",
                    "Unable to load appointments. Please try again."
            );
        }

        request.setAttribute("appointments", appointments);
        request.setAttribute("searching", searching);
    }

    private boolean isAdmin(HttpSession session) {
        return session != null
                && session.getAttribute("username") != null
                && ("Admin".equalsIgnoreCase((String) session.getAttribute("role"))
                    || "Staff".equalsIgnoreCase((String) session.getAttribute("role")));
    }

    private String safe(String value) {
        return value == null || value.isBlank()
                ? "Not assigned" : escapeHtml(value);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
