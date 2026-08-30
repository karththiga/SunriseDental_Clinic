<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%!
    private String value(Object item) { return item == null ? "" : String.valueOf(item); }
    private boolean selected(Object selectedIds, Object id) {
        if (selectedIds instanceof List) return ((List<?>) selectedIds).contains(String.valueOf(id));
        if (selectedIds instanceof String[]) {
            for (String item : (String[]) selectedIds) if (item.equals(String.valueOf(id))) return true;
        }
        return false;
    }
%>
<%
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
    if (username == null || !"Admin".equalsIgnoreCase(role)) { response.sendRedirect("login.jsp"); return; }
    String activeTab = "dentists".equals(request.getAttribute("activeTab")) ? "dentists" : "users";
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
    String editUserId = value(request.getAttribute("editUserId"));
    String editDentistId = value(request.getAttribute("editDentistId"));
    boolean editingUser = !editUserId.isEmpty();
    boolean editingDentist = !editDentistId.isEmpty();
    List<Map<String, Object>> users = (List<Map<String, Object>>) request.getAttribute("users");
    List<Map<String, Object>> dentists = (List<Map<String, Object>>) request.getAttribute("dentists");
    List<Map<String, Object>> treatments = (List<Map<String, Object>>) request.getAttribute("treatments");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage People - Sunrise Dental Clinic</title>
    <style>
        * { box-sizing: border-box; } body { margin: 0; }
        .navbar { display:flex;align-items:center;justify-content:space-between }.navbar-title{font-size:22px;font-weight:bold}.navbar a{color:white;text-decoration:none;font-weight:bold}
        .container{max-width:1220px;margin:40px auto;padding:0 20px}.box{padding:32px;background:white}h1{margin:0 0 5px}.subtitle{margin:0 0 24px}
        .tabs{display:flex;gap:8px;margin-bottom:27px;padding-bottom:12px;border-bottom:1px solid #dce9e8}.tab{padding:10px 18px;color:#176b87;background:#e9f8f5;border-radius:9px;text-decoration:none;font-weight:bold}.tab.active{color:white!important;background:#123047}
        .message{margin-bottom:20px;padding:13px 15px;border-radius:9px}.message.error{color:#9d2638;background:#ffeaed;border:1px solid #f0bdc4}.message.success{color:#176454;background:#e8f8f2;border:1px solid #b9dfd2}
        .form-panel{margin-bottom:30px;padding:24px;background:#f8fcfb;border:1px solid #dce9e8;border-radius:14px}.form-panel h2{margin:0 0 16px;font-size:23px}.form-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:17px}
        .field{display:flex;flex-direction:column;gap:6px}.field label,.field-title{color:#123047;font-size:14px;font-weight:bold}.field input,.field select{width:100%;min-height:45px;padding:10px 11px;border:1px solid #bfd3d3;background:white}.hint{color:#607583;font-size:12px}.span-all{grid-column:1/-1}
        .treatment-options{display:flex;flex-wrap:wrap;gap:9px;margin-top:7px}.check-option{display:inline-flex;align-items:center;gap:7px;padding:8px 11px;background:white;border:1px solid #bfd3d3;border-radius:9px}.check-option input{width:auto;min-height:auto;margin:0}
        .form-actions,.row-actions{display:flex;gap:8px;align-items:center}.form-actions{justify-content:flex-end;margin-top:20px}.form-actions button,.action-link,.delete-button{min-height:42px;padding:0 16px;border:0;border-radius:8px;font-weight:bold;cursor:pointer}.action-link{display:inline-flex;align-items:center;color:white!important;background:#21a7a0;text-decoration:none}.cancel{background:#e9f8f5!important;color:#123047!important}.delete-button{color:#9d2638;background:#ffeaed}.inline-form{display:inline;margin:0}
        .list-heading{display:flex;justify-content:space-between;align-items:center;gap:15px;margin-bottom:13px}.list-heading h2{margin:0;font-size:23px}.count{color:#607583;font-size:14px}.table-container{overflow-x:auto;border:1px solid #dce9e8;border-radius:13px}table{width:100%;min-width:980px;border-collapse:collapse}.dentist-table{min-width:1320px}th,td{padding:12px;text-align:left;vertical-align:top}td{border-bottom:1px solid #dce9e8}.name{color:#123047;font-weight:bold}.status{display:inline-flex;padding:4px 9px;color:#176454;background:#e8f8f2;border-radius:999px;font-size:12px;font-weight:bold}.no-data{padding:35px;color:#607583;text-align:center}.back-link{display:block;margin-top:24px;text-align:center;text-decoration:none;font-weight:bold}
        @media(max-width:850px){.form-grid{grid-template-columns:1fr 1fr}}@media(max-width:600px){.box{padding:22px 16px}.form-panel{padding:18px 14px}.form-grid{grid-template-columns:1fr}.tabs{display:grid;grid-template-columns:1fr 1fr}.tab{text-align:center}}
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinic-theme.css">
</head>
<body>
<header class="navbar"><div class="navbar-title">Sunrise Dental Clinic | Admin</div><a href="adminDashboard.jsp">Admin Dashboard</a></header>
<main class="container"><section class="box">
    <h1>Manage People</h1><p class="subtitle">Create, view, update and delete clinic accounts and dentist profiles.</p>
    <nav class="tabs"><a class="tab <%= "users".equals(activeTab)?"active":"" %>" href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=users">User Accounts</a><a class="tab <%= "dentists".equals(activeTab)?"active":"" %>" href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=dentists">Dentist Profiles</a></nav>
    <% if(error!=null){%><div class="message error"><%=error%></div><%} if(success!=null){%><div class="message success"><%=success%></div><%}%>

    <% if("users".equals(activeTab)){ %>
    <section class="form-panel">
        <h2><%=editingUser?"Update User Account":"Add User Account"%></h2>
        <form action="${pageContext.request.contextPath}/ManagePeopleServlet" method="post">
            <input type="hidden" name="action" value="<%=editingUser?"updateUser":"createUser"%>"><%if(editingUser){%><input type="hidden" name="userId" value="<%=editUserId%>"><%}%>
            <div class="form-grid">
                <div class="field"><label for="firstName">First Name</label><input id="firstName" name="firstName" value="<%=value(request.getAttribute("editFirstName"))%>" required></div>
                <div class="field"><label for="lastName">Last Name</label><input id="lastName" name="lastName" value="<%=value(request.getAttribute("editLastName"))%>" required></div>
                <div class="field"><label for="username">Email Address</label><input id="username" name="username" type="email" value="<%=value(request.getAttribute("editUsername"))%>" required></div>
                <div class="field"><label for="phoneNumber">Phone Number</label><input id="phoneNumber" name="phoneNumber" type="tel" pattern="[0-9]{10}" maxlength="10" value="<%=value(request.getAttribute("editPhoneNumber"))%>" required></div>
                <div class="field"><label for="password"><%=editingUser?"New Password (optional)":"Temporary Password"%></label><input id="password" name="password" type="password" minlength="6" <%=editingUser?"":"required"%>><%if(editingUser){%><span class="hint">Leave blank to keep the current password.</span><%}%></div>
                <div class="field"><label for="role">Role</label><select id="role" name="role" required><%String selectedRole=value(request.getAttribute("editRole")); String[] roles={"Patient","Staff","Cashier","Admin"}; for(String item:roles){%><option value="<%=item%>" <%=item.equals(selectedRole)?"selected":""%>><%=item%></option><%}%></select></div>
            </div>
            <div class="form-actions"><button type="submit"><%=editingUser?"Save Changes":"Create User"%></button><%if(editingUser){%><a class="action-link cancel" href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=users">Cancel</a><%}%></div>
        </form>
    </section>
    <div class="list-heading"><h2>All User Accounts</h2><span class="count"><%=users==null?0:users.size()%> account(s)</span></div>
    <div class="table-container"><table><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th>Actions</th></tr></thead><tbody>
    <%if(users!=null&&!users.isEmpty()){for(Map<String,Object> user:users){%><tr><td><%=user.get("userId")%></td><td class="name"><%=user.get("name")%></td><td><%=user.get("username")%></td><td><%=user.get("phoneNumber")%></td><td><%=user.get("role")%></td><td><div class="row-actions"><a class="action-link" href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=users&edit=<%=user.get("userId")%>">Edit</a><form class="inline-form" method="post" action="${pageContext.request.contextPath}/ManagePeopleServlet" onsubmit="return confirm('Delete this user account?');"><input type="hidden" name="action" value="deleteUser"><input type="hidden" name="userId" value="<%=user.get("userId")%>"><button class="delete-button" type="submit">Delete</button></form></div></td></tr><%}}else{%><tr><td colspan="6" class="no-data">No user accounts are available.</td></tr><%}%>
    </tbody></table></div>

    <% } else { %>
    <section class="form-panel">
        <h2><%=editingDentist?"Update Dentist":"Add Dentist"%></h2>
        <form action="${pageContext.request.contextPath}/ManagePeopleServlet" method="post">
            <input type="hidden" name="action" value="<%=editingDentist?"updateDentist":"createDentist"%>"><%if(editingDentist){%><input type="hidden" name="dentistId" value="<%=editDentistId%>"><input type="hidden" name="userId" value="<%=value(request.getAttribute("editDentistUserId"))%>"><%}%>
            <div class="form-grid">
                <div class="field"><label for="dentistFirstName">First Name</label><input id="dentistFirstName" name="firstName" value="<%=value(request.getAttribute("editFirstName"))%>" required></div>
                <div class="field"><label for="dentistLastName">Last Name</label><input id="dentistLastName" name="lastName" value="<%=value(request.getAttribute("editLastName"))%>" required></div>
                <div class="field"><label for="dentistUsername">Login Email</label><input id="dentistUsername" name="username" type="email" value="<%=value(request.getAttribute("editUsername"))%>" required></div>
                <div class="field"><label for="dentistPhone">Phone Number</label><input id="dentistPhone" name="phoneNumber" type="tel" pattern="[0-9]{10}" maxlength="10" value="<%=value(request.getAttribute("editPhoneNumber"))%>" required></div>
                <div class="field"><label for="dentistPassword"><%=editingDentist?"New Password (optional)":"Temporary Password"%></label><input id="dentistPassword" name="password" type="password" minlength="6" <%=editingDentist?"":"required"%>><%if(editingDentist){%><span class="hint">Leave blank to keep the current password.</span><%}%></div>
                <div class="field"><label for="dentistName">Display Name</label><input id="dentistName" name="dentistName" value="<%=value(request.getAttribute("editDentistName"))%>" placeholder="Dr. Full Name" required></div>
                <div class="field"><label for="specialization">Specialization</label><input id="specialization" name="specialization" value="<%=value(request.getAttribute("editSpecialization"))%>" required></div>
                <div class="field"><label for="qualification">Qualification</label><input id="qualification" name="qualification" value="<%=value(request.getAttribute("editQualification"))%>" required></div>
                <div class="field"><label for="consultationFee">Consultation Fee (LKR)</label><input id="consultationFee" name="consultationFee" type="number" min="0" step="0.01" value="<%=value(request.getAttribute("editConsultationFee"))%>" required></div>
                <div class="field"><label for="availableDay">Visiting Day</label><select id="availableDay" name="availableDay" required><option value="">Select day</option><%String selectedDay=value(request.getAttribute("editAvailableDay"));String[] days={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};for(String day:days){%><option <%=day.equals(selectedDay)?"selected":""%>><%=day%></option><%}%></select></div>
                <div class="field"><label for="availableFrom">Available From</label><input id="availableFrom" name="availableFrom" type="time" value="<%=value(request.getAttribute("editAvailableFrom"))%>" required></div>
                <div class="field"><label for="availableTo">Available To</label><input id="availableTo" name="availableTo" type="time" value="<%=value(request.getAttribute("editAvailableTo"))%>" required></div>
                <div class="field"><label for="status">Status</label><select id="status" name="status"><option <%=!"Inactive".equals(value(request.getAttribute("editStatus")))?"selected":""%>>Active</option><option <%= "Inactive".equals(value(request.getAttribute("editStatus")))?"selected":""%>>Inactive</option></select></div>
                <div class="span-all"><div class="field-title">Treatments</div><div class="treatment-options"><%Object selectedTreatments=request.getAttribute("editTreatmentIds");if(treatments!=null&&!treatments.isEmpty()){for(Map<String,Object> treatment:treatments){%><label class="check-option"><input type="checkbox" name="treatmentIds" value="<%=treatment.get("treatmentId")%>" <%=selected(selectedTreatments,treatment.get("treatmentId"))?"checked":""%>><%=treatment.get("treatmentName")%></label><%}}else{%><span>No treatments are configured.</span><%}%></div></div>
            </div>
            <div class="form-actions"><button type="submit"><%=editingDentist?"Save Changes":"Create Dentist"%></button><%if(editingDentist){%><a class="action-link cancel" href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=dentists">Cancel</a><%}%></div>
        </form>
    </section>
    <div class="list-heading"><h2>All Dentist Profiles</h2><span class="count"><%=dentists==null?0:dentists.size()%> dentist(s)</span></div>
    <div class="table-container"><table class="dentist-table"><thead><tr><th>Dentist</th><th>Login Email</th><th>Specialization</th><th>Qualification</th><th>Visiting Hours</th><th>Treatments</th><th>Fee</th><th>Status</th><th>Actions</th></tr></thead><tbody>
    <%if(dentists!=null&&!dentists.isEmpty()){for(Map<String,Object> dentist:dentists){%><tr><td class="name"><%=dentist.get("dentistName")%></td><td><%=dentist.get("username")%></td><td><%=dentist.get("specialization")%></td><td><%=dentist.get("qualification")%></td><td><%=dentist.get("availableDay")%><br><%=dentist.get("availableFrom")%> – <%=dentist.get("availableTo")%></td><td><%=dentist.get("treatments")%></td><td>LKR <%=dentist.get("consultationFee")%></td><td><span class="status"><%=dentist.get("status")%></span></td><td><div class="row-actions"><a class="action-link" href="${pageContext.request.contextPath}/ManagePeopleServlet?tab=dentists&edit=<%=dentist.get("dentistId")%>">Edit</a><form class="inline-form" method="post" action="${pageContext.request.contextPath}/ManagePeopleServlet" onsubmit="return confirm('Delete this dentist account and profile?');"><input type="hidden" name="action" value="deleteDentist"><input type="hidden" name="dentistId" value="<%=dentist.get("dentistId")%>"><button class="delete-button" type="submit">Delete</button></form></div></td></tr><%}}else{%><tr><td colspan="9" class="no-data">No dentist profiles are available.</td></tr><%}%>
    </tbody></table></div>
    <% } %>
    <a class="back-link" href="adminDashboard.jsp">← Back to Admin Dashboard</a>
</section></main></body></html>
