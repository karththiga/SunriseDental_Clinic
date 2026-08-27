<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String username =
            (String) session.getAttribute("username");

    String firstName =
            (String) session.getAttribute("first_name");

    String role =
            (String) session.getAttribute("role");

    if (username == null
            || !"Cashier".equalsIgnoreCase(role)) {

        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>
        Cashier Dashboard
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
            align-items: center;
        }

        .logout {
            background: white;
            color: #1f6feb;
            padding: 9px 18px;
            border-radius: 6px;
            text-decoration: none;
            font-weight: bold;
        }

        .container {
            max-width: 1200px;
            margin: 40px auto;
            padding: 0 25px;
        }

        .welcome {
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 5px 18px rgba(0,0,0,0.08);
        }

    </style>

</head>

<body>

<div class="navbar">

    <h2>
        Sunrise Dental Clinic | Cashier
    </h2>

    <a class="logout"
       href="${pageContext.request.contextPath}/LogoutServlet">
        Logout
    </a>

</div>

<div class="container">

    <div class="welcome">

        <h1>
            Welcome, <%= firstName %>
        </h1>

        <p>
            Cashier Dashboard
        </p>

        <p>
            Cashier functions will be added later.
        </p>

    </div>

</div>

</body>
</html>