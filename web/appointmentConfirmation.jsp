<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    if (username == null || !"Patient".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp");
        return;
    }

    String error = (String) request.getAttribute("error");
    Boolean found = (Boolean) request.getAttribute("found");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Appointment Confirmation - Sunrise Dental Clinic</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; padding: 35px 18px; font-family: Arial, sans-serif; color: #223; background: #eef3f8; }
        .receipt { max-width: 720px; margin: 0 auto; overflow: hidden; background: white; border-radius: 15px; box-shadow: 0 10px 35px rgba(20,45,75,.13); }
        .receipt-head { padding: 30px 34px; color: white; background: #21a7a0; }
        .receipt-head h1 { margin: 0 0 5px; font-size: 27px; }
        .receipt-head p { margin: 0; color: #dceaff; }
        .confirmed { display: inline-block; margin-top: 17px; padding: 7px 12px; color: #12622b; background: #e0f7e8; border-radius: 999px; font-size: 13px; font-weight: bold; }
        .receipt-body { padding: 32px 34px; }
        .appointment-number { margin-bottom: 26px; padding: 18px; color: #176b87; background: #e9f8f5; border: 1px dashed #8fc8c4; border-radius: 9px; text-align: center; }
        .appointment-number small { display: block; color: #647589; text-transform: uppercase; letter-spacing: .08em; }
        .appointment-number strong { display: block; margin-top: 4px; font-size: 28px; letter-spacing: .04em; }
        .details { display: grid; grid-template-columns: 1fr 1fr; gap: 0 25px; }
        .detail { padding: 14px 0; border-bottom: 1px solid #e6ebf1; }
        .detail small { display: block; color: #738092; margin-bottom: 3px; }
        .detail strong { color: #1e344c; }
        .cost-note { margin-top: 22px; padding: 15px; color: #526274; background: #f6f8fa; border-radius: 8px; font-size: 13px; }
        .actions { display: flex; gap: 12px; margin-top: 27px; }
        .button { flex: 1; padding: 12px; border: 1px solid #176b87; border-radius: 7px; color: white; background: #21a7a0; text-align: center; text-decoration: none; font-weight: bold; cursor: pointer; }
        .button.secondary { color: #176b87; background: white; }
        .error { padding: 20px; color: #a11426; background: #ffe8eb; border-radius: 8px; text-align: center; }
        @media print { body { padding: 0; background: white; } .receipt { box-shadow: none; } .actions { display: none; } }
        @media (max-width: 560px) { .receipt-body, .receipt-head { padding: 24px 20px; } .details { grid-template-columns: 1fr; } .actions { flex-direction: column; } }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>
<body>
    <main class="receipt">
        <header class="receipt-head">
            <h1>Sunrise Dental Clinic</h1>
            <p>Appointment Confirmation Receipt</p>
            <% if (Boolean.TRUE.equals(found)) { %>
                <span class="confirmed">✓ Appointment Confirmed</span>
            <% } %>
        </header>

        <section class="receipt-body">
            <% if (error != null) { %>
                <div class="error"><%= error %></div>
            <% } else if (Boolean.TRUE.equals(found)) { %>
                <div class="appointment-number">
                    <small>Appointment number</small>
                    <strong><%= request.getAttribute("appointmentNumber") %></strong>
                </div>

                <div class="details">
                    <div class="detail"><small>Patient</small><strong><%= request.getAttribute("patientName") %></strong></div>
                    <div class="detail"><small>Contact number</small><strong><%= request.getAttribute("contactNumber") %></strong></div>
                    <div class="detail"><small>Dentist</small><strong><%= request.getAttribute("dentistName") %></strong></div>
                    <div class="detail"><small>Specialization</small><strong><%= request.getAttribute("specialization") == null ? "General Dentistry" : request.getAttribute("specialization") %></strong></div>
                    <div class="detail"><small>Treatment</small><strong><%= request.getAttribute("treatmentName") %></strong></div>
                    <div class="detail"><small>Status</small><strong><%= request.getAttribute("status") %></strong></div>
                    <div class="detail"><small>Date</small><strong><%= request.getAttribute("appointmentDate") %></strong></div>
                    <div class="detail"><small>Time</small><strong><%= request.getAttribute("appointmentTime") %></strong></div>
                </div>

                <div class="cost-note">
                    Estimated treatment cost: Rs. <%= request.getAttribute("treatmentCost") %> ·
                    Consultation fee: Rs. <%= request.getAttribute("consultationFee") %>.
                    This is an appointment confirmation, not a payment receipt.
                </div>
            <% } %>

            <div class="actions">
                <a class="button secondary" href="${pageContext.request.contextPath}/PatientAppointmentsServlet">My appointments</a>
                <% if (Boolean.TRUE.equals(found)) { %>
                    <button class="button" type="button" onclick="window.print()">Print confirmation</button>
                <% } %>
            </div>
        </section>
    </main>
</body>
</html>
