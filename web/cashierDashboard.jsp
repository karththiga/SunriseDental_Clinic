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
            background: #21a7a0;
            color: white;
            padding: 18px 40px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logout {
            background: white;
            color: #176b87;
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

        .section-title { margin-top: 30px; }
        .menu-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 20px; }
        .card { padding: 25px; color: #222; background: white; border-radius: 12px; text-decoration: none; }
        .card h3 { margin: 0 0 9px; }
        .card p { margin: 0; }

    </style>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
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
            Search patient appointments and prepare printable bills.
        </p>

    </div>

    <h2 class="section-title">Cashier Services</h2>
    <div class="menu-grid">
        <a class="card" href="${pageContext.request.contextPath}/BillServlet"><h3>Payments and Receipts</h3><p>Search appointments, collect outstanding payments and print paid online or counter receipts.</p></a>
        <a class="card" href="help.jsp"><h3>Help</h3><p>View step-by-step instructions for clinic workflows and safe logout.</p></a>
    </div>

</div>

</body>
</html>
