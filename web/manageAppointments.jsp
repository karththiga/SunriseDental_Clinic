<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    if (username == null || !("Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role))) {
        response.sendRedirect("login.jsp");
        return;
    }

    String error = (String) request.getAttribute("error");
    String query = (String) request.getAttribute("query");
    Boolean searching = (Boolean) request.getAttribute("searching");
    List<Map<String, Object>> appointments =
            (List<Map<String, Object>>) request.getAttribute("appointments");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Appointments - Sunrise Dental Clinic</title>

    <style>
        * { box-sizing: border-box; }
        body { margin: 0; }
        .navbar { display: flex; align-items: center; justify-content: space-between; }
        .navbar-title { font-size: 22px; font-weight: bold; }
        .navbar a { color: white; text-decoration: none; font-weight: bold; }
        .container { max-width: 1250px; margin: 40px auto; padding: 0 20px; }
        .box { padding: 32px; background: white; }
        h1 { margin: 0 0 6px; }
        .subtitle { margin: 0 0 26px; }
        .search-form { display: grid; grid-template-columns: 1fr auto auto; gap: 10px; margin-bottom: 25px; }
        .search-form input { min-width: 0; padding: 13px; border: 1px solid #bfd3d3; font-size: 15px; }
        .search-form button, .clear-link { min-height: 46px; padding: 0 20px; border: 0; border-radius: 9px; font-size: 14px; font-weight: bold; }
        .clear-link { display: inline-flex; align-items: center; color: #123047; background: #e9f8f5; text-decoration: none; }
        .message { margin-bottom: 20px; padding: 13px; border-radius: 9px; }
        .list-summary { display: flex; justify-content: space-between; align-items: center; gap: 18px; margin-bottom: 14px; }
        .list-summary h2 { margin: 0; font-size: 23px; }
        .count { color: #607583; font-size: 14px; }
        .table-container { overflow-x: auto; border: 1px solid #dce9e8; border-radius: 13px; }
        table { width: 100%; min-width: 980px; border-collapse: collapse; }
        th, td { padding: 13px 12px; text-align: left; }
        td { border-bottom: 1px solid #dce9e8; }
        tbody tr:last-child td { border-bottom: 0; }
        .number { color: #176b87; font-weight: bold; }
        .status { display: inline-flex; padding: 5px 10px; border-radius: 999px; background: #e9f8f5; font-size: 12px; font-weight: bold; }
        .status.rejected, .status.cancelled { color: #9d2638; background: #ffeaed; }
        .status.pending { color: #8a6100; background: #fff5d8; }
        .edit-link { width: 38px; height: 38px; display: inline-grid; place-items: center; color: white !important; background: #21a7a0; border-radius: 9px; text-decoration: none; font-size: 18px; }
        .edit-link:hover { background: #123047; }
        .no-data { padding: 38px; color: #607583; text-align: center; }
        .back-link { display: block; margin-top: 24px; text-align: center; text-decoration: none; font-weight: bold; }
        @media (max-width: 700px) {
            .box { padding: 23px 17px; }
            .search-form { grid-template-columns: 1fr; }
            .clear-link { justify-content: center; }
            .list-summary { align-items: flex-start; flex-direction: column; gap: 3px; }
        }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>

<body>
    <header class="navbar">
        <div class="navbar-title">Sunrise Dental Clinic | Admin</div>
        <a href="<%= "Admin".equalsIgnoreCase(role) ? "adminDashboard.jsp" : "dashboard.jsp" %>">Dashboard</a>
    </header>

    <main class="container">
        <section class="box">
            <h1>Manage Appointments</h1>
            <p class="subtitle">Search by appointment number, patient phone number or patient name.</p>

            <form class="search-form"
                  action="${pageContext.request.contextPath}/ManageAppointmentsServlet"
                  method="get">
                <input type="search" name="query"
                       value="<%= query == null ? "" : query %>"
                       placeholder="Search appointment number, phone or patient name"
                       aria-label="Search appointments">
                <button type="submit">Search</button>
                <% if (Boolean.TRUE.equals(searching)) { %>
                    <a class="clear-link" href="${pageContext.request.contextPath}/ManageAppointmentsServlet">Clear</a>
                <% } %>
            </form>

            <% if (error != null) { %>
                <div class="message error"><%= error %></div>
            <% } %>

            <div class="list-summary">
                <h2><%= Boolean.TRUE.equals(searching) ? "Search Results" : "All Appointments" %></h2>
                <span class="count"><%= appointments == null ? 0 : appointments.size() %> appointment(s)</span>
            </div>

            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Appointment</th>
                            <th>Patient</th>
                            <th>Phone</th>
                            <th>Dentist</th>
                            <th>Treatment</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Status</th>
                            <th>Edit</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (appointments != null && !appointments.isEmpty()) {
                            for (Map<String, Object> row : appointments) {
                                String status = String.valueOf(row.get("status"));
                                String statusClass = status.toLowerCase(); %>
                            <tr>
                                <td class="number"><%= row.get("appointmentNumber") %></td>
                                <td><%= row.get("patientName") %></td>
                                <td><%= row.get("contactNumber") %></td>
                                <td><%= row.get("dentistName") %></td>
                                <td><%= row.get("treatmentName") %></td>
                                <td><%= row.get("appointmentDate") %></td>
                                <td><%= row.get("appointmentTime") %></td>
                                <td><span class="status <%= statusClass %>"><%= status %></span></td>
                                <td>
                                    <a class="edit-link"
                                       href="${pageContext.request.contextPath}/UpdateAppointmentServlet?appointmentNumber=<%= row.get("appointmentNumber") %>"
                                       title="Edit appointment <%= row.get("appointmentNumber") %>"
                                       aria-label="Edit appointment <%= row.get("appointmentNumber") %>">✎</a>
                                </td>
                            </tr>
                        <%  }
                           } else { %>
                            <tr>
                                <td colspan="9" class="no-data">
                                    <%= Boolean.TRUE.equals(searching)
                                            ? "No appointments match your search."
                                            : "No appointments are available." %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>

            <a class="back-link" href="<%= "Admin".equalsIgnoreCase(role) ? "adminDashboard.jsp" : "dashboard.jsp" %>">← Back to Dashboard</a>
    </main>
</body>
</html>
