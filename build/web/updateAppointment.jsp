<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String username =
            (String) session.getAttribute("username");

    if (username == null) {

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

        .search-form {
            display: flex;
            gap: 10px;
            margin-bottom: 25px;
        }

        input,
        select {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 7px;
        }

        .search-form input {
            flex: 1;
        }

        button {
            padding: 12px 20px;
            border: none;
            background: #1f6feb;
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

</head>

<body>

<div class="navbar">

    <strong>
        Sunrise Dental Clinic
    </strong>

    <a href="dashboard.jsp">
        Dashboard
    </a>

</div>


<div class="container">

<div class="box">

    <h1>
        Update Appointment
    </h1>


    <form
        class="search-form"
        action="${pageContext.request.contextPath}/UpdateAppointmentServlet"
        method="get">

        <input
            type="text"
            name="appointmentNumber"
            placeholder="Enter appointment number"
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


                <option value="1"
                    <%= dentistId != null && dentistId == 1
                        ? "selected" : "" %>>

                    Dr. Nimal Fernando

                </option>


                <option value="2"
                    <%= dentistId != null && dentistId == 2
                        ? "selected" : "" %>>

                    Dr. Sarah Silva

                </option>


                <option value="3"
                    <%= dentistId != null && dentistId == 3
                        ? "selected" : "" %>>

                    Dr. Kamal Perera

                </option>


            </select>

        </div>


        <div class="form-group">

            <label>
                Treatment
            </label>

            <select
                name="treatmentId"
                required>


                <option value="1"
                    <%= treatmentId != null && treatmentId == 1
                        ? "selected" : "" %>>

                    Dental Consultation

                </option>


                <option value="2"
                    <%= treatmentId != null && treatmentId == 2
                        ? "selected" : "" %>>

                    Teeth Cleaning

                </option>


                <option value="3"
                    <%= treatmentId != null && treatmentId == 3
                        ? "selected" : "" %>>

                    Tooth Filling

                </option>


                <option value="4"
                    <%= treatmentId != null && treatmentId == 4
                        ? "selected" : "" %>>

                    Tooth Extraction

                </option>


                <option value="5"
                    <%= treatmentId != null && treatmentId == 5
                        ? "selected" : "" %>>

                    Root Canal Treatment

                </option>


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
                name="status">

                <option
                    <%= "Scheduled".equals(
                            request.getAttribute("status"))
                            ? "selected" : "" %>>

                    Scheduled

                </option>

                <option
                    <%= "Completed".equals(
                            request.getAttribute("status"))
                            ? "selected" : "" %>>

                    Completed

                </option>

                <option
                    <%= "Cancelled".equals(
                            request.getAttribute("status"))
                            ? "selected" : "" %>>

                    Cancelled

                </option>

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

</div>

</body>

</html>