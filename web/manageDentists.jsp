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

    <title>
        Manage Dentists - Sunrise Dental Clinic
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

        .navbar a {
            color: white;
            text-decoration: none;
            font-weight: bold;
        }

        .container {
            max-width: 1000px;
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

        h1,
        h2 {
            margin-top: 0;
        }

        label {
            display: block;

            margin-bottom: 8px;

            font-weight: bold;
        }

        input {
            width: 100%;

            padding: 12px;

            border: 1px solid #ccc;
            border-radius: 7px;

            font-size: 15px;
        }

        .button {
            width: 100%;

            margin-top: 18px;

            padding: 13px;

            background: #21a7a0;
            color: white;

            border: none;
            border-radius: 7px;

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

        table {
            width: 100%;

            border-collapse: collapse;
        }

        th {
            background: #21a7a0;
            color: white;

            padding: 13px;

            text-align: left;
        }

        td {
            padding: 13px;

            border-bottom:
                1px solid #ddd;
        }

        tr:hover {
            background: #f7f9fc;
        }

        .back-link {
            display: block;

            margin-top: 25px;

            text-align: center;

            color: #176b87;

            text-decoration: none;

            font-weight: bold;
        }

    </style>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>


<body>


<div class="navbar">

    <div class="navbar-title">
        Sunrise Dental Clinic | Admin
    </div>

    <a href="adminDashboard.jsp">
        Dashboard
    </a>

</div>


<div class="container">


    <div class="box">

        <h1>
            Add Dentist
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


        <form
            action="${pageContext.request.contextPath}/ManageDentistsServlet"
            method="post">


            <label>
                Dentist Name
            </label>


            <input
                type="text"
                name="dentistName"
                placeholder="Example: Dr. Nimal Fernando"
                required>


            <button
                type="submit"
                class="button">

                Add Dentist

            </button>


        </form>

    </div>



    <div class="box">

        <h2>
            Registered Dentists
        </h2>


        <table>

            <thead>

                <tr>

                    <th>
                        Dentist ID
                    </th>

                    <th>
                        Dentist Name
                    </th>

                </tr>

            </thead>


            <tbody>


            <%
                if (dentists != null
                        && !dentists.isEmpty()) {

                    for (Map<String, Object> dentist
                            : dentists) {
            %>


            <tr>

                <td>
                    <%= dentist.get("dentistId") %>
                </td>

                <td>
                    <%= dentist.get("dentistName") %>
                </td>

            </tr>


            <%
                    }

                } else {
            %>


            <tr>

                <td colspan="2"
                    style="text-align:center;">

                    No dentists found.

                </td>

            </tr>


            <%
                }
            %>


            </tbody>

        </table>

    </div>


    <a href="adminDashboard.jsp"
       class="back-link">

        ← Back to Admin Dashboard

    </a>


</div>


</body>

</html>
