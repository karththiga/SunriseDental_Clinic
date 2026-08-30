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
        .cost-note { margin-top: 24px; overflow: hidden; border: 1px solid #dce9e8; border-radius: 11px; }
        .cost-note h2 { margin: 0; padding: 14px 17px; font-size: 19px; background: #f1faf8; }
        .cost-row { display: flex; justify-content: space-between; gap: 22px; padding: 13px 17px; border-top: 1px solid #dce9e8; }
        .cost-row span { color: #526274; }
        .cost-row small { display: block; margin-top: 2px; color: #738092; }
        .cost-row strong { color: #123047; white-space: nowrap; }
        .cost-row.total { padding: 18px 17px; color: white; background: #123047; font-size: 21px; }
        .cost-row.total span, .cost-row.total strong { color: white; }
        .receipt-note { margin: 10px 17px 14px; color: #607583; font-size: 12px; }
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
            <p>Payment and Appointment Confirmation Receipt</p>
            <% if (Boolean.TRUE.equals(found)) { %>
                <span class="confirmed">✓ <%= Boolean.TRUE.equals(request.getAttribute("refunded")) ? "Appointment Cancelled · Payment Refunded" : Boolean.TRUE.equals(request.getAttribute("paymentFound")) ? "Payment Received · Appointment Confirmed" : "Appointment Confirmed" %></span>
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
                    <% if (request.getAttribute("cancellationReason") != null) { %><div class="detail"><small>Cancellation reason</small><strong><%= request.getAttribute("cancellationReason") %></strong></div><% } %>
                    <% if (Boolean.TRUE.equals(request.getAttribute("paymentFound"))) { %>
                        <div class="detail"><small>Payment reference</small><strong><%= request.getAttribute("paymentReference") %></strong></div>
                        <div class="detail"><small>Payment status</small><strong><%= request.getAttribute("paymentStatus") %></strong></div>
                        <div class="detail"><small>Payment method</small><strong><%= request.getAttribute("paymentMethod") %> ending <%= request.getAttribute("cardLastFour") %></strong></div>
                        <div class="detail"><small>Paid at</small><strong><%= request.getAttribute("paymentDate") %></strong></div>
                        <% if (Boolean.TRUE.equals(request.getAttribute("refunded"))) { %>
                            <div class="detail"><small>Refund reference</small><strong><%= request.getAttribute("refundReference") %></strong></div>
                            <div class="detail"><small>Refunded at</small><strong><%= request.getAttribute("refundedAt") %></strong></div>
                        <% } %>
                    <% } %>
                </div>

                <div class="cost-note">
                    <h2>Payment Breakdown</h2>
                    <% if (Boolean.TRUE.equals(request.getAttribute("paymentFound"))) { %>
                        <div class="cost-row">
                            <span>Treatment charge<small>Doctor and equipment charges included</small></span>
                            <strong>Rs. <%= request.getAttribute("treatmentCost") %></strong>
                        </div>
                        <div class="cost-row">
                            <span>Hospital charges</span>
                            <strong>Rs. <%= request.getAttribute("hospitalCharge") %></strong>
                        </div>
                        <div class="cost-row total">
                            <span><%= Boolean.TRUE.equals(request.getAttribute("refunded")) ? "Total Refunded" : "Total Paid" %></span>
                            <strong>Rs. <%= Boolean.TRUE.equals(request.getAttribute("refunded")) ? request.getAttribute("refundedAmount") : request.getAttribute("totalAmount") %></strong>
                        </div>
                        <p class="receipt-note"><%= Boolean.TRUE.equals(request.getAttribute("refunded")) ? "This document confirms the appointment cancellation and full simulated refund." : "This document is your payment and appointment confirmation receipt." %></p>
                    <% } else { %>
                        <div class="cost-row"><span>Treatment charge</span><strong>Rs. <%= request.getAttribute("treatmentCost") %></strong></div>
                        <div class="cost-row"><span>Consultation fee</span><strong>Rs. <%= request.getAttribute("consultationFee") %></strong></div>
                        <p class="receipt-note">This legacy appointment does not have an online-payment receipt.</p>
                    <% } %>
                </div>
            <% } %>

            <div class="actions">
                <a class="button secondary" href="${pageContext.request.contextPath}/PatientAppointmentsServlet">My appointments</a>
                <% if (Boolean.TRUE.equals(found)) { %>
                    <button class="button" type="button" onclick="window.print()">Print receipt</button>
                <% } %>
            </div>
        </section>
    </main>
</body>
</html>
