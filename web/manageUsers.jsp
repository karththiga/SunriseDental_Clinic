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


    List<Map<String, Object>> users =
            (List<Map<String, Object>>)
            request.getAttribute("users");
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Manage Users - Sunrise Dental Clinic
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


        h1,
        h2 {
            margin-top: 0;
        }


        .form-grid {
            display: grid;

            grid-template-columns:
                repeat(2, 1fr);

            gap: 18px;
        }


        .form-group {
            margin-bottom: 5px;
        }


        label {
            display: block;
            margin-bottom: 7px;
            font-weight: bold;
        }


        input,
        select {
            width: 100%;

            padding: 12px;

            border: 1px solid #ccc;
            border-radius: 7px;

            font-size: 15px;
        }


        .button {
            width: 100%;

            padding: 13px;

            margin-top: 20px;

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

            border-bottom:
                1px solid #ddd;
        }


        tr:hover {
            background: #f7f9fc;
        }


        .role {
            font-weight: bold;
        }


        .back-link {
            display: block;

            text-align: center;

            margin-top: 25px;

            text-decoration: none;

            color: #176b87;

            font-weight: bold;
        }


        @media(max-width: 700px) {

            .form-grid {
                grid-template-columns: 1fr;
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

    <a href="adminDashboard.jsp">
        Dashboard
    </a>

</div>


<div class="container">


    <div class="box">


        <h1>
            Add New User
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
            action="${pageContext.request.contextPath}/ManageUsersServlet"
            method="post">


            <div class="form-grid">


                <div class="form-group">

                    <label>
                        First Name
                    </label>

                    <input
                        type="text"
                        name="firstName"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Last Name
                    </label>

                    <input
                        type="text"
                        name="lastName"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Email Address
                    </label>

                    <input
                        type="email"
                        name="username"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Phone Number
                    </label>

                    <input
                        type="text"
                        name="phoneNumber"
                        placeholder="0771234567"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Password
                    </label>

                    <input
                        type="password"
                        name="password"
                        minlength="6"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Role
                    </label>

                    <select
                        name="role"
                        required>

                        <option value="">
                            Select Role
                        </option>

                        <option value="Admin">
                            Admin
                        </option>

                        <option value="Dentist">
                            Dentist
                        </option>

                        <option value="Cashier">
                            Cashier
                        </option>

                        <option value="Patient">
                            Patient
                        </option>

                    </select>

                </div>


            </div>


            <button
                type="submit"
                class="button">

                Create User

            </button>


        </form>


    </div>



    <div class="box">


        <h2>
            Registered Users
        </h2>


        <div class="table-container">


            <table>


                <thead>

                    <tr>

                        <th>ID</th>

                        <th>Name</th>

                        <th>Email</th>

                        <th>Phone</th>

                        <th>Role</th>

                    </tr>

                </thead>


                <tbody>


                <%
                    if (users != null
                            && !users.isEmpty()) {

                        for (Map<String, Object> user
                                : users) {
                %>


                <tr>

                    <td>
                        <%= user.get("userId") %>
                    </td>

                    <td>
                        <%= user.get("firstName") %>
                        <%= user.get("lastName") %>
                    </td>

                    <td>
                        <%= user.get("username") %>
                    </td>

                    <td>
                        <%= user.get("phoneNumber") %>
                    </td>

                    <td class="role">
                        <%= user.get("role") %>
                    </td>

                </tr>


                <%
                        }

                    } else {
                %>


                <tr>

                    <td colspan="5"
                        style="text-align:center;">

                        No users found.

                    </td>

                </tr>


                <%
                    }
                %>


                </tbody>

            </table>


        </div>


    </div>


    <a href="adminDashboard.jsp"
       class="back-link">

        ← Back to Admin Dashboard

    </a>


</div>


</body>

</html>
