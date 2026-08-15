<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String username =
            (String) session.getAttribute("username");

    String firstName =
            (String) session.getAttribute("first_name");

    if (username == null) {

        response.sendRedirect("login.jsp");

        return;
    }
%>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>
        Dashboard - Sunrise Dental Clinic
    </title>

</head>

<body>

    <h1>
        Sunrise Dental Clinic
    </h1>

    <h2>
        Login Successful
    </h2>

    <p>
        Welcome,
        <strong><%= firstName %></strong>
    </p>

    <p>
        Email:
        <%= username %>
    </p>

</body>
</html>