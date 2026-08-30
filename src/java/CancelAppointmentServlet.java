import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Admin-only controller for cancellation, refund and notification. */
@WebServlet("/CancelAppointmentServlet")
public class CancelAppointmentServlet extends HttpServlet {
    private final AppointmentCancellationFacade cancellationFacade =
            new AppointmentCancellationFacade();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null
                || !"Admin".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            int appointmentId = Integer.parseInt(request.getParameter("appointmentId"));
            int adminUserId = ((Number) session.getAttribute("user_id")).intValue();
            AppointmentCancellationFacade.CancellationResult result =
                    cancellationFacade.cancel(appointmentId, adminUserId,
                            request.getParameter("reason"));
            session.setAttribute("appointmentCancellationMessage",
                    successMessage(result));
        } catch (NumberFormatException | NullPointerException e) {
            session.setAttribute("appointmentCancellationError",
                    "Invalid appointment selection.");
        } catch (IllegalArgumentException e) {
            session.setAttribute("appointmentCancellationError", e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Unable to cancel appointment.", e);
            session.setAttribute("appointmentCancellationError",
                    "Cancellation could not be completed. No appointment or payment changes were saved.");
        }
        response.sendRedirect("ManageAppointmentsServlet");
    }

    private String successMessage(AppointmentCancellationFacade.CancellationResult result) {
        String message = "Appointment " + result.getAppointmentNumber() + " was cancelled.";
        if (result.isRefunded()) {
            message += " LKR " + result.getRefundedAmount().toPlainString()
                    + " was refunded (" + result.getRefundReference() + ").";
        } else {
            message += " No refund was required.";
        }
        message += result.isInAppNotificationAvailable()
                ? " The patient was notified in My Appointments."
                : " A notification record was created for the recorded contact number.";
        return message;
    }
}
