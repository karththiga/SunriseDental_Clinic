<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    if (username == null || !"Admin".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp");
        return;
    }

    String activeTab = (String) request.getAttribute("activeTab");
    if (!"dentists".equals(activeTab)) {
        activeTab = "users";
    }
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
    List<Map<String, Object>> users =
            (List<Map<String, Object>>) request.getAttribute("users");
    List<Map<String, Object>> dentists =
            (List<Map<String, Object>>) request.getAttribute("dentists");
    List<Map<String, Object>> treatments =
            (List<Map<String, Object>>) request.getAttribute("treatments");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage People - Sunrise Dental Clinic</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; }
        .navbar { display: flex; align-items: center; justify-content: space-between; }
        .navbar-title { font-size: 22px; font-weight: bold; }
        .navbar a { color: white; text-decoration: none; font-weight: bold; }
        .container { max-width: 1180px; margin: 40px auto; padding: 0 20px; }
        .box { padding: 32px; background: white; }
        h1 { margin: 0 0 5px; }
        .subtitle { margin: 0 0 24px; }
        .tabs { display: flex; gap: 8px; margin-bottom: 27px; padding-bottom: 12px; border-bottom: 1px solid #dce9e8; }
        .tab { padding: 10px 18px; color: #176b87; background: #e9f8f5; border-radius: 9px; text-decoration: none; font-weight: bold; }
        .tab.active { color: white !important; background: #123047; }
        .message { margin-bottom: 20px; padding: 13px 15px; border-radius: 9px; }
        .message.error { color: #9d2638; background: #ffeaed; border: 1px solid #f0bdc4; }
        .message.success { color: #176454; background: #e8f8f2; border: 1px solid #b9dfd2; }
        .section-heading { margin: 0 0 16px; font-size: 23px; }
        .form-panel { margin-bottom: 30px; padding: 24px; background: #f8fcfb; border: 1px solid #dce9e8; border-radius: 14px; }
        .form-panel h2 { margin-top: 0; }
        .form-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 17px; }
        .field { display: flex; flex-direction: column; gap: 6px; }
        .field label, .field-title { color: #123047; font-size: 14px; font-weight: bold; }
        .field input, .field select { width: 100%; min-height: 45px; padding: 10px 11px; border: 1px solid #bfd3d3; background: white; }
        .span-all { grid-column: 1 / -1; }
        .treatment-options { display: flex; flex-wrap: wrap; gap: 9px; margin-top: 7px; }
        .check-option { display: inline-flex; align-items: center; gap: 7px; padding: 8px 11px; background: white; border: 1px solid #bfd3d3; border-radius: 9px; }
        .check-option input { width: auto; min-height: auto; margin: 0; }
        .form-actions { display: flex; justify-content: flex-end; margin-top: 20px; }
        .form-actions button { min-height: 45px; padding: 0 22px; border: 0; font-size: 14px; font-weight: bold; cursor: pointer; }
        .list-heading { display: flex; justify-content: space-between; align-items: center; gap: 15px; margin-bottom: 13px; }
        .list-heading h2 { margin: 0; font-size: 23px; }
        .count { color: #607583; font-size: 14px; }
        .table-container { overflow-x: auto; border: 1px solid #dce9e8; border-radius: 13px; }
        table { width: 100%; min-width: 850px; border-collapse: collapse; }
        .dentist-table { min-width: 1120px; }
        th, td { padding: 12px; text-align: left; vertical-align: top; }
        td { border-bottom: 1px solid #dce9e8; }
        tbody tr:last-child td { border-bottom: 0; }
        .name { color: #123047; font-weight: bold; }
        .status { display: inline-flex; padding: 4px 9px; color: #176454; background: #e8f8f2; border-radius: 999px; font-size: 12px; font-weight: bold; }
        .no-data { padding: 35px; color: #607583; text-align: center; }
        .back-link { display: block; margin-top: 24px; text-align: center; text-decoration: none; font-weight: bold; }
        @media (max-width: 850px) {
            .form-grid { grid-template-columns: 1fr 1fr; }
        }
        @media (max-width: 600px) {
            .box { padding: 22px 16px; }
            .form-panel { padding: 18px 14px; }
            .form-grid { grid-template-columns: 1fr; }
            .tabs { display: grid; grid-template-columns: 1fr 1fr; }
            .tab { text-align: center; }
            .list-heading { align-items: flex-start; flex-direction: column; gap: 2px; }
        }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>
<body>
    <header class="navbar">
        <div class="navbar-title">Sunrise Dental Clinic | Admin</div>
        <a href="adminDashboard.jsp">Admin Dashboard</a>
    </header>

    <main class="container">
        <section class="box">
            <h1>Manage People</h1>
            <p class="subtitle">Manage clinic accounts and complete dentist profiles from one place.</p>

            <nav class="tabs" aria-label="People management sections">
                <a class="tab <%= "users".equals(activeTab) ? "active" : "" %>"
                   href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=users">User Accounts</a>
                <a class="tab <%= "dentists".equals(activeTab) ? "active" : "" %>"
                   href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=dentists">Dentist Profiles</a>
            </nav>

            <% if (error != null) { %>
                <div class="message error"><%= error %></div>
            <% } %>
            <% if (success != null) { %>
                <div class="message success"><%= success %></div>
            <% } %>

            <% if ("users".equals(activeTab)) { %>
                <section class="form-panel">
                    <h2 class="section-heading">Add User Account</h2>
                    <form action="${pageContext.request.contextPath}/ManagePeopleServlet" method="post">
                        <input type="hidden" name="action" value="createUser">
                        <div class="form-grid">
                            <div class="field">
                                <label for="firstName">First Name</label>
                                <input id="firstName" name="firstName" type="text" required>
                            </div>
                            <div class="field">
                                <label for="lastName">Last Name</label>
                                <input id="lastName" name="lastName" type="text" required>
                            </div>
                            <div class="field">
                                <label for="username">Email Address</label>
                                <input id="username" name="username" type="email" required>
                            </div>
                            <div class="field">
                                <label for="phoneNumber">Phone Number</label>
                                <input id="phoneNumber" name="phoneNumber" type="tel" pattern="[0-9]{10}" maxlength="10" required>
                            </div>
                            <div class="field">
                                <label for="password">Temporary Password</label>
                                <input id="password" name="password" type="password" minlength="6" required>
                            </div>
                            <div class="field">
                                <label for="role">Role</label>
                                <select id="role" name="role" required>
                                    <option value="Patient">Patient</option>
                                    <option value="Cashier">Cashier</option>
                                    <option value="Admin">Admin</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-actions"><button type="submit">Create User</button></div>
                    </form>
                </section>

                <div class="list-heading">
                    <h2>All User Accounts</h2>
                    <span class="count"><%= users == null ? 0 : users.size() %> account(s)</span>
                </div>
                <div class="table-container">
                    <table>
                        <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Role</th></tr></thead>
                        <tbody>
                        <% if (users != null && !users.isEmpty()) {
                               for (Map<String, Object> user : users) { %>
                            <tr>
                                <td><%= user.get("userId") %></td>
                                <td class="name"><%= user.get("name") %></td>
                                <td><%= user.get("username") %></td>
                                <td><%= user.get("phoneNumber") %></td>
                                <td><%= user.get("role") %></td>
                            </tr>
                        <%     }
                           } else { %>
                            <tr><td colspan="5" class="no-data">No user accounts are available.</td></tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <section class="form-panel">
                    <h2 class="section-heading">Add Dentist</h2>
                    <form action="${pageContext.request.contextPath}/ManagePeopleServlet" method="post">
                        <input type="hidden" name="action" value="createDentist">
                        <div class="form-grid">
                            <div class="field">
                                <label for="dentistFirstName">First Name</label>
                                <input id="dentistFirstName" name="firstName" type="text" required>
                            </div>
                            <div class="field">
                                <label for="dentistLastName">Last Name</label>
                                <input id="dentistLastName" name="lastName" type="text" required>
                            </div>
                            <div class="field">
                                <label for="dentistUsername">Login Email</label>
                                <input id="dentistUsername" name="username" type="email" required>
                            </div>
                            <div class="field">
                                <label for="dentistPhone">Phone Number</label>
                                <input id="dentistPhone" name="phoneNumber" type="tel" pattern="[0-9]{10}" maxlength="10" required>
                            </div>
                            <div class="field">
                                <label for="dentistPassword">Temporary Password</label>
                                <input id="dentistPassword" name="password" type="password" minlength="6" required>
                            </div>
                            <div class="field">
                                <label for="dentistName">Display Name</label>
                                <input id="dentistName" name="dentistName" type="text" placeholder="Dr. Full Name" required>
                            </div>
                            <div class="field">
                                <label for="specialization">Specialization</label>
                                <input id="specialization" name="specialization" type="text" required>
                            </div>
                            <div class="field">
                                <label for="qualification">Qualification</label>
                                <input id="qualification" name="qualification" type="text" required>
                            </div>
                            <div class="field">
                                <label for="consultationFee">Consultation Fee (LKR)</label>
                                <input id="consultationFee" name="consultationFee" type="number" min="0" step="0.01" required>
                            </div>
                            <div class="field">
                                <label for="availableDay">Visiting Day</label>
                                <select id="availableDay" name="availableDay" required>
                                    <option value="">Select day</option>
                                    <option>Monday</option><option>Tuesday</option><option>Wednesday</option>
                                    <option>Thursday</option><option>Friday</option><option>Saturday</option>
                                </select>
                            </div>
                            <div class="field">
                                <label for="availableFrom">Available From</label>
                                <input id="availableFrom" name="availableFrom" type="time" required>
                            </div>
                            <div class="field">
                                <label for="availableTo">Available To</label>
                                <input id="availableTo" name="availableTo" type="time" required>
                            </div>
                            <div class="span-all">
                                <div class="field-title">Treatments</div>
                                <div class="treatment-options">
                                <% if (treatments != null && !treatments.isEmpty()) {
                                       for (Map<String, Object> treatment : treatments) { %>
                                    <label class="check-option">
                                        <input type="checkbox" name="treatmentIds" value="<%= treatment.get("treatmentId") %>">
                                        <%= treatment.get("treatmentName") %>
                                    </label>
                                <%     }
                                   } else { %>
                                    <span>No treatments are configured. Add treatments before creating a dentist.</span>
                                <% } %>
                                </div>
                            </div>
                        </div>
                        <div class="form-actions"><button type="submit">Create Dentist</button></div>
                    </form>
                </section>

                <div class="list-heading">
                    <h2>All Dentist Profiles</h2>
                    <span class="count"><%= dentists == null ? 0 : dentists.size() %> dentist(s)</span>
                </div>
                <div class="table-container">
                    <table class="dentist-table">
                        <thead>
                            <tr><th>Dentist</th><th>Login Email</th><th>Specialization</th><th>Qualification</th><th>Visiting Hours</th><th>Treatments</th><th>Fee</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                        <% if (dentists != null && !dentists.isEmpty()) {
                               for (Map<String, Object> dentist : dentists) { %>
                            <tr>
                                <td class="name"><%= dentist.get("dentistName") %></td>
                                <td><%= dentist.get("username") %></td>
                                <td><%= dentist.get("specialization") %></td>
                                <td><%= dentist.get("qualification") %></td>
                                <td><%= dentist.get("availableDay") %><br><%= dentist.get("availableFrom") %> – <%= dentist.get("availableTo") %></td>
                                <td><%= dentist.get("treatments") %></td>
                                <td>LKR <%= dentist.get("consultationFee") %></td>
                                <td><span class="status"><%= dentist.get("status") %></span></td>
                            </tr>
                        <%     }
                           } else { %>
                            <tr><td colspan="8" class="no-data">No dentist profiles are available.</td></tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>

            <a class="back-link" href="adminDashboard.jsp">← Back to Admin Dashboard</a>
        </section>
    </main>
</body>
</html>
