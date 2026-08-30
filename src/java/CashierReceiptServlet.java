import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Dedicated cashier task page for one appointment payment and receipt. */
@WebServlet("/CashierReceiptServlet")
public class CashierReceiptServlet extends HttpServlet {
    private final BillingFacade billing = new BillingFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isCashier(request.getSession(false))) { response.sendRedirect("login.jsp"); return; }
        String number = clean(request.getParameter("appointmentNumber"));
        if (number == null) { response.sendRedirect("BillServlet"); return; }
        try {
            BillResult bill = billing.findByAppointmentNumber(number);
            if (bill == null) request.setAttribute("error", "Appointment not found.");
            else request.setAttribute("bill", map(bill));
            flash(request);
        } catch (SQLException e) {
            getServletContext().log("Unable to load cashier receipt.", e);
            request.setAttribute("error", "Unable to load payment information.");
        }
        request.getRequestDispatcher("/cashierReceipt.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isCashier(session)) { response.sendRedirect("login.jsp"); return; }
        String number = clean(request.getParameter("appointmentNumber"));
        try {
            BillResult result = billing.collectCounterPayment(number,
                    request.getParameter("paymentMethod"),
                    ((Number) session.getAttribute("user_id")).intValue());
            session.setAttribute("billingSuccess", "Payment collected successfully. Receipt "
                    + result.getPaymentReference() + " is ready to print.");
        } catch (IllegalArgumentException e) {
            session.setAttribute("billingError", e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Unable to collect counter payment.", e);
            session.setAttribute("billingError", "Payment was not recorded. Please try again.");
        }
        response.sendRedirect("CashierReceiptServlet?appointmentNumber=" + url(number));
    }

    private Map<String,Object> map(BillResult b) {
        Map<String,Object> m=new HashMap<>();m.put("appointmentNumber",html(b.getAppointmentNumber()));
        m.put("patientName",html(b.getPatientName()));m.put("contactNumber",html(b.getContactNumber()));
        m.put("dentistName",html(b.getDentistName()));m.put("treatmentName",html(b.getTreatmentName()));
        m.put("appointmentDate",b.getAppointmentDate());m.put("appointmentTime",b.getAppointmentTime());
        m.put("appointmentStatus",html(b.getAppointmentStatus()));m.put("paymentStatus",html(b.getPaymentStatus()));
        m.put("paymentReference",html(b.getPaymentReference()));m.put("paymentMethod",html(b.getPaymentMethod()));
        m.put("cardLastFour",html(b.getCardLastFour()));m.put("paymentDate",b.getPaymentDate());
        m.put("treatmentCost",b.getTreatmentCost());m.put("hospitalCharge",b.getHospitalCharge());
        m.put("totalAmount",b.getTotalAmount());m.put("refundedAmount",b.getRefundedAmount());
        m.put("refundReference",html(b.getRefundReference()));m.put("refundedAt",b.getRefundedAt());return m;
    }
    private void flash(HttpServletRequest r){HttpSession s=r.getSession();Object ok=s.getAttribute("billingSuccess"),bad=s.getAttribute("billingError");
        if(ok!=null)r.setAttribute("success",ok);if(bad!=null)r.setAttribute("error",bad);s.removeAttribute("billingSuccess");s.removeAttribute("billingError");}
    private boolean isCashier(HttpSession s){return s!=null&&s.getAttribute("username")!=null&&"Cashier".equalsIgnoreCase((String)s.getAttribute("role"));}
    private String clean(String v){return v==null||v.isBlank()?null:v.trim();}
    private String url(String v){return java.net.URLEncoder.encode(v==null?"":v,java.nio.charset.StandardCharsets.UTF_8);}
    private String html(String v){if(v==null)return "";return v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
