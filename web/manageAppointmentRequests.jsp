<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%
    String username =
            (String) session.getAttribute("username");

    String role =
            (String) session.getAttribute("role");

    if (username == null
            || !"Admin".equalsIgnoreCase(role)) {

        response.sendRedirect("login.jsp");
        return;
    }


    String error =
            (String) request.getAttribute("error");

    String success =
            (String) request.getAttribute("success");


    List<Map<String, Object>> requests =
            (List<Map<String, Object>>)
            request.getAttribute("requests");
%>


<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>
    Manage Appointment Requests
</title>


<style>

    * {
        box-sizing: border-box;
    }

    body {
        margin: 0;
        font-family: Arial, sans-serif;
        background: #f4f7fb;
        color: #222;
    }

    .navbar {
        background: #21a7a0;
        color: white;

        padding: 18px 40px;

        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .navbar strong {
        font-size: 22px;
    }

    .navbar a {
        color: white;
        text-decoration: none;
        font-weight: bold;
    }

    .container {
        max-width: 1300px;
        margin: 40px auto;
        padding: 0 25px;
    }

    .box {
        background: white;

        padding: 30px;

        border-radius: 12px;

        box-shadow:
            0 5px 18px rgba(0,0,0,0.08);
    }

    h1 {
        margin-top: 0;
    }

    .message {
        padding: 12px;
        margin-bottom: 20px;

        border-radius: 7px;

        text-align: center;
    }

    .error {
        background: #ffe5e5;
        color: #b00020;
    }

    .success {
        background: #e5f7e8;
        color: #176b2c;
    }

    .table-container {
        overflow-x: auto;
    }

    table {
        width: 100%;
        border-collapse: collapse;
    }

    th {
        background: #21a7a0;
        color: white;

        padding: 12px;

        text-align: left;
    }

    td {
        padding: 12px;
        border-bottom: 1px solid #ddd;
    }

    .pending {
        color: #a96700;
        font-weight: bold;
    }

    .assign-btn {
        background: #21a7a0;
        color: white;

        border: none;

        padding: 8px 14px;

        border-radius: 6px;

        cursor: pointer;

        font-size: 14px;
    }

    .assign-btn:hover {
        opacity: 0.9;
    }

    .empty {
        text-align: center;
        color: #666;
    }

</style>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>


<body>


<div class="navbar">

    <strong>
        Sunrise Dental Clinic | Admin
    </strong>

    <a href="adminDashboard.jsp">
        Dashboard
    </a>

</div>


<div class="container">


<%
    if (error != null) {
%>

<div class="message error">
    <%= error %>
</div>

<%
    }

    if (success != null) {
%>

<div class="message success">
    <%= success %>
</div>

<%
    }
%>


<div class="box">


<h1>
    Pending Appointment Requests
</h1>


<div class="table-container">


<table>


<thead>


<tr>

    <th>Request ID</th>

    <th>Patient</th>

    <th>Treatment</th>

    <th>Dentist</th>

    <th>Contact</th>

    <th>Date</th>

    <th>Time</th>

    <th>Status</th>

    <th>Action</th>

</tr>


</thead>


<tbody>


<%
    if (requests != null
            && !requests.isEmpty()) {


        for (Map<String, Object> row
                : requests) {
%>


<tr>


    <td>
        <%= row.get("appointmentId") %>
    </td>


    <td>
        <%= row.get("patientName") %>
    </td>


    <td>

        <%
            if (row.get("treatmentName")
                    != null) {
        %>

            <%= row.get("treatmentName") %>

        <%
            } else {
        %>

            Not selected

        <%
            }
        %>

    </td>


    <td>

        <%
            if (row.get("dentistName")
                    != null) {
        %>

            <%= row.get("dentistName") %>

        <%
            } else {
        %>

            Not selected

        <%
            }
        %>

    </td>


    <td>
        <%= row.get("contactNumber") %>
    </td>


    <td>
        <%= row.get("appointmentDate") %>
    </td>


    <td>
        <%= row.get("appointmentTime") %>
    </td>


    <td class="pending">
        <%= row.get("status") %>
    </td>


    <td>


        <form
            action="${pageContext.request.contextPath}/ManageAppointmentRequestsServlet"
            method="post">


            <input
                type="hidden"
                name="appointmentId"
                value="<%= row.get("appointmentId") %>">


            <button
                type="submit"
                class="assign-btn">

                Assign Appointment

            </button>


        </form>


    </td>


</tr>


<%
        }

    } else {
%>


<tr>

    <td
        colspan="9"
        class="empty">

        No pending appointment requests.

    </td>

</tr>


<%
    }
%>


</tbody>


</table>


</div>


</div>


</div>


</body>

</html>
