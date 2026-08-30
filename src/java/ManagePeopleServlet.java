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

@WebServlet("/ManagePeopleServlet")
public class ManagePeopleServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request.getSession(false))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String tab = normalizeTab(request.getParameter("tab"));
        request.setAttribute("activeTab", tab);

        String success = request.getParameter("success");
        if ("user_created".equals(success)) {
            request.setAttribute("success", "User account created successfully.");
        } else if ("dentist_created".equals(success)) {
            request.setAttribute(
                    "success",
                    "Dentist account, profile and schedule created successfully."
            );
        }

        loadPageData(request);
        request.getRequestDispatcher("/managePeople.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request.getSession(false))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String error;

        try {
            if ("createDentist".equals(action)) {
                request.setAttribute("activeTab", "dentists");
                error = createDentist(request);
                if (error == null) {
                    response.sendRedirect(
                            "ManagePeopleServlet?tab=dentists&success=dentist_created"
                    );
                    return;
                }
            } else if ("createUser".equals(action)) {
                request.setAttribute("activeTab", "users");
                error = createUser(request);
                if (error == null) {
                    response.sendRedirect(
                            "ManagePeopleServlet?tab=users&success=user_created"
                    );
                    return;
                }
            } else {
                error = "Invalid management action.";
                request.setAttribute("activeTab", "users");
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to create person account.", e);
            error = isDuplicate(e)
                    ? "An account already exists with this email address."
                    : "Unable to save the account. Please try again.";
        }

        request.setAttribute("error", error);
        loadPageData(request);
        request.getRequestDispatcher("/managePeople.jsp")
                .forward(request, response);
    }

    private String createUser(HttpServletRequest request)
            throws SQLException {

        PersonForm person = readPerson(request);
        String validation = validatePerson(person);
        if (validation != null) {
            return validation;
        }

        String role = clean(request.getParameter("role"));
        if (!"Admin".equalsIgnoreCase(role)
                && !"Cashier".equalsIgnoreCase(role)
                && !"Patient".equalsIgnoreCase(role)) {
            return "Select a valid user role.";
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (usernameExists(conn, person.username)) {
                return "An account already exists with this email address.";
            }
            insertUser(conn, person, role);
        }

        return null;
    }

    private String createDentist(HttpServletRequest request)
            throws SQLException {

        PersonForm person = readPerson(request);
        String validation = validatePerson(person);
        if (validation != null) {
            return validation;
        }

        String dentistName = clean(request.getParameter("dentistName"));
        String specialization = clean(request.getParameter("specialization"));
        String qualification = clean(request.getParameter("qualification"));
        String availableDay = clean(request.getParameter("availableDay"));
        String availableFrom = clean(request.getParameter("availableFrom"));
        String availableTo = clean(request.getParameter("availableTo"));
        String[] treatmentValues = request.getParameterValues("treatmentIds");

        BigDecimal consultationFee;
        try {
            consultationFee = new BigDecimal(
                    clean(request.getParameter("consultationFee"))
            );
        } catch (RuntimeException e) {
            return "Enter a valid consultation fee.";
        }

        if (dentistName == null || specialization == null
                || qualification == null || availableDay == null
                || availableFrom == null || availableTo == null) {
            return "Complete all dentist profile and schedule fields.";
        }
        if (!isWorkingDay(availableDay)) {
            return "Select a valid working day.";
        }
        if (availableFrom.compareTo(availableTo) >= 0) {
            return "Visiting start time must be before the end time.";
        }
        if (consultationFee.signum() < 0) {
            return "Consultation fee cannot be negative.";
        }
        if (treatmentValues == null || treatmentValues.length == 0) {
            return "Select at least one treatment for the dentist.";
        }

        List<Integer> treatmentIds = new ArrayList<>();
        try {
            for (String value : treatmentValues) {
                treatmentIds.add(Integer.valueOf(value));
            }
        } catch (NumberFormatException e) {
            return "Invalid treatment selection.";
        }

        /*
         * One transaction keeps the user account, dentist profile and
         * treatment assignments consistent. A failure rolls back all three.
         */
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (usernameExists(conn, person.username)) {
                    conn.rollback();
                    return "An account already exists with this email address.";
                }
                if (dentistNameExists(conn, dentistName)) {
                    conn.rollback();
                    return "A dentist profile already exists with this name.";
                }

                int userId = insertUser(conn, person, "Dentist");
                int dentistId = insertDentist(
                        conn,
                        userId,
                        dentistName,
                        specialization,
                        qualification,
                        consultationFee,
                        availableDay,
                        availableFrom,
                        availableTo
                );

                String treatmentSql =
                        "INSERT INTO dentist_treatments "
                        + "(dentist_id,treatment_id) VALUES (?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(treatmentSql)) {
                    for (Integer treatmentId : treatmentIds) {
                        stmt.setInt(1, dentistId);
                        stmt.setInt(2, treatmentId);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return null;
    }

    private int insertUser(
            Connection conn,
            PersonForm person,
            String role)
            throws SQLException {

        String sql =
                "INSERT INTO users "
                + "(first_name,last_name,username,phone_number,password,role) "
                + "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement stmt = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, person.firstName);
            stmt.setString(2, person.lastName);
            stmt.setString(3, person.username);
            stmt.setString(4, person.phoneNumber);
            stmt.setString(5, PasswordUtil.hash(person.password));
            stmt.setString(6, role);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("User ID was not generated.");
                }
                return keys.getInt(1);
            }
        }
    }

    private int insertDentist(
            Connection conn,
            int userId,
            String dentistName,
            String specialization,
            String qualification,
            BigDecimal fee,
            String day,
            String from,
            String to)
            throws SQLException {

        String sql =
                "INSERT INTO dentists "
                + "(user_id,dentist_name,specialization,qualification,"
                + "consultation_fee,available_day,available_from,available_to,status) "
                + "VALUES (?,?,?,?,?,?,?,?, 'Active')";

        try (PreparedStatement stmt = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, dentistName);
            stmt.setString(3, specialization);
            stmt.setString(4, qualification);
            stmt.setBigDecimal(5, fee);
            stmt.setString(6, day);
            stmt.setString(7, from);
            stmt.setString(8, to);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Dentist ID was not generated.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void loadPageData(HttpServletRequest request) {
        request.setAttribute("users", loadUsers(request));
        request.setAttribute("dentists", loadDentists(request));
        request.setAttribute("treatments", loadTreatments(request));
    }

    private List<Map<String, Object>> loadUsers(HttpServletRequest request) {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql =
                "SELECT user_id,first_name,last_name,username,phone_number,role "
                + "FROM users ORDER BY user_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("userId", rs.getInt("user_id"));
                row.put("name", escapeHtml(
                        rs.getString("first_name") + " " + rs.getString("last_name")
                ));
                row.put("username", escapeHtml(rs.getString("username")));
                row.put("phoneNumber", escapeHtml(rs.getString("phone_number")));
                row.put("role", escapeHtml(rs.getString("role")));
                users.add(row);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load users.", e);
            request.setAttribute("error", "Unable to load user accounts.");
        }
        return users;
    }

    private List<Map<String, Object>> loadDentists(HttpServletRequest request) {
        List<Map<String, Object>> dentists = new ArrayList<>();
        String sql =
                "SELECT d.dentist_id,d.dentist_name,d.specialization,"
                + "d.qualification,d.consultation_fee,d.available_day,"
                + "d.available_from,d.available_to,d.status,u.username,"
                + "GROUP_CONCAT(t.treatment_name ORDER BY t.treatment_name "
                + "SEPARATOR ', ') AS treatments "
                + "FROM dentists d LEFT JOIN users u ON d.user_id=u.user_id "
                + "LEFT JOIN dentist_treatments dt ON d.dentist_id=dt.dentist_id "
                + "LEFT JOIN treatments t ON dt.treatment_id=t.treatment_id "
                + "GROUP BY d.dentist_id,d.dentist_name,d.specialization,"
                + "d.qualification,d.consultation_fee,d.available_day,"
                + "d.available_from,d.available_to,d.status,u.username "
                + "ORDER BY d.dentist_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("dentistId", rs.getInt("dentist_id"));
                row.put("dentistName", safe(rs.getString("dentist_name")));
                row.put("specialization", safe(rs.getString("specialization")));
                row.put("qualification", safe(rs.getString("qualification")));
                row.put("consultationFee", rs.getBigDecimal("consultation_fee"));
                row.put("availableDay", safe(rs.getString("available_day")));
                row.put("availableFrom", rs.getTime("available_from"));
                row.put("availableTo", rs.getTime("available_to"));
                row.put("status", safe(rs.getString("status")));
                row.put("username", safe(rs.getString("username")));
                row.put("treatments", safe(rs.getString("treatments")));
                dentists.add(row);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load dentists.", e);
            request.setAttribute("error", "Unable to load dentist profiles.");
        }
        return dentists;
    }

    private List<Map<String, Object>> loadTreatments(HttpServletRequest request) {
        List<Map<String, Object>> treatments = new ArrayList<>();
        String sql =
                "SELECT treatment_id,treatment_name FROM treatments "
                + "ORDER BY treatment_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("treatmentId", rs.getInt("treatment_id"));
                row.put("treatmentName", escapeHtml(rs.getString("treatment_name")));
                treatments.add(row);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load treatments.", e);
            request.setAttribute("error", "Unable to load treatments.");
        }
        return treatments;
    }

    private boolean usernameExists(Connection conn, String username)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id FROM users WHERE username=?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean dentistNameExists(Connection conn, String name)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT dentist_id FROM dentists WHERE LOWER(dentist_name)=LOWER(?)")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private PersonForm readPerson(HttpServletRequest request) {
        return new PersonForm(
                clean(request.getParameter("firstName")),
                clean(request.getParameter("lastName")),
                clean(request.getParameter("username")),
                clean(request.getParameter("phoneNumber")),
                request.getParameter("password")
        );
    }

    private String validatePerson(PersonForm person) {
        if (person.firstName == null || person.lastName == null
                || person.username == null || person.phoneNumber == null
                || person.password == null || person.password.isBlank()) {
            return "Complete all account fields.";
        }
        if (!person.username.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Enter a valid email address.";
        }
        if (!person.phoneNumber.matches("\\d{10}")) {
            return "Phone number must contain exactly 10 digits.";
        }
        if (person.password.length() < 6) {
            return "Password must contain at least 6 characters.";
        }
        return null;
    }

    private boolean isWorkingDay(String day) {
        return "Monday".equals(day) || "Tuesday".equals(day)
                || "Wednesday".equals(day) || "Thursday".equals(day)
                || "Friday".equals(day) || "Saturday".equals(day);
    }

    private boolean isAdmin(HttpSession session) {
        return session != null
                && session.getAttribute("username") != null
                && "Admin".equalsIgnoreCase(
                        (String) session.getAttribute("role")
                );
    }

    private boolean isDuplicate(SQLException e) {
        return "23000".equals(e.getSQLState()) || e.getErrorCode() == 1062;
    }

    private String normalizeTab(String tab) {
        return "dentists".equalsIgnoreCase(tab) ? "dentists" : "users";
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safe(String value) {
        return value == null || value.isBlank()
                ? "Not configured" : escapeHtml(value);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static class PersonForm {
        private final String firstName;
        private final String lastName;
        private final String username;
        private final String phoneNumber;
        private final String password;

        PersonForm(
                String firstName,
                String lastName,
                String username,
                String phoneNumber,
                String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.phoneNumber = phoneNumber;
            this.password = password;
        }
    }
}
