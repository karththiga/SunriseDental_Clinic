<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%
    String username =
            (String) session.getAttribute("username");

    String role =
            (String) session.getAttribute("role");

    if (username == null
            || !"Patient".equalsIgnoreCase(role)) {

        response.sendRedirect("login.jsp");
        return;
    }


    String error =
            (String) request.getAttribute("error");

    String success =
            (String) request.getAttribute("success");


    List<Map<String, Object>> treatments =
            (List<Map<String, Object>>)
            request.getAttribute("treatments");
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Request Appointment
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
            max-width: 750px;
            margin: 40px auto;
            padding: 0 25px;
        }

        .box {
            background: white;

            padding: 35px;

            border-radius: 12px;

            box-shadow:
                0 5px 18px rgba(0,0,0,0.08);
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

        label {
            display: block;
            margin-bottom: 7px;
            margin-top: 18px;
            font-weight: bold;
        }

        select,
        input {
            width: 100%;

            padding: 12px;

            border:
                1px solid #ccc;

            border-radius: 7px;

            font-size: 15px;
        }

        button {
            width: 100%;

            margin-top: 25px;

            padding: 13px;

            border: none;

            border-radius: 7px;

            background: #1f6feb;
            color: white;

            font-size: 16px;

            cursor: pointer;
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

        .note {
            margin-top: 20px;

            padding: 15px;

            background: #eef5ff;

            border-radius: 7px;

            color: #555;

            line-height: 1.5;
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
        Sunrise Dental Clinic | Patient
    </div>

    <a href="patientDashboard.jsp">
        Dashboard
    </a>

</div>


<div class="container">

<div class="box">


    <h1>
        Request Appointment
    </h1>

    <p class="subtitle">
        Select the treatment and your preferred
        appointment date and time.
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


    <form
        action="${pageContext.request.contextPath}/PatientRequestAppointmentServlet"
        method="post">


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


        <label>
            Preferred Date
        </label>


        <input
            type="date"
            name="appointmentDate"
            required>


        <label>
            Preferred Time
        </label>


        <input
            type="time"
            name="appointmentTime"
            required>


        <button type="submit">
            Submit Appointment Request
        </button>


    </form>


    <div class="note">

        Your request will initially be marked as
        <strong>Pending</strong>.

        <br><br>

        The administrator will check dentist availability
        and assign an appointment number and dentist.

        <br><br>

        The dentist will then confirm or reject
        the appointment.

    </div>


</div>


<a href="patientDashboard.jsp"
   class="back-link">

    ← Back to Patient Dashboard

</a>


</div>


</body>

</html>