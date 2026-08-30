<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.time.LocalDateTime"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%!
    private String html(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    if (username == null || !("Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role))) {
        response.sendRedirect("login.jsp");
        return;
    }
    Boolean reportAvailable = (Boolean) request.getAttribute("reportAvailable");
    List<Map<String, Object>> dentistWorkload = (List<Map<String, Object>>) request.getAttribute("dentistWorkload");
    List<Map<String, Object>> treatmentPopularity = (List<Map<String, Object>>) request.getAttribute("treatmentPopularity");
    List<Map<String, Object>> dailyAppointments = (List<Map<String, Object>>) request.getAttribute("dailyAppointments");
    String error = (String) request.getAttribute("error");
    String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clinic Reports - Sunrise Dental Clinic</title>
    <style>
        * { box-sizing: border-box; } body { margin: 0; }
        .navbar { display: flex; justify-content: space-between; align-items: center; }
        .navbar-title { font-size: 22px; font-weight: bold; }
        .navbar a { color: white; text-decoration: none; font-weight: bold; }
        .container { max-width: 1180px; margin: 40px auto; padding: 0 20px; }
        .box { padding: 32px; background: white; }
        .report-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 20px; margin-bottom: 25px; }
        h1 { margin: 0 0 5px; } .subtitle { margin: 0; }
        .print-btn { min-height: 43px; padding: 0 18px; border: 0; font-weight: bold; cursor: pointer; }
        .message { padding: 13px; color: #9d2638; background: #ffeaed; border-radius: 9px; }
        .metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 29px; }
        .metric { padding: 19px; background: #f4fbf9; border: 1px solid #dce9e8; border-radius: 13px; }
        .metric span { display: block; color: #607583; font-size: 13px; }
        .metric strong { display: block; margin-top: 3px; color: #123047; font-size: 27px; }
        .report-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 22px; }
        .panel { overflow: hidden; border: 1px solid #dce9e8; border-radius: 13px; }
        .panel h2 { margin: 0; padding: 15px 17px; background: #f4fbf9; font-size: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 11px 14px; text-align: left; }
        td { border-bottom: 1px solid #dce9e8; }
        th:last-child, td:last-child { text-align: right; }
        .no-data { color: #607583; text-align: center !important; }
        .generated { margin-top: 22px; color: #607583; font-size: 13px; text-align: right; }
        .back-link { display: block; margin-top: 20px; text-align: center; text-decoration: none; font-weight: bold; }
        @media (max-width: 800px) { .metrics { grid-template-columns: 1fr 1fr; } .report-grid { grid-template-columns: 1fr; } }
        @media (max-width: 520px) { .box { padding: 22px 15px; } .metrics { grid-template-columns: 1fr; } .report-head { flex-direction: column; } }
        @media print { .navbar, .print-btn, .back-link { display: none !important; } body { background: white !important; } .container { width: 100% !important; margin: 0 !important; } .box { box-shadow: none !important; border: 0 !important; } }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>
<body>
    <header class="navbar"><div class="navbar-title">Sunrise Dental Clinic | Reports</div><a href="<%= "Admin".equalsIgnoreCase(role) ? "adminDashboard.jsp" : "dashboard.jsp" %>">Dashboard</a></header>
    <main class="container"><section class="box">
        <div class="report-head"><div><h1>Clinic Performance Report</h1><p class="subtitle">Appointment activity, revenue and workload information for management decisions.</p></div><button class="print-btn" type="button" onclick="window.print()">Print Report</button></div>
        <% if (error != null) { %><div class="message"><%= error %></div><% } %>
        <% if (Boolean.TRUE.equals(reportAvailable)) { %>
            <div class="metrics">
                <div class="metric"><span>All Appointments</span><strong><%= request.getAttribute("totalAppointments") %></strong></div>
                <div class="metric"><span>Appointments Today</span><strong><%= request.getAttribute("todaysAppointments") %></strong></div>
                <div class="metric"><span>Upcoming Active</span><strong><%= request.getAttribute("upcomingAppointments") %></strong></div>
                <div class="metric"><span>Confirmed Appointments</span><strong><%= request.getAttribute("confirmedAppointments") %></strong></div>
                <div class="metric"><span>Paid Bills</span><strong><%= request.getAttribute("paidBills") %></strong></div>
                <div class="metric"><span>Total Collected Revenue</span><strong>LKR <%= request.getAttribute("totalRevenue") %></strong></div>
            </div>
            <div class="report-grid">
                <section class="panel"><h2>Upcoming Dentist Workload</h2><table><thead><tr><th>Dentist</th><th>Appointments</th></tr></thead><tbody>
                    <% if (dentistWorkload == null || dentistWorkload.isEmpty()) { %><tr><td colspan="2" class="no-data">No workload data.</td></tr><% } else { for (Map<String, Object> row : dentistWorkload) { %><tr><td><%= html(String.valueOf(row.get("label"))) %></td><td><%= row.get("count") %></td></tr><% }} %>
                </tbody></table></section>
                <section class="panel"><h2>Treatment Demand</h2><table><thead><tr><th>Treatment</th><th>Appointments</th></tr></thead><tbody>
                    <% if (treatmentPopularity == null || treatmentPopularity.isEmpty()) { %><tr><td colspan="2" class="no-data">No treatment data.</td></tr><% } else { for (Map<String, Object> row : treatmentPopularity) { %><tr><td><%= html(String.valueOf(row.get("label"))) %></td><td><%= row.get("count") %></td></tr><% }} %>
                </tbody></table></section>
                <section class="panel"><h2>Appointments — Last 7 Days</h2><table><thead><tr><th>Date</th><th>Appointments</th></tr></thead><tbody>
                    <% if (dailyAppointments == null || dailyAppointments.isEmpty()) { %><tr><td colspan="2" class="no-data">No recent appointments.</td></tr><% } else { for (Map<String, Object> row : dailyAppointments) { %><tr><td><%= html(String.valueOf(row.get("label"))) %></td><td><%= row.get("count") %></td></tr><% }} %>
                </tbody></table></section>
            </div>
            <p class="generated">Generated <%= generated %></p>
        <% } %>
        <a class="back-link" href="<%= "Admin".equalsIgnoreCase(role) ? "adminDashboard.jsp" : "dashboard.jsp" %>">← Back to Dashboard</a>
    </section></main>
</body>
</html>
