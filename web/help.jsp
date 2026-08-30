<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    String dashboard = "dashboard.jsp";
    if ("Admin".equalsIgnoreCase(role)) dashboard = "adminDashboard.jsp";
    else if ("Cashier".equalsIgnoreCase(role)) dashboard = "cashierDashboard.jsp";
    else if ("Dentist".equalsIgnoreCase(role)) dashboard = "dentistDashboard.jsp";
    else if ("Patient".equalsIgnoreCase(role)) dashboard = "patientDashboard.jsp";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Help - Sunrise Dental Clinic</title>
    <style>
        * { box-sizing: border-box; } body { margin: 0; }
        .navbar { display: flex; justify-content: space-between; align-items: center; }
        .navbar-title { font-size: 22px; font-weight: bold; }
        .navbar a { color: white; text-decoration: none; font-weight: bold; }
        .container { max-width: 980px; margin: 40px auto; padding: 0 20px; }
        .box { padding: 34px; background: white; }
        h1 { margin: 0 0 5px; } .subtitle { margin: 0 0 26px; }
        .steps { display: grid; gap: 16px; counter-reset: help-step; }
        .step { position: relative; padding: 20px 20px 20px 69px; background: #f8fcfb; border: 1px solid #dce9e8; border-radius: 13px; counter-increment: help-step; }
        .step::before { content: counter(help-step); position: absolute; top: 19px; left: 19px; width: 34px; height: 34px; display: grid; place-items: center; color: white; background: #21a7a0; border-radius: 50%; font-weight: bold; }
        .step h2 { margin: 0 0 5px; font-size: 20px; }
        .step p { margin: 0; color: #607583; }
        .note { margin-top: 24px; padding: 17px; color: #123047; background: #e9f8f5; border-left: 4px solid #21a7a0; border-radius: 9px; }
        .back-link { display: block; margin-top: 24px; text-align: center; text-decoration: none; font-weight: bold; }
        @media (max-width: 550px) { .box { padding: 23px 16px; } }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>
<body>
    <header class="navbar"><div class="navbar-title">Sunrise Dental Clinic | Help</div><a href="<%= dashboard %>">Dashboard</a></header>
    <main class="container"><section class="box">
        <h1>System Help</h1><p class="subtitle">Step-by-step guidance for using the clinic management system safely.</p>
        <div class="steps">
            <article class="step"><h2>Sign in securely</h2><p>Use the Login option on the public home page. Never share your password. Use Logout when you finish, especially on a shared clinic computer.</p></article>
            <article class="step"><h2>Register or reserve an appointment</h2><p>Open the appointment option, select an available dentist, treatment, date and live time slot, then enter the patient details. Review the details before confirming.</p></article>
            <article class="step"><h2>Find an appointment</h2><p>Administrators can open Manage Appointments and search by appointment number, patient name or phone number. The full list appears when the search field is empty.</p></article>
            <article class="step"><h2>Update an appointment</h2><p>Select the edit icon beside an appointment. Change only the required information and save. The previous version can be restored using the undo option provided after an update.</p></article>
            <article class="step"><h2>Generate a bill</h2><p>Open Billing, enter the appointment number and generate the receipt. The system combines the treatment charge and dentist consultation fee and records the paid bill.</p></article>
            <article class="step"><h2>Maintain clinic data</h2><p>Administrators use Manage People for accounts and dentist schedules, Manage Treatments for services and prices, and Reports for appointment, workload and revenue summaries.</p></article>
            <article class="step"><h2>Exit safely</h2><p>Select Logout in the dashboard header. This invalidates the active session and returns to the public clinic home page.</p></article>
        </div>
        <div class="note"><strong>Need assistance?</strong> Contact the system administrator before repeating a failed billing or appointment operation, to avoid duplicate records.</div>
        <a class="back-link" href="<%= dashboard %>">← Back to Dashboard</a>
    </section></main>
</body>
</html>
