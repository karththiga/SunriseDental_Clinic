import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AppointmentConfirmationServlet")
public class AppointmentConfirmationServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null
                || session.getAttribute("username") == null
                || !"Patient".equalsIgnoreCase(
                        (String) session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String appointmentNumber = request.getParameter("appointmentNumber");
        if (appointmentNumber == null || appointmentNumber.isBlank()) {
            response.sendRedirect("PatientAppointmentsServlet");
            return;
        }

        int patientUserId = (Integer) session.getAttribute("user_id");

        String sql =
                "SELECT a.appointment_number, a.patient_name, "
                + "a.contact_number, a.appointment_date, "
                + "a.appointment_time, a.status, d.dentist_name, "
                + "d.specialization, d.consultation_fee, "
                + "t.treatment_name, t.treatment_cost "
                + "FROM appointments a "
                + "INNER JOIN dentists d ON a.dentist_id = d.dentist_id "
                + "INNER JOIN treatments t ON a.treatment_id = t.treatment_id "
                + "WHERE a.appointment_number = ? AND a.patient_user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, appointmentNumber.trim());
            stmt.setInt(2, patientUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    request.setAttribute(
                            "error",
                            "Appointment confirmation was not found."
                    );
                } else {
                    request.setAttribute(
                            "appointmentNumber",
                            rs.getString("appointment_number")
                    );
                    request.setAttribute("patientName", rs.getString("patient_name"));
                    request.setAttribute("contactNumber", rs.getString("contact_number"));
                    request.setAttribute("dentistName", rs.getString("dentist_name"));
                    request.setAttribute("specialization", rs.getString("specialization"));
                    request.setAttribute("treatmentName", rs.getString("treatment_name"));
                    request.setAttribute("appointmentDate", rs.getDate("appointment_date"));
                    request.setAttribute("appointmentTime", rs.getTime("appointment_time"));
                    request.setAttribute("status", rs.getString("status"));
                    request.setAttribute("treatmentCost", rs.getBigDecimal("treatment_cost"));
                    request.setAttribute("consultationFee", rs.getBigDecimal("consultation_fee"));
                    request.setAttribute("found", true);
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load appointment confirmation.", e);
            request.setAttribute(
                    "error",
                    "Unable to load the confirmation receipt."
            );
        }

        request.getRequestDispatcher("/appointmentConfirmation.jsp")
                .forward(request, response);
    }
}
