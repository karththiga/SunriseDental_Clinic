import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Controller for the simulated card gateway and atomic paid reservation. */
@WebServlet("/PatientPaymentServlet")
public class PatientPaymentServlet extends HttpServlet {
    private static final String SESSION_CHECKOUT = "pendingReservation";
    private final PatientPaymentFacade paymentFacade = new PatientPaymentFacade();
    private final DummyPaymentGateway gateway = new DummyPaymentGateway();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PendingReservation pending = getPending(request, response);
        if (pending == null) return;
        if (!loadCheckout(request, pending)) {
            request.getSession().removeAttribute(SESSION_CHECKOUT);
        }
        request.getRequestDispatcher("/patientPayment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PendingReservation pending = getPending(request, response);
        if (pending == null) return;

        try {
            PaymentSummary summary = paymentFacade.loadSummary(pending);
            if (summary == null) {
                throw new IllegalArgumentException(
                        "This checkout is no longer available. Please choose the appointment again.");
            }

            DummyPaymentGateway.Authorization authorization = gateway.authorize(
                    request.getParameter("cardholderName"),
                    request.getParameter("cardNumber"),
                    request.getParameter("expiry"),
                    request.getParameter("cvv"),
                    summary.getTotalAmount());

            PatientPaymentFacade.PaymentCompletion completion =
                    paymentFacade.completePayment(pending, authorization);
            request.getSession().removeAttribute(SESSION_CHECKOUT);
            response.sendRedirect("AppointmentConfirmationServlet?appointmentNumber="
                    + completion.getAppointmentNumber());
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            loadCheckout(request, pending);
            request.getRequestDispatcher("/patientPayment.jsp").forward(request, response);
        } catch (SQLException e) {
            getServletContext().log("Unable to complete patient payment.", e);
            request.setAttribute("error",
                    "Payment could not be completed. No appointment was created; please try again.");
            loadCheckout(request, pending);
            request.getRequestDispatcher("/patientPayment.jsp").forward(request, response);
        }
    }

    private PendingReservation getPending(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null
                || !"Patient".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return null;
        }

        Object value = session.getAttribute(SESSION_CHECKOUT);
        String token = request.getParameter("checkout");
        if (!(value instanceof PendingReservation)) {
            response.sendRedirect("PatientRequestAppointmentServlet");
            return null;
        }
        PendingReservation pending = (PendingReservation) value;
        if (pending.isExpired() || token == null
                || !pending.getCheckoutToken().equals(token)) {
            session.removeAttribute(SESSION_CHECKOUT);
            response.sendRedirect("PatientRequestAppointmentServlet");
            return null;
        }
        return pending;
    }

    private boolean loadCheckout(HttpServletRequest request,
            PendingReservation pending) {
        try {
            PaymentSummary summary = paymentFacade.loadSummary(pending);
            if (summary == null) {
                request.setAttribute("error", "Unable to load this checkout.");
                return false;
            }
            request.setAttribute("checkoutToken", pending.getCheckoutToken());
            request.setAttribute("patientName", html(summary.getPatientName()));
            request.setAttribute("dentistName", html(summary.getDentistName()));
            request.setAttribute("treatmentName", html(summary.getTreatmentName()));
            request.setAttribute("appointmentDate", pending.getAppointmentDate());
            request.setAttribute("appointmentTime", pending.getAppointmentTime());
            request.setAttribute("treatmentCost", summary.getTreatmentCost());
            request.setAttribute("hospitalCharge", summary.getHospitalCharge());
            request.setAttribute("totalAmount", summary.getTotalAmount());
            return true;
        } catch (SQLException e) {
            getServletContext().log("Unable to load patient checkout.", e);
            request.setAttribute("error", "Unable to load payment information.");
            return false;
        }
    }

    private String html(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
