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
                    "login.jsp?error=required"
            );

            return;
        }

        String sql =
                "SELECT * FROM users "
                + "WHERE username = ? AND password = ?";

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

            stmt.setString(
                    2,
                    password
            );

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

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

                } else {

                    // Unknown role
                    session.invalidate();

                    response.sendRedirect(
                            "login.jsp?error=invalidrole"
                    );
                }

            } else {

                response.sendRedirect(
                        "login.jsp?error=invalid"
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            response.sendRedirect(
                    "login.jsp?error=database"
            );
        }
    }
}