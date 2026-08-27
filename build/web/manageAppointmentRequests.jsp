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


    List<Map<String, Object>> availableDentists =
            (List<Map<String, Object>>)
            request.getAttribute("availableDentists");


    List<Map<String, Object>> treatments =
            (List<Map<String, Object>>)
            request.getAttribute("treatments");


    Boolean selected =
            (Boolean) request.getAttribute("selected");
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
        background: #1f6feb;
        color: white;
        padding: 18px 40px;

        display: flex;
        justify-content: space-between;
    }

    .navbar a {
        color: white;
        text-decoration: none;
        font-weight: bold;
    }

    .container {
        max-width: 1200px;
        margin: 40px auto;
        padding: 0 25px;
    }

    .box {
        background: white;
        padding: 30px;
        border-radius: 12px;
        margin-bottom: 30px;

        box-shadow:
            0 5px 18px rgba(0,0,0,0.08);
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
        background: #1f6feb;
        color: white;
        padding: 12px;
        text-align: left;
    }

    td {
        padding: 12px;
        border-bottom: 1px solid #ddd;
    }

    .manage-btn {
        background: #1f6feb;
        color: white;
        padding: 7px 13px;
        border-radius: 6px;
        text-decoration: none;
    }

    .details-grid {
        display: grid;
        grid-template-columns:
            repeat(2, 1fr);
        gap: 15px;
    }

    .detail {
        background: #f7f9fc;
        padding: 13px;
        border-radius: 7px;
    }

    .detail strong {
        display: block;
        margin-bottom: 5px;
    }

    label {
        display: block;
        margin-top: 18px;
        margin-bottom: 7px;
        font-weight: bold;
    }

    select {
        width: 100%;
        padding: 12px;
        border: 1px solid #ccc;
        border-radius: 7px;
    }

    .assign-btn {
        width: 100%;
        margin-top: 25px;
        padding: 13px;

        background: #1f6feb;
        color: white;

        border: none;
        border-radius: 7px;

        font-size: 16px;
        cursor: pointer;
    }

    .pending {
        color: #a96700;
        font-weight: bold;
    }

    @media(max-width: 700px) {

        .details-grid {
            grid-template-columns: 1fr;
        }
    }

</style>

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
<th>Appointment No.</th>
<th>Patient</th>
<th>Treatment</th>
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

        <%
            if (row.get("appointmentNumber")
                    == null) {
        %>

            Not assigned

        <%
            } else {
        %>

            <%= row.get("appointmentNumber") %>

        <%
            }
        %>

    </td>


    <td>
        <%= row.get("patientName") %>
    </td>
    
    <td>

    <%
        if (row.get("treatmentName") != null) {
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

        <a
            class="manage-btn"
            href="${pageContext.request.contextPath}/ManageAppointmentRequestsServlet?appointmentId=<%= row.get("appointmentId") %>">

            Manage

        </a>

    </td>

</tr>


<%
        }

    } else {
%>


<tr>

    <td colspan="9"
        style="text-align:center;">

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



<%
    if (Boolean.TRUE.equals(selected)) {
%>


<div class="box">


<h2>
    Process Appointment Request
</h2>


<div class="details-grid">


    <div class="detail">

        <strong>
            Patient
        </strong>

        <%= request.getAttribute("patientName") %>

    </div>


    <div class="detail">

        <strong>
            Contact
        </strong>

        <%= request.getAttribute("contactNumber") %>

    </div>


    <div class="detail">

        <strong>
            Address
        </strong>

        <%= request.getAttribute("address") %>

    </div>


    <div class="detail">

        <strong>
            Requested Date
        </strong>

        <%= request.getAttribute("appointmentDate") %>

    </div>


    <div class="detail">

        <strong>
            Requested Time
        </strong>

        <%= request.getAttribute("appointmentTime") %>

    </div>
        
        <div class="detail">

    <strong>
        Requested Treatment
    </strong>

    <%= request.getAttribute("treatmentName") %>

    <br>

    Rs.
    <%= request.getAttribute("treatmentCost") %>

</div>


</div>



<form
    action="${pageContext.request.contextPath}/ManageAppointmentRequestsServlet"
    method="post">


    <input
        type="hidden"
        name="appointmentId"
        value="<%= request.getAttribute("appointmentId") %>">


    <label>
        Available Dentist
    </label>


    <select
        name="dentistId"
        required>


        <option value="">
            Select Available Dentist
        </option>


        <%
            if (availableDentists != null) {

                for (Map<String, Object> dentist
                        : availableDentists) {
        %>


        <option
            value="<%= dentist.get("dentistId") %>">

            Dr. <%= dentist.get("dentistName") %>

            -
            <%= dentist.get("specialization") %>

            (
            <%= dentist.get("availableFrom") %>
            -
            <%= dentist.get("availableTo") %>
            )

        </option>


        <%
                }
            }
        %>


    </select>


    <%
        if (availableDentists == null
                || availableDentists.isEmpty()) {
    %>

    <p style="color:#b00020;">

        No dentists are available at the
        requested date and time.

    </p>

    <%
        }
    %>



    <label>
        Treatment
    </label>


    <select
        name="treatmentId"
        required>


        <option value="">
            Select Treatment
        </option>


        <%
            if (treatments != null) {

                for (Map<String, Object> treatment
                        : treatments) {
        %>


        <option
            value="<%= treatment.get("treatmentId") %>">

            <%= treatment.get("treatmentName") %>

            -
            Rs. <%= treatment.get("treatmentCost") %>

        </option>


        <%
                }
            }
        %>


    </select>


    <button
        class="assign-btn"
        type="submit"

        <%
            if (availableDentists == null
                    || availableDentists.isEmpty()) {
        %>

            disabled

        <%
            }
        %>
    >

        Assign Appointment

    </button>


</form>


</div>


<%
    }
%>


</div>


</body>

</html>