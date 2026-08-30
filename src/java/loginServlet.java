import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class loginServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String username =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        // Check empty fields
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.sendRedirect(
                    "HomeServlet?auth=login&error=required"
            );

            return;
        }

        String sql =
                "SELECT * FROM users "
                + "WHERE username = ?";

        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    username.trim()
            );

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next() && PasswordUtil.verify(password, rs.getString("password"))) {

                if (PasswordUtil.needsUpgrade(rs.getString("password"))) {
                    try (PreparedStatement upgrade = conn.prepareStatement(
                            "UPDATE users SET password=? WHERE user_id=?")) {
                        upgrade.setString(1, PasswordUtil.hash(password));
                        upgrade.setInt(2, rs.getInt("user_id"));
                        upgrade.executeUpdate();
                    }
                }

                HttpSession session =
                        request.getSession();

                // Store user details in session
                session.setAttribute(
                        "user_id",
                        rs.getInt("user_id")
                );

                session.setAttribute(
                        "username",
                        rs.getString("username")
                );

                session.setAttribute(
                        "first_name",
                        rs.getString("first_name")
                );

                session.setAttribute(
                        "last_name",
                        rs.getString("last_name")
                );

                String role =
                        rs.getString("role");

                session.setAttribute(
                        "role",
                        role
                );

                // Redirect according to role
                if ("Admin".equalsIgnoreCase(role)) {

                    response.sendRedirect(
                            "adminDashboard.jsp"
                    );

                } else if ("Dentist".equalsIgnoreCase(role)) {

                    response.sendRedirect(
                            "dentistDashboard.jsp"
                    );

                } else if ("Cashier".equalsIgnoreCase(role)) {

                    response.sendRedirect(
                            "cashierDashboard.jsp"
                    );

                } else if ("Patient".equalsIgnoreCase(role)) {

                    response.sendRedirect(
                            "patientDashboard.jsp"
                    );

                } else if ("Staff".equalsIgnoreCase(role)) {

                    response.sendRedirect(
                            "dashboard.jsp"
                    );

                } else {

                    // Unknown role
                    session.invalidate();

                    response.sendRedirect(
                            "HomeServlet?auth=login&error=invalidrole"
                    );
                }

            } else {

                response.sendRedirect(
                        "HomeServlet?auth=login&error=invalid"
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            response.sendRedirect(
                    "HomeServlet?auth=login&error=database"
            );
        }
    }
}
