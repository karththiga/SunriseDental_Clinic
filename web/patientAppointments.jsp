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

    List<Map<String, Object>> appointments =
            (List<Map<String, Object>>)
            request.getAttribute("appointments");

    List<Map<String, Object>> notifications =
            (List<Map<String, Object>>) request.getAttribute("notifications");
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        My Appointments - Sunrise Dental Clinic
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

            box-shadow:
                0 5px 18px rgba(0,0,0,0.08);
        }

        h1 {
            margin-top: 0;
        }

        .subtitle {
            color: #666;
            margin-bottom: 25px;
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
            border-bottom: 1px solid #ddd;
        }

        .status {
            font-weight: bold;
        }

        .pending {
            color: #a96700;
        }

        .confirmed {
            color: #176b2c;
        }

        .rejected {
            color: #b00020;
        }

        .cancelled { color: #9d2638; }
        .notifications { margin-bottom: 25px; }
        .notifications h2 { margin: 0 0 12px; }
        .notice { margin-bottom: 10px; padding: 14px 16px; background: #f4fbf9; border: 1px solid #cfe5e1; border-left: 4px solid #21a7a0; border-radius: 9px; }
        .notice.unread { background: #e8f8f2; }
        .notice-head { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 5px; }
        .notice-head strong { color: #123047; }
        .notice-head time { color: #607583; font-size: 12px; }
        .notice p { margin: 0; line-height: 1.5; }
        .refund-info { margin-top: 5px; color: #176454; font-size: 13px; font-weight: bold; }

        .no-data {
            text-align: center;
            color: #666;
            padding: 30px;
        }

        .back-link {
            display: block;

            margin-top: 25px;

            text-align: center;

            text-decoration: none;

            color: #176b87;

            font-weight: bold;
        }

    </style>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
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
        My Appointments
    </h1>

    <p class="subtitle">
        View your appointment details
        and current confirmation status.
    </p>


    <%
        if (error != null) {
    %>

        <div class="message error">
            <%= error %>
        </div>

    <%
        }
    %>


    <% if (notifications != null && !notifications.isEmpty()) { %>
    <section class="notifications" aria-label="Patient notifications">
        <h2>Clinic Notifications</h2>
        <% for (Map<String, Object> notice : notifications) { %>
        <article class="notice <%= Boolean.FALSE.equals(notice.get("read")) ? "unread" : "" %>">
            <div class="notice-head"><strong><%= notice.get("title") %></strong><time><%= notice.get("createdAt") %></time></div>
            <p><%= notice.get("message") %></p>
        </article>
        <% } %>
    </section>
    <% } %>


    <div class="table-container">

    <table>

        <thead>

            <tr>

                <th>
                    Appointment No.
                </th>

                <th>
                    Treatment
                </th>

                <th>
                    Dentist
                </th>

                <th>
                    Date
                </th>

                <th>
                    Time
                </th>

                <th>
                    Status
                </th>

                <th>
                    Confirmation
                </th>

            </tr>

        </thead>


        <tbody>


        <%
            if (appointments != null
                    && !appointments.isEmpty()) {

                for (Map<String, Object> row
                        : appointments) {

                    String status =
                            (String) row.get("status");

                    String statusClass =
                            "";

                    if ("Pending".equalsIgnoreCase(status)) {

                        statusClass = "pending";

                    } else if ("Confirmed".equalsIgnoreCase(status)) {

                        statusClass = "confirmed";

                    } else if ("Rejected".equalsIgnoreCase(status)) {

                        statusClass = "rejected";
                    } else if ("Cancelled".equalsIgnoreCase(status)) {

                        statusClass = "cancelled";
                    }
        %>


        <tr>

            <td>

                <%
                    if (row.get("appointmentNumber") == null) {
                %>

                    Not assigned yet

                <%
                    } else {
                %>

                    <%= row.get("appointmentNumber") %>

                <%
                    }
                %>

            </td>


            <td>

                <%
                    if (row.get("treatmentName") == null) {
                %>

                    Not selected

                <%
                    } else {
                %>

                    <%= row.get("treatmentName") %>

                <%
                    }
                %>

            </td>


            <td>

                <%
                    if (row.get("dentistName") == null) {
                %>

                    Not assigned yet

                <%
                    } else {
                %>

                    <%= row.get("dentistName") %>

                <%
                    }
                %>

            </td>


            <td>
                <%= row.get("appointmentDate") %>
            </td>


            <td>
                <%= row.get("appointmentTime") %>
            </td>


            <td>

                <span class="status <%= statusClass %>">

                    <%= status %>

                </span>

                <%
                    if ("Pending".equalsIgnoreCase(status)) {
                %>

                    <br>
                    <small>
                        Waiting for dentist confirmation
                    </small>

                <%
                    } else if ("Confirmed".equalsIgnoreCase(status)) {
                %>

                    <br>
                    <small>
                        Your appointment is confirmed
                    </small>

                <%
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                %>

                    <br>
                    <small>
                        Please request another appointment
                    </small>

                <%
                    } else if ("Cancelled".equalsIgnoreCase(status)) {
                %>

                    <br><small><%= row.get("cancellationReason") == null
                            ? "Cancelled by the clinic"
                            : row.get("cancellationReason") %></small>
                    <% if (row.get("refundedAmount") != null) { %>
                        <div class="refund-info">Refunded LKR <%= row.get("refundedAmount") %><br><%= row.get("refundReference") %></div>
                    <% } %>

                <%
                    }
                %>

            </td>

            <td>
                <%
                    if (row.get("appointmentNumber") != null
                            && !"Rejected".equalsIgnoreCase(status)) {
                %>
                    <a href="${pageContext.request.contextPath}/AppointmentConfirmationServlet?appointmentNumber=<%= row.get("appointmentNumber") %>">
                        <%= "Cancelled".equalsIgnoreCase(status) ? "View refund receipt" : "View receipt" %>
                    </a>
                <%
                    } else {
                %>
                    —
                <%
                    }
                %>
            </td>

        </tr>


        <%
                }

            } else {
        %>


        <tr>

            <td colspan="7"
                class="no-data">

                You do not have any appointments.

            </td>

        </tr>


        <%
            }
        %>


        </tbody>

    </table>

    </div>


</div>


<a href="patientDashboard.jsp"
   class="back-link">

    ← Back to Patient Dashboard

</a>


</div>


</body>

</html>
