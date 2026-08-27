<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String username =
            (String) session.getAttribute("username");

    String firstName =
            (String) session.getAttribute("first_name");

    String role =
            (String) session.getAttribute("role");

    if (username == null
            || !"Dentist".equalsIgnoreCase(role)) {

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

    <title>
        Dentist Dashboard
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

            margin-bottom: 30px;

            box-shadow:
                0 5px 18px rgba(0,0,0,0.08);
        }

        .welcome h1 {
            margin-top: 0;
            margin-bottom: 10px;
        }

        .welcome p {
            color: #666;
        }

        .section-title {
            margin-bottom: 20px;
        }

        .menu-grid {
            display: grid;

            grid-template-columns:
                repeat(auto-fit, minmax(240px, 1fr));

            gap: 20px;
        }

        .card {
            background: white;

            padding: 25px;

            border-radius: 12px;

            text-decoration: none;

            color: #222;

            box-shadow:
                0 5px 18px rgba(0,0,0,0.08);

            transition: 0.2s;
        }

        .card:hover {
            transform: translateY(-4px);

            box-shadow:
                0 8px 22px rgba(0,0,0,0.12);
        }

        .card h3 {
            margin-top: 0;
            color: #1f6feb;
        }

        .card p {
            color: #666;
            line-height: 1.5;
            margin-bottom: 0;
        }

    </style>

</head>

<body>


<div class="navbar">

    <div class="navbar-title">
        Sunrise Dental Clinic | Dentist
    </div>

    <a class="logout"
       href="${pageContext.request.contextPath}/LogoutServlet">

        Logout

    </a>

</div>


<div class="container">


    <div class="welcome">

        <h1>
            Welcome, Dr. <%= firstName %>
        </h1>

        <p>
            Dentist Dashboard
        </p>

        <p>
            View and manage appointments
            assigned to you.
        </p>

    </div>


    <h2 class="section-title">
        Dentist Functions
    </h2>


    <div class="menu-grid">


        <a href="${pageContext.request.contextPath}/DentistAppointmentsServlet"
           class="card">

            <h3>
                Pending Appointments
            </h3>

            <p>
                View assigned appointment requests
                and confirm or reject them.
            </p>

        </a>


    </div>


</div>


</body>

</html>