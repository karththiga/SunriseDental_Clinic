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
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
    String editId = (String) request.getAttribute("editTreatmentId");
    String editName = (String) request.getAttribute("editTreatmentName");
    String editCost = (String) request.getAttribute("editTreatmentCost");
    List<Map<String, Object>> treatments =
            (List<Map<String, Object>>) request.getAttribute("treatments");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Treatments - Sunrise Dental Clinic</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; }
        .navbar { display: flex; align-items: center; justify-content: space-between; }
        .navbar-title { font-size: 22px; font-weight: bold; }
        .navbar a { color: white; text-decoration: none; font-weight: bold; }
        .container { max-width: 1050px; margin: 40px auto; padding: 0 20px; }
        .box { padding: 32px; background: white; }
        h1 { margin: 0 0 5px; }
        .subtitle { margin: 0 0 25px; }
        .message { margin-bottom: 18px; padding: 12px 14px; border-radius: 9px; }
        .error { color: #9d2638; background: #ffeaed; }
        .success { color: #176454; background: #e8f8f2; }
        .form-panel { margin-bottom: 28px; padding: 22px; background: #f8fcfb; border: 1px solid #dce9e8; border-radius: 14px; }
        .form-panel h2 { margin: 0 0 15px; font-size: 22px; }
        .form-grid { display: grid; grid-template-columns: 1fr 220px auto; align-items: end; gap: 14px; }
        label { display: block; margin-bottom: 6px; color: #123047; font-size: 14px; font-weight: bold; }
        input { width: 100%; min-height: 45px; padding: 10px; border: 1px solid #bfd3d3; }
        button, .cancel { min-height: 45px; padding: 0 19px; border: 0; font-weight: bold; cursor: pointer; }
        .actions { display: flex; gap: 8px; }
        .cancel { display: inline-flex; align-items: center; color: #123047; background: #e9f8f5; border-radius: 9px; text-decoration: none; }
        .list-heading { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
        .list-heading h2 { margin: 0; font-size: 22px; }
        .count { color: #607583; font-size: 14px; }
        .table-container { overflow-x: auto; border: 1px solid #dce9e8; border-radius: 13px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 13px; text-align: left; }
        td { border-bottom: 1px solid #dce9e8; }
        .row-actions { display: flex; align-items: center; gap: 8px; }
        .inline-form { display: inline; margin: 0; }
        .edit-link { display: inline-flex; padding: 7px 12px; color: white !important; background: #21a7a0; border-radius: 8px; text-decoration: none; font-weight: bold; }
        .delete-button { min-height: auto; padding: 7px 12px; color: #9d2638; background: #ffeaed; border-radius: 8px; }
        .no-data { padding: 30px; color: #607583; text-align: center; }
        .back-link { display: block; margin-top: 23px; text-align: center; text-decoration: none; font-weight: bold; }
        @media (max-width: 700px) { .box { padding: 22px 16px; } .form-grid { grid-template-columns: 1fr; } }
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
            <h1>Manage Treatments</h1>
            <p class="subtitle">Maintain the treatment catalogue and prices used for appointment billing.</p>
            <% if (error != null) { %><div class="message error"><%= error %></div><% } %>
            <% if (success != null) { %><div class="message success"><%= success %></div><% } %>

            <section id="treatment-form" class="form-panel ux-focus-target" tabindex="-1">
                <h2><%= editId == null ? "Add Treatment" : "Update Treatment" %></h2>
                <form action="${pageContext.request.contextPath}/ManageTreatmentsServlet" method="post">
                    <% if (editId != null) { %><input type="hidden" name="treatmentId" value="<%= editId %>"><% } %>
                    <div class="form-grid">
                        <div><label for="treatmentName">Treatment Name</label><input id="treatmentName" name="treatmentName" value="<%= editName == null ? "" : editName %>" required></div>
                        <div><label for="treatmentCost">Cost (LKR)</label><input id="treatmentCost" name="treatmentCost" type="number" min="0" step="0.01" value="<%= editCost == null ? "" : editCost %>" required></div>
                        <div class="actions"><button type="submit"><%= editId == null ? "Add" : "Save" %></button><% if (editId != null) { %><a class="cancel" href="${pageContext.request.contextPath}/ManageTreatmentsServlet">Cancel</a><% } %></div>
                    </div>
                </form>
            </section>

            <div class="list-heading"><h2>Treatment Catalogue</h2><span class="count"><%= treatments == null ? 0 : treatments.size() %> treatment(s)</span></div>
            <div class="table-container">
                <table>
                    <thead><tr><th>ID</th><th>Treatment</th><th>Cost</th><th>Action</th></tr></thead>
                    <tbody>
                    <% if (treatments != null && !treatments.isEmpty()) {
                           for (Map<String, Object> item : treatments) { %>
                        <tr><td><%= item.get("id") %></td><td><%= item.get("name") %></td><td>LKR <%= item.get("cost") %></td><td><div class="row-actions"><a class="edit-link" href="${pageContext.request.contextPath}/ManageTreatmentsServlet?edit=<%= item.get("id") %>">Edit</a><form class="inline-form" action="${pageContext.request.contextPath}/ManageTreatmentsServlet" method="post" onsubmit="return confirm('Delete this treatment?');"><input type="hidden" name="action" value="delete"><input type="hidden" name="treatmentId" value="<%= item.get("id") %>"><button class="delete-button" type="submit">Delete</button></form></div></td></tr>
                    <%     }
                       } else { %><tr><td colspan="4" class="no-data">No treatments are configured.</td></tr><% } %>
                    </tbody>
                </table>
            </div>
            <a class="back-link" href="adminDashboard.jsp">← Back to Admin Dashboard</a>
        </section>
    </main>
    <script src="${pageContext.request.contextPath}/js/clinic-ux.js"></script>
    <% if (editId != null) { %><script>window.addEventListener("DOMContentLoaded", function () { clinicFocusSection("treatment-form"); });</script><% } %>
</body>
</html>
