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

    Integer selectedTreatmentId =
            (Integer) request.getAttribute(
                    "selectedTreatmentId"
            );


    List<Map<String, Object>> treatments =
            (List<Map<String, Object>>)
            request.getAttribute("treatments");


    List<Map<String, Object>> dentists =
            (List<Map<String, Object>>)
            request.getAttribute("dentists");
%>


<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Request Appointment</title>


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

            max-width: 800px;

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

            margin-top: 18px;

            margin-bottom: 7px;

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


        .load-btn {

            width: 100%;

            margin-top: 15px;

            padding: 11px;

            border: none;

            border-radius: 7px;

            background: #555;

            color: white;

            cursor: pointer;

            font-size: 15px;
        }


        .submit-btn {

            width: 100%;

            margin-top: 25px;

            padding: 13px;

            border: none;

            border-radius: 7px;

            background: #1f6feb;

            color: white;

            cursor: pointer;

            font-size: 16px;
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

            margin-top: 25px;

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

        Select treatment, dentist,
        preferred date and time.

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



    <!-- FIRST SELECT TREATMENT -->

    <form
        action="${pageContext.request.contextPath}/PatientRequestAppointmentServlet"
        method="get">


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

                        Integer treatmentId =
                                (Integer)
                                treatment.get(
                                        "treatmentId"
                                );
            %>


            <option
                value="<%= treatmentId %>"

                <%
                    if (selectedTreatmentId != null
                            && selectedTreatmentId.equals(
                                    treatmentId)) {
                %>

                    selected

                <%
                    }
                %>
            >

                <%= treatment.get(
                        "treatmentName") %>

                -

                Rs.

                <%= treatment.get(
                        "treatmentCost") %>


            </option>


            <%
                    }
                }
            %>


        </select>


        <button
            type="submit"
            class="load-btn">

            Show Dentists

        </button>


    </form>



    <%
        if (selectedTreatmentId != null) {
    %>


    <form
        action="${pageContext.request.contextPath}/PatientRequestAppointmentServlet"
        method="post">


        <input
            type="hidden"
            name="treatmentId"
            value="<%= selectedTreatmentId %>">


        <label>

            Dentist

        </label>


        <select
            name="dentistId"
            required>


            <option value="">

                Select Dentist

            </option>


            <%
                if (dentists != null
                        && !dentists.isEmpty()) {

                    for (Map<String, Object> dentist
                            : dentists) {
            %>


            <option
                value="<%= dentist.get(
                        "dentistId") %>">


                <%= dentist.get(
                        "dentistName") %>


                <%
                    Object specialization =
                            dentist.get(
                                    "specialization"
                            );

                    if (specialization != null
                            && !specialization
                            .toString()
                            .trim()
                            .isEmpty()) {
                %>


                    -

                    <%= specialization %>


                <%
                    }
                %>


            </option>


            <%
                    }

                } else {
            %>


            <option
                value=""
                disabled>

                No dentists provide this treatment

            </option>


            <%
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



        <%
            if (dentists != null
                    && !dentists.isEmpty()) {
        %>


        <button
            type="submit"
            class="submit-btn">

            Submit Appointment Request

        </button>


        <%
            }
        %>


    </form>


    <%
        }
    %>



    <div class="note">

        Select a dentist who provides
        your required treatment.

        <br><br>

        The administrator will verify
        the dentist's availability for
        your requested date and time.

        <br><br>

        Your appointment will initially
        remain

        <strong>Pending</strong>.

    </div>


</div>


<a
    href="patientDashboard.jsp"
    class="back-link">

    ← Back to Patient Dashboard

</a>


</div>


</body>

</html>