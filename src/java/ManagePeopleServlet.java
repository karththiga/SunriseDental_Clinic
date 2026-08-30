import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Admin controller providing CRUD operations for user accounts and dentists. */
@WebServlet("/ManagePeopleServlet")
public class ManagePeopleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request.getSession(false))) {
            response.sendRedirect("login.jsp");
            return;
        }
        String tab = normalizeTab(request.getParameter("tab"));
        request.setAttribute("activeTab", tab);
        setSuccessMessage(request, request.getParameter("success"));
        String editId = clean(request.getParameter("edit"));
        if (editId != null) {
            if ("dentists".equals(tab)) loadDentistForEdit(request, editId);
            else loadUserForEdit(request, editId);
        }
        loadPageData(request);
        request.getRequestDispatcher("/managePeople.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect("login.jsp");
            return;
        }
        String action = clean(request.getParameter("action"));
        String tab = action != null && action.toLowerCase().contains("dentist") ? "dentists" : "users";
        request.setAttribute("activeTab", tab);
        String error;
        try {
            switch (action == null ? "" : action) {
                case "createUser":
                    error = saveUser(request, false);
                    if (error == null) { redirect(response, "users", "user_created"); return; }
                    preserveUserForm(request);
                    break;
                case "updateUser":
                    error = saveUser(request, true);
                    if (error == null) { redirect(response, "users", "user_updated"); return; }
                    preserveUserForm(request);
                    break;
                case "deleteUser":
                    error = deleteUser(request, session);
                    if (error == null) { redirect(response, "users", "user_deleted"); return; }
                    break;
                case "createDentist":
                    error = saveDentist(request, false);
                    if (error == null) { redirect(response, "dentists", "dentist_created"); return; }
                    preserveDentistForm(request);
                    break;
                case "updateDentist":
                    error = saveDentist(request, true);
                    if (error == null) { redirect(response, "dentists", "dentist_updated"); return; }
                    preserveDentistForm(request);
                    break;
                case "deleteDentist":
                    error = deleteDentist(request);
                    if (error == null) { redirect(response, "dentists", "dentist_deleted"); return; }
                    break;
                default:
                    error = "Invalid management action.";
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to manage clinic people.", e);
            error = isDuplicate(e) ? "That email address or dentist profile is already in use."
                    : "Unable to save the change. Please try again.";
        }
        request.setAttribute("error", error);
        loadPageData(request);
        request.getRequestDispatcher("/managePeople.jsp").forward(request, response);
    }

    private String saveUser(HttpServletRequest request, boolean update) throws SQLException {
        PersonForm person = readPerson(request);
        String error = validatePerson(person, update);
        if (error != null) return error;
        String role = clean(request.getParameter("role"));
        if (!isManagedUserRole(role)) return "Select a valid user role.";
        Integer userId = update ? integer(request.getParameter("userId")) : null;
        if (update && userId == null) return "Invalid user identifier.";

        try (Connection conn = DBConnection.getConnection()) {
            if (usernameExists(conn, person.username, userId)) return "That email address is already in use.";
            if (!update) {
                insertUser(conn, person, role);
                return null;
            }
            String currentRole = findUserRole(conn, userId);
            if (currentRole == null) return "The selected user no longer exists.";
            if ("Dentist".equalsIgnoreCase(currentRole)) return "Update dentists from the Dentist Profiles tab.";
            String sql = "UPDATE users SET first_name=?,last_name=?,username=?,phone_number=?,role=?"
                    + (hasText(person.password) ? ",password=?" : "") + " WHERE user_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int i = 1;
                stmt.setString(i++, person.firstName); stmt.setString(i++, person.lastName);
                stmt.setString(i++, person.username); stmt.setString(i++, person.phoneNumber); stmt.setString(i++, role);
                if (hasText(person.password)) stmt.setString(i++, PasswordUtil.hash(person.password));
                stmt.setInt(i, userId);
                if (stmt.executeUpdate() == 0) return "The selected user no longer exists.";
            }
        }
        return null;
    }

    private String deleteUser(HttpServletRequest request, HttpSession session) throws SQLException {
        Integer userId = integer(request.getParameter("userId"));
        if (userId == null) return "Invalid user identifier.";
        Object activeId = session.getAttribute("user_id");
        if (activeId instanceof Number && ((Number) activeId).intValue() == userId) {
            return "You cannot delete the administrator account currently in use.";
        }
        try (Connection conn = DBConnection.getConnection()) {
            String role = findUserRole(conn, userId);
            if (role == null) return "The selected user no longer exists.";
            if ("Dentist".equalsIgnoreCase(role)) return "Delete dentists from the Dentist Profiles tab.";
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id=?")) {
                stmt.setInt(1, userId); stmt.executeUpdate();
            }
        }
        return null;
    }

    private String saveDentist(HttpServletRequest request, boolean update) throws SQLException {
        PersonForm person = readPerson(request);
        String error = validatePerson(person, update);
        if (error != null) return error;
        DentistForm dentist = readDentist(request);
        error = validateDentist(dentist);
        if (error != null) return error;
        Integer dentistId = update ? integer(request.getParameter("dentistId")) : null;
        Integer userId = update ? integer(request.getParameter("userId")) : null;
        if (update && (dentistId == null || userId == null)) return "Invalid dentist identifier.";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (usernameExists(conn, person.username, userId)) {
                    conn.rollback(); return "That email address is already in use.";
                }
                if (dentistNameExists(conn, dentist.name, dentistId)) {
                    conn.rollback(); return "A dentist profile already exists with this name.";
                }
                if (!update) {
                    userId = insertUser(conn, person, "Dentist");
                    dentistId = insertDentist(conn, userId, dentist);
                } else {
                    if (!dentistBelongsToUser(conn, dentistId, userId)) {
                        conn.rollback(); return "The selected dentist no longer exists.";
                    }
                    updateDentistUser(conn, userId, person);
                    updateDentistProfile(conn, dentistId, dentist);
                    try (PreparedStatement clear = conn.prepareStatement(
                            "DELETE FROM dentist_treatments WHERE dentist_id=?")) {
                        clear.setInt(1, dentistId); clear.executeUpdate();
                    }
                }
                insertTreatmentAssignments(conn, dentistId, dentist.treatmentIds);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return null;
    }

    private String deleteDentist(HttpServletRequest request) throws SQLException {
        Integer dentistId = integer(request.getParameter("dentistId"));
        if (dentistId == null) return "Invalid dentist identifier.";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer userId;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM dentists WHERE dentist_id=?")) {
                    stmt.setInt(1, dentistId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return "The selected dentist no longer exists."; }
                        userId = (Integer) rs.getObject(1);
                    }
                }
                if (hasAppointment(conn, "dentist_id", dentistId)) {
                    conn.rollback();
                    return "This dentist has appointment history and cannot be deleted. Set the profile to Inactive instead.";
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM dentists WHERE dentist_id=?")) {
                    stmt.setInt(1, dentistId); stmt.executeUpdate();
                }
                if (userId != null) try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id=?")) {
                    stmt.setInt(1, userId); stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return null;
    }

    private void loadUserForEdit(HttpServletRequest request, String value) {
        Integer id = integer(value);
        if (id == null) { request.setAttribute("error", "Invalid user identifier."); return; }
        String sql = "SELECT user_id,first_name,last_name,username,phone_number,role FROM users WHERE user_id=? AND role<>'Dentist'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) { request.setAttribute("error", "User account not found."); return; }
                request.setAttribute("editUserId", rs.getString("user_id"));
                setPersonEditAttributes(request, rs);
                request.setAttribute("editRole", html(rs.getString("role")));
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load user for editing.", e);
            request.setAttribute("error", "Unable to load the selected user.");
        }
    }

    private void loadDentistForEdit(HttpServletRequest request, String value) {
        Integer id = integer(value);
        if (id == null) { request.setAttribute("error", "Invalid dentist identifier."); return; }
        String sql = "SELECT d.*,u.first_name,u.last_name,u.username,u.phone_number FROM dentists d "
                + "JOIN users u ON d.user_id=u.user_id WHERE d.dentist_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) { request.setAttribute("error", "Dentist profile not found."); return; }
                request.setAttribute("editDentistId", rs.getString("dentist_id"));
                request.setAttribute("editDentistUserId", rs.getString("user_id"));
                setPersonEditAttributes(request, rs);
                request.setAttribute("editDentistName", html(rs.getString("dentist_name")));
                request.setAttribute("editSpecialization", html(rs.getString("specialization")));
                request.setAttribute("editQualification", html(rs.getString("qualification")));
                request.setAttribute("editConsultationFee", rs.getString("consultation_fee"));
                request.setAttribute("editAvailableDay", html(rs.getString("available_day")));
                request.setAttribute("editAvailableFrom", timeText(rs.getString("available_from")));
                request.setAttribute("editAvailableTo", timeText(rs.getString("available_to")));
                request.setAttribute("editStatus", html(rs.getString("status")));
            }
            request.setAttribute("editTreatmentIds", loadTreatmentIds(conn, id));
        } catch (SQLException e) {
            getServletContext().log("Unable to load dentist for editing.", e);
            request.setAttribute("error", "Unable to load the selected dentist.");
        }
    }

    private void setPersonEditAttributes(HttpServletRequest request, ResultSet rs) throws SQLException {
        request.setAttribute("editFirstName", html(rs.getString("first_name")));
        request.setAttribute("editLastName", html(rs.getString("last_name")));
        request.setAttribute("editUsername", html(rs.getString("username")));
        request.setAttribute("editPhoneNumber", html(rs.getString("phone_number")));
    }

    private void preserveUserForm(HttpServletRequest request) {
        request.setAttribute("editUserId", clean(request.getParameter("userId")));
        preservePerson(request);
        request.setAttribute("editRole", html(request.getParameter("role")));
    }

    private void preserveDentistForm(HttpServletRequest request) {
        request.setAttribute("editDentistId", clean(request.getParameter("dentistId")));
        request.setAttribute("editDentistUserId", clean(request.getParameter("userId")));
        preservePerson(request);
        request.setAttribute("editDentistName", html(request.getParameter("dentistName")));
        request.setAttribute("editSpecialization", html(request.getParameter("specialization")));
        request.setAttribute("editQualification", html(request.getParameter("qualification")));
        request.setAttribute("editConsultationFee", html(request.getParameter("consultationFee")));
        request.setAttribute("editAvailableDay", html(request.getParameter("availableDay")));
        request.setAttribute("editAvailableFrom", html(request.getParameter("availableFrom")));
        request.setAttribute("editAvailableTo", html(request.getParameter("availableTo")));
        request.setAttribute("editStatus", html(request.getParameter("status")));
        request.setAttribute("editTreatmentIds", request.getParameterValues("treatmentIds"));
    }

    private void preservePerson(HttpServletRequest request) {
        request.setAttribute("editFirstName", html(request.getParameter("firstName")));
        request.setAttribute("editLastName", html(request.getParameter("lastName")));
        request.setAttribute("editUsername", html(request.getParameter("username")));
        request.setAttribute("editPhoneNumber", html(request.getParameter("phoneNumber")));
    }

    private void loadPageData(HttpServletRequest request) {
        request.setAttribute("users", loadUsers(request));
        request.setAttribute("dentists", loadDentists(request));
        request.setAttribute("treatments", loadTreatments(request));
    }

    private List<Map<String, Object>> loadUsers(HttpServletRequest request) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT user_id,first_name,last_name,username,phone_number,role FROM users WHERE role<>'Dentist' ORDER BY user_id DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("userId", rs.getInt("user_id"));
                row.put("name", html(rs.getString("first_name") + " " + rs.getString("last_name")));
                row.put("username", html(rs.getString("username")));
                row.put("phoneNumber", html(rs.getString("phone_number")));
                row.put("role", html(rs.getString("role")));
                rows.add(row);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load users.", e);
            request.setAttribute("error", "Unable to load user accounts.");
        }
        return rows;
    }

    private List<Map<String, Object>> loadDentists(HttpServletRequest request) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT d.dentist_id,d.dentist_name,d.specialization,d.qualification,d.consultation_fee,"
                + "d.available_day,d.available_from,d.available_to,d.status,u.username,"
                + "GROUP_CONCAT(t.treatment_name ORDER BY t.treatment_name SEPARATOR ', ') treatments "
                + "FROM dentists d LEFT JOIN users u ON d.user_id=u.user_id "
                + "LEFT JOIN dentist_treatments dt ON d.dentist_id=dt.dentist_id "
                + "LEFT JOIN treatments t ON dt.treatment_id=t.treatment_id "
                + "GROUP BY d.dentist_id,d.dentist_name,d.specialization,d.qualification,d.consultation_fee,"
                + "d.available_day,d.available_from,d.available_to,d.status,u.username ORDER BY d.dentist_name";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("dentistId", rs.getInt("dentist_id")); row.put("dentistName", safe(rs.getString("dentist_name")));
                row.put("specialization", safe(rs.getString("specialization"))); row.put("qualification", safe(rs.getString("qualification")));
                row.put("consultationFee", rs.getBigDecimal("consultation_fee")); row.put("availableDay", safe(rs.getString("available_day")));
                row.put("availableFrom", timeText(rs.getString("available_from"))); row.put("availableTo", timeText(rs.getString("available_to")));
                row.put("status", safe(rs.getString("status"))); row.put("username", safe(rs.getString("username")));
                row.put("treatments", safe(rs.getString("treatments"))); rows.add(row);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load dentists.", e);
            request.setAttribute("error", "Unable to load dentist profiles.");
        }
        return rows;
    }

    private List<Map<String, Object>> loadTreatments(HttpServletRequest request) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT treatment_id,treatment_name FROM treatments ORDER BY treatment_name"); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("treatmentId", rs.getInt(1)); row.put("treatmentName", html(rs.getString(2))); rows.add(row);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load treatments.", e);
            request.setAttribute("error", "Unable to load treatments.");
        }
        return rows;
    }

    private int insertUser(Connection conn, PersonForm p, String role) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (first_name,last_name,username,phone_number,password,role) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.firstName); stmt.setString(2, p.lastName); stmt.setString(3, p.username);
            stmt.setString(4, p.phoneNumber); stmt.setString(5, PasswordUtil.hash(p.password)); stmt.setString(6, role);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("User ID was not generated.");
                return keys.getInt(1);
            }
        }
    }

    private int insertDentist(Connection conn, int userId, DentistForm d) throws SQLException {
        String sql = "INSERT INTO dentists (user_id,dentist_name,specialization,qualification,consultation_fee,"
                + "available_day,available_from,available_to,status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId); stmt.setString(2, d.name); stmt.setString(3, d.specialization); stmt.setString(4, d.qualification);
            stmt.setBigDecimal(5, d.fee); stmt.setString(6, d.day); stmt.setString(7, d.from); stmt.setString(8, d.to);
            stmt.setString(9, d.status); stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Dentist ID was not generated.");
                return keys.getInt(1);
            }
        }
    }

    private void updateDentistUser(Connection conn, int userId, PersonForm p) throws SQLException {
        String sql = "UPDATE users SET first_name=?,last_name=?,username=?,phone_number=?,role='Dentist'"
                + (hasText(p.password) ? ",password=?" : "") + " WHERE user_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int i = 1; stmt.setString(i++, p.firstName); stmt.setString(i++, p.lastName); stmt.setString(i++, p.username);
            stmt.setString(i++, p.phoneNumber); if (hasText(p.password)) stmt.setString(i++, PasswordUtil.hash(p.password));
            stmt.setInt(i, userId); stmt.executeUpdate();
        }
    }

    private void updateDentistProfile(Connection conn, int dentistId, DentistForm d) throws SQLException {
        String sql = "UPDATE dentists SET dentist_name=?,specialization=?,qualification=?,consultation_fee=?,"
                + "available_day=?,available_from=?,available_to=?,status=? WHERE dentist_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.name); stmt.setString(2, d.specialization); stmt.setString(3, d.qualification);
            stmt.setBigDecimal(4, d.fee); stmt.setString(5, d.day); stmt.setString(6, d.from); stmt.setString(7, d.to);
            stmt.setString(8, d.status); stmt.setInt(9, dentistId); stmt.executeUpdate();
        }
    }

    private void insertTreatmentAssignments(Connection conn, int dentistId, List<Integer> ids) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO dentist_treatments (dentist_id,treatment_id) VALUES (?,?)")) {
            for (Integer id : ids) { stmt.setInt(1, dentistId); stmt.setInt(2, id); stmt.addBatch(); }
            stmt.executeBatch();
        }
    }

    private PersonForm readPerson(HttpServletRequest request) {
        return new PersonForm(clean(request.getParameter("firstName")), clean(request.getParameter("lastName")),
                clean(request.getParameter("username")), clean(request.getParameter("phoneNumber")), request.getParameter("password"));
    }

    private DentistForm readDentist(HttpServletRequest request) {
        BigDecimal fee = null;
        try { fee = new BigDecimal(clean(request.getParameter("consultationFee"))); } catch (RuntimeException ignored) { }
        List<Integer> ids = new ArrayList<>();
        String[] values = request.getParameterValues("treatmentIds");
        if (values != null) for (String value : values) { Integer id = integer(value); if (id != null) ids.add(id); }
        return new DentistForm(clean(request.getParameter("dentistName")), clean(request.getParameter("specialization")),
                clean(request.getParameter("qualification")), fee, clean(request.getParameter("availableDay")),
                clean(request.getParameter("availableFrom")), clean(request.getParameter("availableTo")),
                clean(request.getParameter("status")), ids);
    }

    private String validatePerson(PersonForm p, boolean update) {
        if (p.firstName == null || p.lastName == null || p.username == null || p.phoneNumber == null
                || (!update && !hasText(p.password))) return "Complete all required account fields.";
        if (!p.username.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) return "Enter a valid email address.";
        if (!p.phoneNumber.matches("\\d{10}")) return "Phone number must contain exactly 10 digits.";
        if (hasText(p.password) && p.password.length() < 6) return "Password must contain at least 6 characters.";
        return null;
    }

    private String validateDentist(DentistForm d) {
        if (d.name == null || d.specialization == null || d.qualification == null || d.fee == null || d.day == null
                || d.from == null || d.to == null) return "Complete all dentist profile fields.";
        if (!isWorkingDay(d.day)) return "Select a valid working day.";
        if (d.from.compareTo(d.to) >= 0) return "Visiting start time must be before the end time.";
        if (d.fee.signum() < 0) return "Consultation fee cannot be negative.";
        if (!"Active".equals(d.status) && !"Inactive".equals(d.status)) return "Select a valid profile status.";
        if (d.treatmentIds.isEmpty()) return "Select at least one treatment for the dentist.";
        return null;
    }

    private boolean usernameExists(Connection conn, String username, Integer excluded) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE LOWER(username)=LOWER(?)" + (excluded == null ? "" : " AND user_id<>?");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username); if (excluded != null) stmt.setInt(2, excluded);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private boolean dentistNameExists(Connection conn, String name, Integer excluded) throws SQLException {
        String sql = "SELECT dentist_id FROM dentists WHERE LOWER(dentist_name)=LOWER(?)"
                + (excluded == null ? "" : " AND dentist_id<>?");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name); if (excluded != null) stmt.setInt(2, excluded);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private String findUserRole(Connection conn, int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE user_id=?")) {
            stmt.setInt(1, id); try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    private boolean dentistBelongsToUser(Connection conn, int dentistId, int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM dentists WHERE dentist_id=? AND user_id=?")) {
            stmt.setInt(1, dentistId); stmt.setInt(2, userId); try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private boolean hasAppointment(Connection conn, String column, int id) throws SQLException {
        String safeColumn = "dentist_id".equals(column) ? "dentist_id" : "patient_user_id";
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM appointments WHERE " + safeColumn + "=? LIMIT 1")) {
            stmt.setInt(1, id); try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private List<String> loadTreatmentIds(Connection conn, int dentistId) throws SQLException {
        List<String> ids = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT treatment_id FROM dentist_treatments WHERE dentist_id=?")) {
            stmt.setInt(1, dentistId); try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) ids.add(rs.getString(1)); }
        }
        return ids;
    }

    private void setSuccessMessage(HttpServletRequest request, String code) {
        Map<String, String> messages = new HashMap<>();
        messages.put("user_created", "User account created successfully."); messages.put("user_updated", "User account updated successfully.");
        messages.put("user_deleted", "User account deleted successfully."); messages.put("dentist_created", "Dentist account and profile created successfully.");
        messages.put("dentist_updated", "Dentist account and profile updated successfully.");
        messages.put("dentist_deleted", "Dentist account and profile deleted successfully.");
        if (messages.containsKey(code)) request.setAttribute("success", messages.get(code));
    }

    private void redirect(HttpServletResponse response, String tab, String success) throws IOException {
        response.sendRedirect("ManagePeopleServlet?tab=" + tab + "&success=" + success);
    }
    private boolean isManagedUserRole(String role) { return "Admin".equals(role) || "Staff".equals(role)
            || "Cashier".equals(role) || "Patient".equals(role); }
    private boolean isWorkingDay(String day) { return "Monday".equals(day) || "Tuesday".equals(day) || "Wednesday".equals(day)
            || "Thursday".equals(day) || "Friday".equals(day) || "Saturday".equals(day); }
    private boolean isAdmin(HttpSession session) { return session != null && session.getAttribute("username") != null
            && "Admin".equalsIgnoreCase((String) session.getAttribute("role")); }
    private boolean isDuplicate(SQLException e) { return "23000".equals(e.getSQLState()) || e.getErrorCode() == 1062; }
    private String normalizeTab(String tab) { return "dentists".equalsIgnoreCase(tab) ? "dentists" : "users"; }
    private Integer integer(String value) { try { return value == null ? null : Integer.valueOf(value); }
        catch (NumberFormatException e) { return null; } }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
    private String safe(String value) { return value == null || value.isBlank() ? "Not configured" : html(value); }
    private String timeText(String value) { return value == null ? "" : value.length() >= 5 ? value.substring(0, 5) : value; }
    private String html(String value) { if (value == null) return ""; return value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }

    private static class PersonForm {
        final String firstName, lastName, username, phoneNumber, password;
        PersonForm(String f, String l, String u, String p, String pass) {
            firstName = f; lastName = l; username = u; phoneNumber = p; password = pass;
        }
    }
    private static class DentistForm {
        final String name, specialization, qualification, day, from, to, status;
        final BigDecimal fee; final List<Integer> treatmentIds;
        DentistForm(String n, String s, String q, BigDecimal f, String d, String fromValue, String toValue,
                String state, List<Integer> treatments) {
            name = n; specialization = s; qualification = q; fee = f; day = d; from = fromValue; to = toValue;
            status = state == null ? "Active" : state; treatmentIds = treatments;
        }
    }
}
