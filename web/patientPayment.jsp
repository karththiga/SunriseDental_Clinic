<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    if (username == null || !"Patient".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp");
        return;
    }
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Appointment Payment - Sunrise Dental Clinic</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; }
        .navbar { display: flex; align-items: center; justify-content: space-between; }
        .navbar-title { font-size: 21px; font-weight: bold; }
        .navbar a { color: white; text-decoration: none; font-weight: bold; }
        .container { max-width: 1000px; margin: 40px auto; padding: 0 20px; }
        .box { overflow: hidden; background: white; }
        .checkout-head { padding: 29px 32px 22px; border-bottom: 1px solid #dce9e8; }
        h1 { margin: 0 0 5px; }
        .subtitle { margin: 0; color: #607583; }
        .steps { display: grid; grid-template-columns: repeat(3, 1fr); margin-top: 22px; color: #607583; font-size: 13px; font-weight: bold; }
        .step { padding: 9px; border-bottom: 3px solid #dce9e8; text-align: center; }
        .step.active { color: #123047; border-color: #21a7a0; background: #e9f8f5; }
        .checkout-grid { display: grid; grid-template-columns: .9fr 1.1fr; }
        .summary { padding: 29px 30px; background: #f4fbf9; border-right: 1px solid #dce9e8; }
        .summary h2, .payment h2 { margin: 0 0 18px; font-size: 22px; }
        .summary-row { display: flex; justify-content: space-between; gap: 20px; padding: 11px 0; border-bottom: 1px solid #dce9e8; }
        .summary-row span { color: #607583; }
        .summary-row strong { color: #123047; text-align: right; }
        .summary-row.total { margin-top: 14px; padding: 17px 14px; color: white; background: #123047; border: 0; border-radius: 10px; font-size: 20px; }
        .summary-row.total span, .summary-row.total strong { color: white; }
        .payment { padding: 29px 30px; }
        .message { margin-bottom: 18px; padding: 12px 14px; color: #9d2638; background: #ffeaed; border-radius: 9px; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
        .field { display: flex; flex-direction: column; gap: 6px; }
        .field.full { grid-column: 1 / -1; }
        label { color: #123047; font-size: 14px; font-weight: bold; }
        input { width: 100%; min-height: 45px; padding: 10px 11px; border: 1px solid #bfd3d3; }
        .pay-button { width: 100%; min-height: 48px; margin-top: 19px; border: 0; font-size: 15px; font-weight: bold; cursor: pointer; }
        .security { margin: 13px 0 0; color: #607583; font-size: 12px; text-align: center; }
        .back-link { display: block; margin-top: 20px; text-align: center; text-decoration: none; font-weight: bold; }
        @media (max-width: 760px) { .checkout-grid { grid-template-columns: 1fr; } .summary { border-right: 0; border-bottom: 1px solid #dce9e8; } }
        @media (max-width: 520px) { .container { padding: 0 13px; } .checkout-head, .summary, .payment { padding: 23px 18px; } .form-grid { grid-template-columns: 1fr; } .field.full { grid-column: auto; } .steps { font-size: 10px; } }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>
<body>
    <header class="navbar">
        <div class="navbar-title">Sunrise Dental Clinic | Secure Checkout</div>
        <a href="patientDashboard.jsp">Dashboard</a>
    </header>
    <main class="container">
        <section class="box">
            <div class="checkout-head">
                <h1>Complete Appointment Payment</h1>
                <p class="subtitle">Review the booking and authorize the simulated card payment.</p>
                <div class="steps"><div class="step active">1. Slot selected</div><div class="step active">2. Payment</div><div class="step">3. Confirmation</div></div>
            </div>
            <div class="checkout-grid">
                <section class="summary">
                    <h2>Booking Summary</h2>
                    <div class="summary-row"><span>Patient</span><strong><%= request.getAttribute("patientName") %></strong></div>
                    <div class="summary-row"><span>Dentist</span><strong><%= request.getAttribute("dentistName") %></strong></div>
                    <div class="summary-row"><span>Treatment</span><strong><%= request.getAttribute("treatmentName") %></strong></div>
                    <div class="summary-row"><span>Date</span><strong><%= request.getAttribute("appointmentDate") %></strong></div>
                    <div class="summary-row"><span>Time</span><strong><%= request.getAttribute("appointmentTime") %></strong></div>
                    <div class="summary-row"><span>Treatment charge<br><small>Doctor and equipment charges included</small></span><strong>Rs. <%= request.getAttribute("treatmentCost") %></strong></div>
                    <div class="summary-row"><span>Hospital charges</span><strong>Rs. <%= request.getAttribute("hospitalCharge") %></strong></div>
                    <div class="summary-row total"><span>Total payable</span><strong>Rs. <%= request.getAttribute("totalAmount") %></strong></div>
                </section>
                <section class="payment">
                    <h2>Card Payment</h2>
                    <% if (error != null) { %><div class="message"><%= error %></div><% } %>
                    <form action="${pageContext.request.contextPath}/PatientPaymentServlet" method="post" autocomplete="off">
                        <input type="hidden" name="checkout" value="<%= request.getAttribute("checkoutToken") %>">
                        <div class="form-grid">
                            <div class="field full"><label for="cardholderName">Cardholder Name</label><input id="cardholderName" name="cardholderName" type="text" maxlength="100" required></div>
                            <div class="field full"><label for="cardNumber">Card Number</label><input id="cardNumber" name="cardNumber" type="text" inputmode="numeric" placeholder="Card number" maxlength="23" required></div>
                            <div class="field"><label for="expiry">Expiry (MM/YY)</label><input id="expiry" name="expiry" type="text" inputmode="numeric" placeholder="12/30" maxlength="5" pattern="(0[1-9]|1[0-2])/[0-9]{2}" required></div>
                            <div class="field"><label for="cvv">CVV</label><input id="cvv" name="cvv" type="password" inputmode="numeric" placeholder="123" maxlength="4" pattern="[0-9]{3,4}" required></div>
                        </div>
                        <button class="pay-button" type="submit">Pay Rs. <%= request.getAttribute("totalAmount") %> and Confirm</button>
                    </form>
                    <p class="security">Full card numbers and CVV values are never stored.</p>
                </section>
            </div>
        </section>
        <a class="back-link" href="${pageContext.request.contextPath}/PatientRequestAppointmentServlet">← Choose another appointment</a>
    </main>
</body>
</html>
