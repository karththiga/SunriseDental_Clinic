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

    boolean adminHelp = "Admin".equalsIgnoreCase(role);
    boolean staffHelp = "Staff".equalsIgnoreCase(role);
    boolean cashierHelp = "Cashier".equalsIgnoreCase(role);
    boolean dentistHelp = "Dentist".equalsIgnoreCase(role);
    boolean patientHelp = "Patient".equalsIgnoreCase(role);
    String helpTitle = adminHelp ? "Administrator Help"
            : staffHelp ? "Clinic Staff Help"
            : cashierHelp ? "Cashier Help"
            : dentistHelp ? "Dentist Help"
            : patientHelp ? "Patient Help" : "System Help";
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
        <h1><%= helpTitle %></h1><p class="subtitle">Instructions for the functions available to your <%= role %> account.</p>
        <div class="steps">
            <% if (adminHelp) { %>
                <article class="step"><h2>Manage people</h2><p>Open Manage People to create, view, edit or delete user accounts and dentist profiles. Dentist editing also maintains login details, visiting hours, status and treatment assignments together.</p></article>
                <article class="step"><h2>Manage treatments</h2><p>Create, view, edit or delete treatment names and prices. Records already used by appointments are protected so historical clinic data remains accurate.</p></article>
                <article class="step"><h2>Manage appointments</h2><p>Open Manage Appointments to search or edit records. Use Cancel to provide a reason, refund a paid bill automatically and notify the registered patient. Undo remains available for ordinary appointment edits.</p></article>
                <article class="step"><h2>Review clinic reports</h2><p>Open Reports to review appointment totals, upcoming dentist workload, treatment demand and collected revenue. Use Print Report when a paper or PDF copy is required.</p></article>
                <article class="step"><h2>Exit the administration system</h2><p>Select Logout after completing administration work. This invalidates the active session and returns to the public clinic home page.</p></article>
            <% } else if (staffHelp) { %>
                <article class="step"><h2>Register an appointment</h2><p>Open New Appointment, enter the patient and appointment information, validate the selected dentist and treatment, then save the record.</p></article>
                <article class="step"><h2>View and search appointments</h2><p>Use View Appointments for the complete list. Use Search Appointment when an appointment number is available, or use the management search to locate a patient by number, phone or name.</p></article>
                <article class="step"><h2>Update an appointment</h2><p>Open the selected appointment, change only the required values and save. Use the restore option if the latest update must be undone.</p></article>
                <article class="step"><h2>Billing and reports</h2><p>Use Billing to generate a receipt for an appointment that requires staff billing. Use Reports to review clinic appointment summaries and operational activity.</p></article>
                <article class="step"><h2>Exit safely</h2><p>Select Logout when staff work is complete, especially before leaving a shared reception computer.</p></article>
            <% } else if (cashierHelp) { %>
                <article class="step"><h2>Open Billing</h2><p>Select Billing from the Cashier Dashboard and enter the patient appointment number.</p></article>
                <article class="step"><h2>Verify the bill</h2><p>Confirm the appointment, patient, treatment and displayed charges before recording the bill. Do not repeat the operation if a paid receipt already exists.</p></article>
                <article class="step"><h2>Print the receipt</h2><p>After the bill is generated, review the total and use Print Receipt to provide the patient with a paper or PDF copy.</p></article>
                <article class="step"><h2>Exit cashier access</h2><p>Select Logout immediately after completing cashier work. Never leave a billing session open on a shared terminal.</p></article>
            <% } else if (dentistHelp) { %>
                <article class="step"><h2>View assigned appointments</h2><p>Open Upcoming Appointments to see pending and confirmed bookings assigned to your dentist account, including patient, treatment, date and time details.</p></article>
                <article class="step"><h2>Handle legacy pending requests</h2><p>For a Pending request, select Confirm to accept it or Reject to decline it. Check the appointment details before choosing an action because the status is updated immediately.</p></article>
                <article class="step"><h2>Review online reservations</h2><p>Appointments marked Confirmed were reserved and paid online. They are shown for your schedule and do not require another approval.</p></article>
                <article class="step"><h2>Exit dentist access</h2><p>Select Logout when you finish reviewing the schedule so patient information is not left visible.</p></article>
            <% } else if (patientHelp) { %>
                <article class="step"><h2>Reserve an appointment</h2><p>Open Reserve Appointment, choose a treatment, select a dentist and visiting date, then choose one of the live available time slots.</p></article>
                <article class="step"><h2>Review charges and pay</h2><p>Continue to Card Payment. Review the treatment charge, constant hospital charge and total payable, then enter the card details and confirm payment. Card numbers and CVV values are not stored.</p></article>
                <article class="step"><h2>Save the confirmation receipt</h2><p>After successful payment, note the appointment and payment reference numbers. Use Print Receipt to save or print the payment and appointment confirmation.</p></article>
                <article class="step"><h2>View your appointments</h2><p>Open My Appointments to review clinic notifications and booking details. A clinic cancellation shows its reason and, when paid, the refunded amount and refund reference.</p></article>
                <article class="step"><h2>Exit patient access</h2><p>Select Logout when finished, particularly when using a shared device.</p></article>
            <% } %>
        </div>
        <div class="note"><strong>Need assistance?</strong> Contact the system administrator if an available function fails or displays information you do not expect.</div>
        <a class="back-link" href="<%= dashboard %>">← Back to Dashboard</a>
    </section></main>
</body>
</html>
