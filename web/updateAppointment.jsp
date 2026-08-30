<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%
    String username =
            (String) session.getAttribute("username");

    String role =
            (String) session.getAttribute("role");

    if (username == null
            || !("Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role))) {

        response.sendRedirect("login.jsp");
        return;
    }

    String error =
            (String) request.getAttribute("error");

    String success =
            (String) request.getAttribute("success");

    Boolean found =
            (Boolean) request.getAttribute("found");

    Integer dentistId =
            (Integer) request.getAttribute("dentistId");

    Integer treatmentId =
            (Integer) request.getAttribute("treatmentId");

    Boolean canUndo =
            (Boolean) request.getAttribute("canUndo");

    List<Map<String, Object>> dentists =
            (List<Map<String, Object>>) request.getAttribute("dentists");

    List<Map<String, Object>> treatments =
            (List<Map<String, Object>>) request.getAttribute("treatments");
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Update Appointment
    </title>

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fb;
        }

        .navbar {
            background: #21a7a0;
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
            max-width: 800px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .box {
            background: white;
            padding: 35px;
            border-radius: 12px;
            box-shadow:
                0 5px 20px rgba(0,0,0,0.08);
        }

        h1 {
            text-align: center;
        }

        input,
        select {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 7px;
        }

        button {
            padding: 12px 20px;
            border: none;
            background: #21a7a0;
            color: white;
            border-radius: 7px;
            cursor: pointer;
        }

        .form-group {
            margin-bottom: 18px;
        }

        label {
            display: block;
            font-weight: bold;
            margin-bottom: 7px;
        }

        .readonly {
            background: #eee;
        }

        .update-btn {
            width: 100%;
        }

        .undo-btn {
            width: 100%;
            margin-top: 12px;
            background: #555;
        }

        .message {
            margin-bottom: 20px;
            padding: 12px;
            text-align: center;
            border-radius: 7px;
        }

        .error {
            background: #ffe5e5;
            color: #b00020;
        }

        .success {
            background: #e5f7e8;
            color: #176b2c;
        }

    </style>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>

<body>

<div class="navbar">

    <div class="navbar-title">
        Sunrise Dental Clinic | Admin
    </div>

    <a href="${pageContext.request.contextPath}/ManageAppointmentsServlet">
        Manage Appointments
    </a>

</div>


<div class="container">

<div class="box">

    <h1>
        Update Appointment
    </h1>


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


    <%
        if (Boolean.TRUE.equals(found)) {
    %>


    <form
        action="${pageContext.request.contextPath}/UpdateAppointmentServlet"
        method="post">


        <div class="form-group">

            <label>
                Appointment Number
            </label>

            <input
                class="readonly"
                type="text"
                name="appointmentNumber"
                value="<%= request.getAttribute("appointmentNumber") %>"
                readonly>

        </div>


        <div class="form-group">

            <label>
                Patient Name
            </label>

            <input
                class="readonly"
                type="text"
                value="<%= request.getAttribute("patientName") %>"
                readonly>

        </div>


        <div class="form-group">

            <label>
                Contact Number
            </label>

            <input
                class="readonly"
                type="text"
                value="<%= request.getAttribute("contactNumber") %>"
                readonly>

        </div>


        <div class="form-group">

            <label>
                Dentist
            </label>

            <select
                name="dentistId"
                required>


                <%
                    if (dentists != null) {
                        for (Map<String, Object> dentist : dentists) {
                            Integer optionId =
                                    (Integer) dentist.get("dentistId");
                %>
                    <option value="<%= optionId %>"
                        <%= optionId.equals(dentistId) ? "selected" : "" %>>
                        <%= dentist.get("dentistName") %>
                        — <%= dentist.get("availableDay") %>
                    </option>
                <%
                        }
                    }
                %>


            </select>

        </div>


        <div class="form-group">

            <label>
                Treatment
            </label>

            <select
                name="treatmentId"
                required>


                <%
                    if (treatments != null) {
                        for (Map<String, Object> treatment : treatments) {
                            Integer optionId =
                                    (Integer) treatment.get("treatmentId");
                %>
                    <option value="<%= optionId %>"
                        <%= optionId.equals(treatmentId) ? "selected" : "" %>>
                        <%= treatment.get("treatmentName") %>
                    </option>
                <%
                        }
                    }
                %>


            </select>

        </div>


        <div class="form-group">

            <label>
                Appointment Date
            </label>

            <input
                type="date"
                name="appointmentDate"
                value="<%= request.getAttribute("appointmentDate") %>"
                required>

        </div>


        <div class="form-group">

            <label>
                Appointment Time
            </label>

            <input
                type="time"
                name="appointmentTime"
                value="<%= request.getAttribute("appointmentTime") %>"
                required>

        </div>


        <div class="form-group">

            <label>
                Status
            </label>

            <select
                name="status"
                required>

                <%
                    String[] statuses = {
                        "Pending", "Confirmed", "Scheduled",
                        "Completed", "Cancelled", "Rejected"
                    };
                    for (String statusOption : statuses) {
                %>
                    <option value="<%= statusOption %>"
                        <%= statusOption.equals(request.getAttribute("status"))
                                ? "selected" : "" %>>
                        <%= statusOption %>
                    </option>
                <%
                    }
                %>

            </select>

        </div>


        <button
            class="update-btn"
            type="submit">

            Update Appointment

        </button>

    </form>


    <%
        if (Boolean.TRUE.equals(canUndo)) {
    %>

        <form
            action="${pageContext.request.contextPath}/UpdateAppointmentServlet"
            method="get">

            <input
                type="hidden"
                name="action"
                value="undo">

            <button
                class="undo-btn"
                type="submit">

                Undo Last Update

            </button>

        </form>

    <%
        }
    %>


    <%
        }
    %>


</div>

<a href="${pageContext.request.contextPath}/ManageAppointmentsServlet"
   class="back-link">
    ← Back to Manage Appointments
</a>

</div>

</body>

</html>
