<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");

    if (username == null || !"Patient".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp");
        return;
    }

    String error = (String) request.getAttribute("error");
    String scheduleMessage =
            (String) request.getAttribute("scheduleMessage");
    Integer selectedTreatmentId =
            (Integer) request.getAttribute("selectedTreatmentId");
    Integer selectedDentistId =
            (Integer) request.getAttribute("selectedDentistId");
    LocalDate selectedDate =
            (LocalDate) request.getAttribute("selectedDate");

    List<Map<String, Object>> treatments =
            (List<Map<String, Object>>) request.getAttribute("treatments");
    List<Map<String, Object>> dentists =
            (List<Map<String, Object>>) request.getAttribute("dentists");
    List<Map<String, String>> availableSlots =
            (List<Map<String, String>>) request.getAttribute("availableSlots");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reserve Appointment - Sunrise Dental Clinic</title>

    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: Arial, sans-serif; background: #f4f7fb; color: #223; }
        .navbar { display: flex; justify-content: space-between; align-items: center; padding: 18px 40px; color: white; background: #1f6feb; }
        .navbar-title { font-size: 22px; font-weight: bold; }
        .navbar a { color: white; text-decoration: none; font-weight: bold; }
        .container { max-width: 920px; margin: 38px auto; padding: 0 24px; }
        .box { padding: 36px; background: white; border-radius: 14px; box-shadow: 0 6px 22px rgba(0,0,0,.08); }
        h1 { margin: 0 0 8px; text-align: center; }
        .subtitle { margin: 0 0 28px; color: #667; text-align: center; }
        .steps { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 28px; }
        .step { padding: 12px; border-radius: 8px; color: #5f6c7b; background: #edf2f8; text-align: center; font-size: 13px; font-weight: bold; }
        .step.active { color: #125ac1; background: #e4efff; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
        .field.full { grid-column: 1 / -1; }
        label { display: block; margin-bottom: 7px; font-weight: bold; }
        select, input { width: 100%; padding: 12px; border: 1px solid #c8d1dd; border-radius: 7px; font-size: 15px; background: white; }
        .hint { margin: 7px 0 0; color: #6c7886; font-size: 13px; }
        .button { width: 100%; margin-top: 20px; padding: 13px; border: 0; border-radius: 7px; color: white; background: #1f6feb; cursor: pointer; font-size: 16px; font-weight: bold; }
        .button:hover { background: #195fc8; }
        .message { margin-bottom: 20px; padding: 13px; border-radius: 8px; text-align: center; }
        .error { color: #a11426; background: #ffe8eb; }
        .info { color: #775300; background: #fff5d8; }
        .slots-panel { margin-top: 28px; padding-top: 27px; border-top: 1px solid #e0e6ed; }
        .slots-panel h2 { margin: 0 0 5px; }
        .slot-summary { margin: 0 0 18px; color: #667; }
        .slots { display: grid; grid-template-columns: repeat(4, 1fr); gap: 11px; }
        .slot input { position: absolute; opacity: 0; pointer-events: none; }
        .slot span { display: block; padding: 12px 8px; border: 1px solid #bfcde0; border-radius: 8px; color: #205b9e; text-align: center; font-weight: bold; cursor: pointer; transition: .15s; }
        .slot input:checked + span { color: white; background: #1f6feb; border-color: #1f6feb; box-shadow: 0 5px 13px rgba(31,111,235,.25); }
        .slot span:hover { border-color: #1f6feb; }
        .empty { padding: 20px; color: #687687; background: #f5f7fa; border-radius: 8px; text-align: center; }
        .note { margin-top: 26px; padding: 16px; color: #365a47; background: #eaf8ef; border-radius: 8px; line-height: 1.5; }
        .back-link { display: block; margin-top: 23px; color: #1f6feb; text-align: center; text-decoration: none; font-weight: bold; }
        @media (max-width: 650px) {
            .navbar { padding: 15px 18px; }
            .navbar-title { font-size: 17px; }
            .container { margin: 22px auto; padding: 0 14px; }
            .box { padding: 24px 18px; }
            .form-grid { grid-template-columns: 1fr; }
            .field.full { grid-column: auto; }
            .slots { grid-template-columns: repeat(2, 1fr); }
            .steps { font-size: 11px; }
        }
    </style>
</head>

<body>
    <div class="navbar">
        <div class="navbar-title">Sunrise Dental Clinic | Patient</div>
        <a href="patientDashboard.jsp">Dashboard</a>
    </div>

    <main class="container">
        <section class="box">
            <h1>Reserve an Appointment</h1>
            <p class="subtitle">View live availability and confirm your appointment instantly.</p>

            <div class="steps">
                <div class="step active">1. Choose service</div>
                <div class="step <%= selectedTreatmentId != null ? "active" : "" %>">2. Check availability</div>
                <div class="step <%= availableSlots != null ? "active" : "" %>">3. Reserve slot</div>
            </div>

            <% if (error != null) { %>
                <div class="message error"><%= error %></div>
            <% } %>
            <% if (scheduleMessage != null) { %>
                <div class="message info"><%= scheduleMessage %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/PatientRequestAppointmentServlet"
                  method="get">
                <div class="form-grid">
                    <div class="field <%= selectedTreatmentId == null ? "full" : "" %>">
                        <label for="treatmentId">Treatment</label>
                        <select id="treatmentId" name="treatmentId" required>
                            <option value="">Select treatment</option>
                            <% if (treatments != null) {
                                for (Map<String, Object> treatment : treatments) {
                                    Integer treatmentId =
                                            (Integer) treatment.get("treatmentId"); %>
                                    <option value="<%= treatmentId %>"
                                        <%= treatmentId.equals(selectedTreatmentId)
                                                ? "selected" : "" %>>
                                        <%= treatment.get("treatmentName") %> — Rs. <%= treatment.get("treatmentCost") %>
                                    </option>
                            <%  }
                               } %>
                        </select>
                    </div>

                    <% if (selectedTreatmentId != null) { %>
                        <div class="field">
                            <label for="dentistId">Dentist</label>
                            <select id="dentistId" name="dentistId" required>
                                <option value="">Select dentist</option>
                                <% if (dentists != null) {
                                    for (Map<String, Object> dentist : dentists) {
                                        Integer dentistId =
                                            (Integer) dentist.get("dentistId"); %>
                                        <option value="<%= dentistId %>"
                                            <%= dentistId.equals(selectedDentistId)
                                                    ? "selected" : "" %>>
                                            <%= dentist.get("dentistName") %> — <%= dentist.get("availableDay") %>
                                        </option>
                                <%  }
                                   } %>
                            </select>
                            <% if (dentists == null || dentists.isEmpty()) { %>
                                <p class="hint">No dentist with configured visiting hours offers this treatment.</p>
                            <% } %>
                        </div>

                        <div class="field full">
                            <label for="appointmentDate">Appointment date</label>
                            <input id="appointmentDate" type="date"
                                   name="appointmentDate"
                                   min="<%= request.getAttribute("minimumDate") %>"
                                   value="<%= selectedDate == null ? "" : selectedDate %>"
                                   required>
                            <p class="hint">Choose the visiting day shown beside the dentist's name.</p>
                        </div>
                    <% } %>
                </div>

                <button class="button" type="submit">
                    <%= selectedTreatmentId == null
                            ? "Continue to dentists" : "Show available slots" %>
                </button>
            </form>

            <% if (availableSlots != null && scheduleMessage == null) { %>
                <div class="slots-panel">
                    <h2>Available time slots</h2>
                    <p class="slot-summary">
                        <%= request.getAttribute("selectedDentistName") %> ·
                        <%= selectedDate %> ·
                        <%= request.getAttribute("visitingWindow") %>
                    </p>

                    <% if (!availableSlots.isEmpty()) { %>
                        <form action="${pageContext.request.contextPath}/PatientRequestAppointmentServlet"
                              method="post">
                            <input type="hidden" name="treatmentId" value="<%= selectedTreatmentId %>">
                            <input type="hidden" name="dentistId" value="<%= selectedDentistId %>">
                            <input type="hidden" name="appointmentDate" value="<%= selectedDate %>">

                            <div class="slots">
                                <% for (Map<String, String> slot : availableSlots) { %>
                                    <label class="slot">
                                        <input type="radio" name="appointmentTime"
                                               value="<%= slot.get("value") %>" required>
                                        <span><%= slot.get("label") %></span>
                                    </label>
                                <% } %>
                            </div>

                            <button class="button" type="submit">
                                Reserve and Confirm Appointment
                            </button>
                        </form>
                    <% } else { %>
                        <div class="empty">No slots remain for this date. Please choose another date.</div>
                    <% } %>
                </div>
            <% } %>

            <div class="note">
                Available times are updated from current appointment data. Your selected
                slot is checked again when you reserve it, then confirmed immediately—no
                administrator approval is required.
            </div>
        </section>

        <a href="patientDashboard.jsp" class="back-link">← Back to Patient Dashboard</a>
    </main>
</body>
</html>
