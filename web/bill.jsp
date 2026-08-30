<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%><%@page import="java.util.Map"%>
<%
String username=(String)session.getAttribute("username"),role=(String)session.getAttribute("role");
if(username==null||!"Cashier".equalsIgnoreCase(role)){response.sendRedirect("login.jsp");return;}
List<Map<String,Object>> appointments=(List<Map<String,Object>>)request.getAttribute("appointments");
String query=(String)request.getAttribute("query"),error=(String)request.getAttribute("error");
boolean searched=query!=null&&!query.isBlank();
%>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>Cashier Billing - Sunrise Dental Clinic</title>
<style>
*{box-sizing:border-box}body{margin:0}.navbar{display:flex;justify-content:space-between;align-items:center}.navbar a{color:white;text-decoration:none;font-weight:bold}.container{max-width:1200px;margin:40px auto;padding:0 20px}.box{padding:30px;background:white}.search{display:grid;grid-template-columns:1fr auto;gap:10px}.search input{min-height:46px;padding:11px;border:1px solid #bfd3d3}.search button{min-height:44px;padding:0 20px;border:0;font-weight:bold;cursor:pointer}.message{padding:13px;margin:15px 0;border-radius:9px}.error{color:#9d2638;background:#ffeaed}.result-summary{display:flex;justify-content:space-between;align-items:center;gap:15px;margin-top:25px}.result-summary h2{margin:0}.count{color:#607583}.table-wrap{margin-top:14px;overflow-x:auto;border:1px solid #dce9e8;border-radius:12px}table{width:100%;min-width:900px;border-collapse:collapse}th,td{padding:12px;text-align:left;border-bottom:1px solid #dce9e8}.status{padding:4px 9px;border-radius:999px;background:#fff5d8;font-weight:bold}.status.paid{color:#176454;background:#e8f8f2}.status.refunded{color:#9d2638;background:#ffeaed}.open{display:inline-flex;padding:8px 12px;color:white!important;background:#21a7a0;border-radius:8px;text-decoration:none;font-weight:bold;white-space:nowrap}.back{display:block;margin-top:22px;text-align:center;text-decoration:none;font-weight:bold}@media(max-width:650px){.search{grid-template-columns:1fr}.result-summary{align-items:flex-start;flex-direction:column}}
</style><link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css"></head><body>
<header class="navbar"><strong>Sunrise Dental Clinic | Cashier</strong><a href="cashierDashboard.jsp">Dashboard</a></header>
<main class="container"><section class="box"><h1>Appointments and Billing</h1><p>Search by appointment number, patient name or phone number. Open a focused payment and receipt page for each appointment.</p>
<form class="search" action="${pageContext.request.contextPath}/BillServlet" method="get"><input type="search" name="query" value="<%=query==null?"":query%>" placeholder="Appointment number, patient name or phone" aria-label="Search appointments"><button type="submit">Search</button></form>
<%if(error!=null){%><div class="message error" role="alert"><%=error%></div><%}%>
<section id="search-results" class="ux-focus-target" tabindex="-1" aria-labelledby="results-heading"><div class="result-summary"><h2 id="results-heading"><%=searched?"Search Results":"All Appointments"%></h2><span class="count"><%=appointments==null?0:appointments.size()%> appointment(s)</span></div>
<div class="table-wrap" aria-label="Cashier appointments"><table><thead><tr><th>Appointment</th><th>Patient</th><th>Phone</th><th>Date</th><th>Appointment Status</th><th>Payment</th><th>Action</th></tr></thead><tbody>
<%if(appointments!=null&&!appointments.isEmpty())for(Map<String,Object> item:appointments){String ps=String.valueOf(item.get("paymentStatus"));String number=String.valueOf(item.get("appointmentNumber"));%>
<tr><td><%=number%></td><td><%=item.get("patientName")%></td><td><%=item.get("contactNumber")%></td><td><%=item.get("appointmentDate")%> <%=item.get("appointmentTime")%></td><td><%=item.get("appointmentStatus")%></td><td><span class="status <%=ps.toLowerCase()%>"><%=ps%></span></td><td><a class="open" href="${pageContext.request.contextPath}/CashierReceiptServlet?appointmentNumber=<%=java.net.URLEncoder.encode(number,java.nio.charset.StandardCharsets.UTF_8)%>"><%="Paid".equalsIgnoreCase(ps)||"Refunded".equalsIgnoreCase(ps)?"Open Receipt":"Review & Collect Payment"%></a></td></tr>
<%}else{%><tr><td colspan="7">No appointments found.</td></tr><%}%>
</tbody></table></div></section></section><a class="back" href="cashierDashboard.jsp">← Back to Cashier Dashboard</a></main>
<script src="${pageContext.request.contextPath}/js/clinic-ux.js"></script><%if(searched){%><script>window.addEventListener("DOMContentLoaded",function(){clinicFocusSection("search-results");});</script><%}%>
</body></html>
