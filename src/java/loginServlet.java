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

            stmt.setString(1, username.trim());
            stmt.setString(2, password);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                HttpSession session =
                        request.getSession();

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

                session.setAttribute(
                        "role",
                        rs.getString("role")
                );

                response.sendRedirect(
                        "dashboard.jsp"
                );

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