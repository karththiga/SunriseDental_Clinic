<%@page contentType="text/html" pageEncoding="UTF-8"%>

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

    Boolean found =
            (Boolean) request.getAttribute("found");
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Search Appointment - Sunrise Dental Clinic
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

        .navbar-title {
            font-size: 23px;
            font-weight: bold;
        }

        .dashboard-link {
            color: white;
            text-decoration: none;
            font-weight: bold;
        }

        .container {
            max-width: 850px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .search-box {
            background: white;
            padding: 35px;
            border-radius: 12px;

            box-shadow:
                0 5px 20px rgba(0,0,0,0.08);
        }

        h1 {
            margin-top: 0;
            text-align: center;
        }

        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }

        .search-form {
            display: flex;
            gap: 10px;
        }

        .search-form input {
            flex: 1;
            padding: 13px;

            border:
                1px solid #ccc;

            border-radius: 7px;
            font-size: 16px;
        }

        .search-form input:focus {
            outline: none;
            border-color: #176b87;
        }

        .search-form button {
            padding: 13px 25px;

            border: none;
            border-radius: 7px;

            background: #21a7a0;
            color: white;

            font-size: 16px;
            cursor: pointer;
        }

        .message {
            padding: 13px;
            margin-top: 20px;
            border-radius: 7px;
            text-align: center;
        }

        .error {
            background: #ffe5e5;
            color: #b00020;
        }

        .result-box {
            background: white;

            margin-top: 25px;
            padding: 30px;

            border-radius: 12px;

            box-shadow:
                0 5px 20px rgba(0,0,0,0.08);
        }

        .result-box h2 {
            margin-top: 0;
            color: #176b87;
            border-bottom: 1px solid #ddd;
            padding-bottom: 15px;
        }

        .detail-row {
            display: flex;

            padding: 12px 0;

            border-bottom:
                1px solid #eee;
        }

        .detail-label {
            width: 220px;
            font-weight: bold;
        }

        .detail-value {
            flex: 1;
            color: #555;
        }

        .status {
            display: inline-block;

            padding: 6px 12px;

            border-radius: 20px;

            background: #e9f8f5;
            color: #176b87;

            font-weight: bold;
        }

        .back-link {
            display: block;

            margin-top: 25px;

            text-align: center;

            text-decoration: none;
            color: #176b87;

            font-weight: bold;
        }

        @media(max-width: 600px) {

            .search-form {
                flex-direction: column;
            }

            .detail-row {
                flex-direction: column;
            }

            .detail-label {
                width: 100%;
                margin-bottom: 5px;
            }
        }

    </style>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>

<body>

<div class="navbar">

    <div class="navbar-title">
        Sunrise Dental Clinic | Admin
    </div>

    <a href="<%= "Admin".equalsIgnoreCase(role) ? "adminDashboard.jsp" : "dashboard.jsp" %>"
       class="dashboard-link">

        Dashboard

    </a>

</div>


<div class="container">

    <div class="search-box">

        <h1>
            Search Appointment
        </h1>

        <p class="subtitle">
            Enter the appointment number
            to view complete details.
        </p>

        <form
            class="search-form"
            action="${pageContext.request.contextPath}/SearchAppointmentServlet"
            method="get">

            <input
                type="text"
                name="appointmentNumber"
                placeholder="Example: APT001"
                value="<%= request.getParameter("appointmentNumber") == null
                        ? "" : request.getParameter("appointmentNumber") %>"
                required>

            <button type="submit">
                Search
            </button>

        </form>

        <%
            if (error != null) {
        %>

        <div class="message error">
            <%= error %>
        </div>

        <%
            }
        %>

    </div>


    <%
        if (Boolean.TRUE.equals(found)) {
    %>

    <div class="result-box">

        <h2>
            Appointment Details
        </h2>

        <div class="detail-row">

            <div class="detail-label">
                Appointment Number
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "appointmentNumber"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Patient Name
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "patientName"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Address
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "address"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Contact Number
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "contactNumber"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Dentist
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "dentistName"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Treatment
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "treatmentName"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Treatment Cost
            </div>

            <div class="detail-value">

                Rs.
                <%= request.getAttribute(
                        "treatmentCost"
                ) %>

            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Appointment Date
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "appointmentDate"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Appointment Time
            </div>

            <div class="detail-value">
                <%= request.getAttribute(
                        "appointmentTime"
                ) %>
            </div>

        </div>


        <div class="detail-row">

            <div class="detail-label">
                Status
            </div>

            <div class="detail-value">

                <span class="status">

                    <%= request.getAttribute(
                            "status"
                    ) %>

                </span>

            </div>

        </div>

        <a class="button"
           href="${pageContext.request.contextPath}/UpdateAppointmentServlet?appointmentNumber=<%= request.getAttribute("appointmentNumber") %>">
            Update this appointment
        </a>

    </div>

    <%
        }
    %>


    <a href="<%= "Admin".equalsIgnoreCase(role) ? "adminDashboard.jsp" : "dashboard.jsp" %>"
       class="back-link">

        ← Back to Dashboard

    </a>

</div>

</body>

</html>
