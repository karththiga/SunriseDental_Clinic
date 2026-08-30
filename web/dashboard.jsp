<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String username = (String) session.getAttribute("username");
    String firstName = (String) session.getAttribute("first_name");
    String lastName = (String) session.getAttribute("last_name");
    String role = (String) session.getAttribute("role");

    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Dashboard - Sunrise Dental Clinic</title>

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
            font-size: 24px;
            font-weight: bold;
        }

        .navbar-user {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .logout-btn {
            background: white;
            color: #176b87;
            text-decoration: none;
            padding: 9px 18px;
            border-radius: 6px;
            font-weight: bold;
        }

        .container {
            max-width: 1200px;
            margin: 40px auto;
            padding: 0 25px;
        }

        .welcome-box {
            background: white;
            padding: 30px;
            border-radius: 12px;
            margin-bottom: 30px;
            box-shadow: 0 5px 18px rgba(0,0,0,0.08);
        }

        .welcome-box h1 {
            margin-top: 0;
            margin-bottom: 10px;
        }

        .welcome-box p {
            margin: 5px 0;
            color: #666;
        }

        .section-title {
            margin-bottom: 20px;
        }

        .menu-grid {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
        }

        .menu-card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            text-decoration: none;
            color: #222;
            box-shadow: 0 5px 18px rgba(0,0,0,0.08);
            transition: 0.2s;
        }

        .menu-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 22px rgba(0,0,0,0.12);
        }

        .menu-card h3 {
            margin-top: 0;
            color: #176b87;
        }

        .menu-card p {
            margin-bottom: 0;
            color: #666;
            line-height: 1.5;
        }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>

<body>

<div class="navbar">

    <div class="navbar-title">
        Sunrise Dental Clinic
    </div>

    <div class="navbar-user">

        <span>
            <%= firstName %> <%= lastName %>
        </span>

        <a class="notification-link" href="${pageContext.request.contextPath}/NotificationServlet">Notifications</a>

        <a class="logout-btn"
           href="${pageContext.request.contextPath}/LogoutServlet">
            Logout
        </a>

    </div>

</div>

<div class="container">

    <div class="welcome-box">

        <h1>
            Welcome, <%= firstName %>!
        </h1>

        <p>
            Logged in as:
            <strong><%= username %></strong>
        </p>

        <p>
            Role:
            <strong><%= role %></strong>
        </p>

    </div>

    <h2 class="section-title">
        Clinic Management
    </h2>

    <div class="menu-grid">

        <a href="${pageContext.request.contextPath}/ManageAppointmentsServlet"
           class="menu-card">

            <h3>
                Manage Appointments
            </h3>

            <p>
                Search, reserve, review and update registered appointments.
            </p>

        </a>

        <a href="${pageContext.request.contextPath}/ReportsServlet"
           class="menu-card">

            <h3>
                Reports
            </h3>

            <p>
                View clinic reports and appointment summaries.
            </p>

        </a>

        <a href="help.jsp"
           class="menu-card">

            <h3>
                Help
            </h3>

            <p>
                View instructions for using the system.
            </p>

        </a>

    </div>

</div>

</body>
</html>
