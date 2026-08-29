<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%
    String username =
            (String) session.getAttribute("username");

    String role =
            (String) session.getAttribute("role");

    if (username == null
            || !"Dentist".equalsIgnoreCase(role)) {

        response.sendRedirect("login.jsp");
        return;
    }

    String error =
            (String) request.getAttribute("error");

    String success =
            (String) request.getAttribute("success");

    List<Map<String, Object>> appointments =
            (List<Map<String, Object>>)
            request.getAttribute("appointments");
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Upcoming Appointments - Sunrise Dental Clinic
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
            align-items: center;
        }

        .navbar-title {
            font-size: 23px;
            font-weight: bold;
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

        .subtitle {
            color: #666;
            margin-bottom: 25px;
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

        tr:hover {
            background: #f7f9fc;
        }

        .pending {
            color: #a96700;
            font-weight: bold;
        }

        .confirmed {
            color: #176b2c;
            font-weight: bold;
        }

        .action-form {
            display: inline-block;
            margin-right: 5px;
        }

        .confirm-btn {
            background: #1f6feb;
            color: white;

            border: none;
            border-radius: 6px;

            padding: 8px 14px;

            cursor: pointer;
        }

        .reject-btn {
            background: #b00020;
            color: white;

            border: none;
            border-radius: 6px;

            padding: 8px 14px;

            cursor: pointer;
        }

        .no-data {
            text-align: center;
            color: #666;
            padding: 30px;
        }

        .back-link {
            display: block;
            margin-top: 25px;
            text-align: center;

            text-decoration: none;
            color: #1f6feb;
            font-weight: bold;
        }

    </style>

</head>

<body>

<div class="navbar">

    <div class="navbar-title">
        Sunrise Dental Clinic | Dentist
    </div>

    <a href="dentistDashboard.jsp">
        Dashboard
    </a>

</div>


<div class="container">

<div class="box">

    <h1>
        Upcoming Appointments
    </h1>

    <p class="subtitle">
        View appointments reserved with you. Legacy pending requests
        can still be confirmed or rejected.
    </p>


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


    <div class="table-container">

        <table>

            <thead>

                <tr>

                    <th>
                        Appointment No.
                    </th>

                    <th>
                        Patient
                    </th>

                    <th>
                        Contact
                    </th>

                    <th>
                        Treatment
                    </th>

                    <th>
                        Date
                    </th>

                    <th>
                        Time
                    </th>

                    <th>
                        Status
                    </th>

                    <th>
                        Action
                    </th>

                </tr>

            </thead>


            <tbody>

            <%
                if (appointments != null
                        && !appointments.isEmpty()) {

                    for (Map<String, Object> row
                            : appointments) {
            %>


            <tr>

                <td>
                    <%= row.get("appointmentNumber") %>
                </td>

                <td>
                    <%= row.get("patientName") %>
                </td>

                <td>
                    <%= row.get("contactNumber") %>
                </td>

                <td>
                    <%= row.get("treatmentName") %>
                </td>

                <td>
                    <%= row.get("appointmentDate") %>
                </td>

                <td>
                    <%= row.get("appointmentTime") %>
                </td>

                <td class="<%= "Confirmed".equalsIgnoreCase(
                        (String) row.get("status"))
                        ? "confirmed" : "pending" %>">
                    <%= row.get("status") %>
                </td>

                <td>

                    <%
                        if ("Pending".equalsIgnoreCase(
                                (String) row.get("status"))) {
                    %>

                    <form
                        class="action-form"
                        action="${pageContext.request.contextPath}/DentistAppointmentsServlet"
                        method="post">

                        <input
                            type="hidden"
                            name="appointmentId"
                            value="<%= row.get("appointmentId") %>">

                        <input
                            type="hidden"
                            name="action"
                            value="confirm">

                        <button
                            type="submit"
                            class="confirm-btn">

                            Confirm

                        </button>

                    </form>


                    <form
                        class="action-form"
                        action="${pageContext.request.contextPath}/DentistAppointmentsServlet"
                        method="post">

                        <input
                            type="hidden"
                            name="appointmentId"
                            value="<%= row.get("appointmentId") %>">

                        <input
                            type="hidden"
                            name="action"
                            value="reject">

                        <button
                            type="submit"
                            class="reject-btn">

                            Reject

                        </button>

                    </form>

                    <%
                        } else {
                    %>

                        Reserved online

                    <%
                        }
                    %>

                </td>

            </tr>


            <%
                    }

                } else {
            %>


            <tr>

                <td colspan="8"
                    class="no-data">

                    No upcoming appointments.

                </td>

            </tr>


            <%
                }
            %>

            </tbody>

        </table>

    </div>

</div>


<a href="dentistDashboard.jsp"
   class="back-link">

    ← Back to Dentist Dashboard

</a>

</div>

</body>

</html>
