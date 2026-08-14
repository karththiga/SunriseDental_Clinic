
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/LoginServlet"})
public class loginServlet extends HttpServlet {

    private static final String JDBC_URL
            = "jdbc:mysql://127.0.0.1:3306/HospitalManagement";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "";

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        System.out.println("============================");

        String username
                = request.getParameter("username");

        String password
                = request.getParameter("password");

        response.setContentType("text/html;charset=UTF-8");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=driver"
            );

            return;
        }

        try {

            Connection conn = DriverManager.getConnection(
                    JDBC_URL,
                    JDBC_USER,
                    JDBC_PASS
            );

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM `user` WHERE username=? AND password=?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                HttpSession session = request.getSession();

                session.setAttribute("username", username);

                response.sendRedirect(
                        request.getContextPath() + "/HomePage.jsp"
                );

                return;
            } else {
                response.sendRedirect(
                        request.getContextPath() + "/login.jsp?error=invalid"
                );

                return;
            }

        } catch (SQLException ex) {
            Logger.getLogger(loginServlet.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.out.println("============================");
            System.out.println("DATABASE CONNECTION ERROR");
            System.out.println("Message: " + ex.getMessage());
            System.out.println("SQL State: " + ex.getSQLState());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("============================");

            ex.printStackTrace();

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp?error=database&errorMessage="+ex.getMessage()
            );

        }

    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.sendRedirect(
                request.getContextPath() + "/login.jsp"
        );
    }
}
