<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String username =
            (String) session.getAttribute("username");

    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String error =
            request.getParameter("error");

    String success =
            request.getParameter("success");
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>
        Register Appointment - Sunrise Dental Clinic
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

        .container {
            max-width: 750px;
            margin: 40px auto;
            background: white;
            padding: 35px;
            border-radius: 12px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
        }

        h1 {
            text-align: center;
        }

        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }

        .form-group {
            margin-bottom: 18px;
        }

        label {
            display: block;
            margin-bottom: 7px;
            font-weight: bold;
        }

        input,
        select,
        textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 7px;
            font-size: 15px;
        }

        textarea {
            resize: vertical;
        }

        .row {
            display: flex;
            gap: 15px;
        }

        .row .form-group {
            width: 50%;
        }

        button {
            width: 100%;
            padding: 13px;
            border: none;
            border-radius: 7px;
            background: #21a7a0;
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

        .back-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            text-decoration: none;
            color: #176b87;
        }

    </style>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>

<body>

<div class="container">

    <h1>
        Register New Appointment
    </h1>

    <p class="subtitle">
        Sunrise Dental Clinic
    </p>

    <%
        if (error != null) {
    %>

    <div class="message error">
        <%= error %>
    </div>

    <%
        }

        if ("true".equals(success)) {
    %>

    <div class="message success">
        Appointment registered successfully.
    </div>

    <%
        }
    %>

    <form
        action="${pageContext.request.contextPath}/AppointmentServlet"
        method="post">

        <div class="form-group">

            <label>
                Appointment Number
            </label>

            <input
                type="text"
                name="appointmentNumber"
                placeholder="Example: APT001"
                required>

        </div>

        <div class="form-group">

            <label>
                Patient Name
            </label>

            <input
                type="text"
                name="patientName"
                required>

        </div>

        <div class="form-group">

            <label>
                Address
            </label>

            <textarea
                name="address"
                rows="3"
                required></textarea>

        </div>

        <div class="form-group">

            <label>
                Contact Number
            </label>

            <input
                type="text"
                name="contactNumber"
                placeholder="0771234567"
                required>

        </div>

        <div class="row">

            <div class="form-group">

                <label>
                    Dentist
                </label>

                <select
                    name="dentistId"
                    required>

                    <option value="">
                        Select Dentist
                    </option>

                    <option value="1">
                        Dr. Nimal Fernando
                    </option>

                    <option value="2">
                        Dr. Sarah Silva
                    </option>

                    <option value="3">
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

                    <option value="">
                        Select Treatment
                    </option>

                    <option value="1">
                        Dental Consultation
                    </option>

                    <option value="2">
                        Teeth Cleaning
                    </option>

                    <option value="3">
                        Tooth Filling
                    </option>

                    <option value="4">
                        Tooth Extraction
                    </option>

                    <option value="5">
                        Root Canal Treatment
                    </option>

                </select>

            </div>

        </div>

        <div class="row">

            <div class="form-group">

                <label>
                    Appointment Date
                </label>

                <input
                    type="date"
                    name="appointmentDate"
                    required>

            </div>

            <div class="form-group">

                <label>
                    Appointment Time
                </label>

                <input
                    type="time"
                    name="appointmentTime"
                    required>

            </div>

        </div>

        <button type="submit">
            Register Appointment
        </button>

    </form>

    <a href="dashboard.jsp"
       class="back-link">
        ← Back to Dashboard
    </a>

</div>

</body>
</html>
