

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

@WebServlet("/SignupServlet")
public class signupServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

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

        String confirmPassword =
                request.getParameter("confirmPassword");

        /*
         * Check required fields
         */
        if (firstName == null || firstName.trim().isEmpty()
                || lastName == null || lastName.trim().isEmpty()
                || username == null || username.trim().isEmpty()
                || phoneNumber == null || phoneNumber.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || confirmPassword == null
                || confirmPassword.trim().isEmpty()) {

            response.sendRedirect(
                    "signup.jsp?error=required"
            );

            return;
        }

        /*
         * Check password length
         */
        if (password.length() < 6) {

            response.sendRedirect(
                    "signup.jsp?error=length"
            );

            return;
        }

        /*
         * Check password confirmation
         */
        if (!password.equals(confirmPassword)) {

            response.sendRedirect(
                    "signup.jsp?error=password"
            );

            return;
        }

        try (
            Connection conn =
                    DBConnection.getConnection()
        ) {

            /*
             * Check whether email already exists
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

                    response.sendRedirect(
                            "signup.jsp?error=exists"
                    );

                    return;
                }
            }

            /*
             * Insert new user
             */
            String insertSql =
                    "INSERT INTO users "
                    + "(first_name, last_name, "
                    + "username, phone_number, "
                    + "password, role) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (
                PreparedStatement stmt =
                        conn.prepareStatement(insertSql)
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
                        "Staff"
                );

                int rows =
                        stmt.executeUpdate();

                if (rows > 0) {

                    response.sendRedirect(
                            "signup.jsp?success=true"
                    );

                } else {

                    response.sendRedirect(
                            "signup.jsp?error=failed"
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            response.sendRedirect(
                    "signup.jsp?error=database&param="+e.getMessage()
            );
        }
    }
}