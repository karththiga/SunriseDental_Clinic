import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

@WebServlet("/ManageUsersServlet")
public class ManageUsersServlet extends HttpServlet {

    /*
     * GET = Display all users
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("username") == null
                || !"Admin".equalsIgnoreCase(
                        (String) session.getAttribute("role"))) {

            response.sendRedirect("login.jsp");
            return;
        }

        loadUsers(request);

        request.getRequestDispatcher(
                "manageUsers.jsp"
        ).forward(request, response);
    }


    /*
     * POST = Add new user
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("username") == null
                || !"Admin".equalsIgnoreCase(
                        (String) session.getAttribute("role"))) {

            response.sendRedirect("login.jsp");
            return;
        }


        String firstName =
                request.getParameter("firstName");

        String lastName =
                request.getParameter("lastName");

        String username =
                request.getParameter("username");

        String phoneNumber =
                request.getParameter("phoneNumber");

        String password =
                request.getParameter("password");

        String role =
                request.getParameter("role");


        /*
         * Required field validation
         */
        if (firstName == null || firstName.trim().isEmpty()
                || lastName == null || lastName.trim().isEmpty()
                || username == null || username.trim().isEmpty()
                || phoneNumber == null || phoneNumber.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || role == null || role.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Please fill in all fields."
            );

            loadUsers(request);

            request.getRequestDispatcher(
                    "manageUsers.jsp"
            ).forward(request, response);

            return;
        }


        /*
         * Password validation
         */
        if (password.length() < 6) {

            request.setAttribute(
                    "error",
                    "Password must contain at least 6 characters."
            );

            loadUsers(request);

            request.getRequestDispatcher(
                    "manageUsers.jsp"
            ).forward(request, response);

            return;
        }


        /*
         * Phone validation
         */
        if (!phoneNumber.trim().matches("\\d{10}")) {

            request.setAttribute(
                    "error",
                    "Phone number must contain exactly 10 digits."
            );

            loadUsers(request);

            request.getRequestDispatcher(
                    "manageUsers.jsp"
            ).forward(request, response);

            return;
        }


        /*
         * Validate allowed roles
         */
        if (!role.equalsIgnoreCase("Admin")
                && !role.equalsIgnoreCase("Dentist")
                && !role.equalsIgnoreCase("Cashier")
                && !role.equalsIgnoreCase("Patient")) {

            request.setAttribute(
                    "error",
                    "Invalid user role."
            );

            loadUsers(request);

            request.getRequestDispatcher(
                    "manageUsers.jsp"
            ).forward(request, response);

            return;
        }


        try (
            Connection conn =
                    DBConnection.getConnection()
        ) {

            /*
             * Check duplicate email
             */
            String checkSql =
                    "SELECT user_id FROM users "
                    + "WHERE username = ?";


            try (
                PreparedStatement checkStmt =
                        conn.prepareStatement(checkSql)
            ) {

                checkStmt.setString(
                        1,
                        username.trim()
                );

                ResultSet rs =
                        checkStmt.executeQuery();


                if (rs.next()) {

                    request.setAttribute(
                            "error",
                            "A user already exists with this email address."
                    );

                    loadUsers(request);

                    request.getRequestDispatcher(
                            "manageUsers.jsp"
                    ).forward(request, response);

                    return;
                }
            }


            /*
             * Insert user
             */
            String sql =
                    "INSERT INTO users "
                    + "(first_name, last_name, username, "
                    + "phone_number, password, role) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";


            try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
            ) {

                stmt.setString(
                        1,
                        firstName.trim()
                );

                stmt.setString(
                        2,
                        lastName.trim()
                );

                stmt.setString(
                        3,
                        username.trim()
                );

                stmt.setString(
                        4,
                        phoneNumber.trim()
                );

                stmt.setString(
                        5,
                        password
                );

                stmt.setString(
                        6,
                        role
                );

                stmt.executeUpdate();
            }


            request.setAttribute(
                    "success",
                    "User created successfully."
            );


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to create user."
            );
        }


        loadUsers(request);

        request.getRequestDispatcher(
                "manageUsers.jsp"
        ).forward(request, response);
    }


    /*
     * Load all users
     */
    private void loadUsers(
            HttpServletRequest request) {

        List<Map<String, Object>> users =
                new ArrayList<>();


        String sql =
                "SELECT user_id, first_name, last_name, "
                + "username, phone_number, role "
                + "FROM users "
                + "ORDER BY user_id DESC";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    stmt.executeQuery()
        ) {


            while (rs.next()) {

                Map<String, Object> user =
                        new HashMap<>();


                user.put(
                        "userId",
                        rs.getInt("user_id")
                );

                user.put(
                        "firstName",
                        rs.getString("first_name")
                );

                user.put(
                        "lastName",
                        rs.getString("last_name")
                );

                user.put(
                        "username",
                        rs.getString("username")
                );

                user.put(
                        "phoneNumber",
                        rs.getString("phone_number")
                );

                user.put(
                        "role",
                        rs.getString("role")
                );


                users.add(user);
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load users."
            );
        }


        request.setAttribute(
                "users",
                users
        );
    }
}