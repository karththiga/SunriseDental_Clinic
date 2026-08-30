import java.io.IOException;
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

/** Cashier-only controller for searching, collecting payment and printing. */
@WebServlet("/BillServlet")
public class BillServlet extends HttpServlet {
    private final BillingFacade billing=new BillingFacade();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(!isCashier(req.getSession(false))){resp.sendRedirect("login.jsp");return;}
        String query=clean(req.getParameter("query"));String number=clean(req.getParameter("appointmentNumber"));
        try{req.setAttribute("query",html(query));req.setAttribute("appointments",maps(billing.searchAppointments(query)));
            if(number!=null){BillResult bill=billing.findByAppointmentNumber(number);if(bill==null)req.setAttribute("error","Appointment not found.");else req.setAttribute("bill",map(bill));}
            flash(req);}
        catch(SQLException e){getServletContext().log("Unable to load cashier billing.",e);req.setAttribute("error","Unable to load billing information.");}
        req.getRequestDispatcher("/bill.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        HttpSession session=req.getSession(false);if(!isCashier(session)){resp.sendRedirect("login.jsp");return;}
        String number=clean(req.getParameter("appointmentNumber"));
        try{BillResult result=billing.collectCounterPayment(number,req.getParameter("paymentMethod"),((Number)session.getAttribute("user_id")).intValue());
            session.setAttribute("billingSuccess","Payment collected successfully. Receipt "+result.getPaymentReference()+" is ready to print.");}
        catch(IllegalArgumentException e){session.setAttribute("billingError",e.getMessage());}
        catch(SQLException e){getServletContext().log("Unable to collect counter payment.",e);session.setAttribute("billingError","Payment was not recorded. Please try again.");}
        resp.sendRedirect("BillServlet?appointmentNumber="+url(number));
    }
    private void flash(HttpServletRequest r){HttpSession s=r.getSession();Object ok=s.getAttribute("billingSuccess"),bad=s.getAttribute("billingError");
        if(ok!=null)r.setAttribute("success",ok);if(bad!=null)r.setAttribute("error",bad);s.removeAttribute("billingSuccess");s.removeAttribute("billingError");}
    private boolean isCashier(HttpSession s){return s!=null&&s.getAttribute("username")!=null&&"Cashier".equalsIgnoreCase((String)s.getAttribute("role"));}
    private String clean(String v){return v==null||v.isBlank()?null:v.trim();}private String url(String v){return java.net.URLEncoder.encode(v==null?"":v,java.nio.charset.StandardCharsets.UTF_8);}
    private String html(String v){if(v==null)return "";return v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
    private List<Map<String,Object>> maps(List<BillResult> bills){List<Map<String,Object>> rows=new ArrayList<>();for(BillResult bill:bills)rows.add(map(bill));return rows;}
    private Map<String,Object> map(BillResult b){Map<String,Object> m=new HashMap<>();m.put("appointmentNumber",html(b.getAppointmentNumber()));m.put("patientName",html(b.getPatientName()));
        m.put("contactNumber",html(b.getContactNumber()));m.put("dentistName",html(b.getDentistName()));m.put("treatmentName",html(b.getTreatmentName()));
        m.put("appointmentDate",b.getAppointmentDate());m.put("appointmentTime",b.getAppointmentTime());m.put("appointmentStatus",html(b.getAppointmentStatus()));
        m.put("paymentStatus",html(b.getPaymentStatus()));m.put("paymentReference",html(b.getPaymentReference()));m.put("paymentMethod",html(b.getPaymentMethod()));
        m.put("cardLastFour",html(b.getCardLastFour()));m.put("paymentDate",b.getPaymentDate());m.put("treatmentCost",b.getTreatmentCost());
        m.put("hospitalCharge",b.getHospitalCharge());m.put("totalAmount",b.getTotalAmount());m.put("refundedAmount",b.getRefundedAmount());
        m.put("refundReference",html(b.getRefundReference()));m.put("refundedAt",b.getRefundedAt());return m;}
}
