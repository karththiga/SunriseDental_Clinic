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

@WebServlet("/ManageDentistsServlet")
public class ManageDentistsServlet extends HttpServlet {

    /*
     * GET = Display dentists
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

        loadDentists(request);

        request.getRequestDispatcher(
                "manageDentists.jsp"
        ).forward(request, response);
    }


    /*
     * POST = Add dentist
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


        String dentistName =
                request.getParameter("dentistName");


        /*
         * Required field validation
         */
        if (dentistName == null
                || dentistName.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Please enter the dentist name."
            );

            loadDentists(request);

            request.getRequestDispatcher(
                    "manageDentists.jsp"
            ).forward(request, response);

            return;
        }


        try (
            Connection conn =
                    DBConnection.getConnection()
        ) {

            /*
             * Check duplicate dentist
             */
            String checkSql =
                    "SELECT dentist_id "
                    + "FROM dentists "
                    + "WHERE LOWER(dentist_name) = LOWER(?)";


            try (
                PreparedStatement checkStmt =
                        conn.prepareStatement(checkSql)
            ) {

                checkStmt.setString(
                        1,
                        dentistName.trim()
                );

                ResultSet rs =
                        checkStmt.executeQuery();


                if (rs.next()) {

                    request.setAttribute(
                            "error",
                            "This dentist already exists."
                    );

                    loadDentists(request);

                    request.getRequestDispatcher(
                            "manageDentists.jsp"
                    ).forward(request, response);

                    return;
                }
            }


            /*
             * Insert dentist
             */
            String insertSql =
                    "INSERT INTO dentists "
                    + "(dentist_name) "
                    + "VALUES (?)";


            try (
                PreparedStatement stmt =
                        conn.prepareStatement(insertSql)
            ) {

                stmt.setString(
                        1,
                        dentistName.trim()
                );

                stmt.executeUpdate();
            }


            request.setAttribute(
                    "success",
                    "Dentist added successfully."
            );


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to add dentist."
            );
        }


        loadDentists(request);

        request.getRequestDispatcher(
                "manageDentists.jsp"
        ).forward(request, response);
    }


    /*
     * Load all dentists from database
     */
    private void loadDentists(
            HttpServletRequest request) {

        List<Map<String, Object>> dentists =
                new ArrayList<>();


        String sql =
                "SELECT dentist_id, dentist_name "
                + "FROM dentists "
                + "ORDER BY dentist_name";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    stmt.executeQuery()
        ) {

            while (rs.next()) {

                Map<String, Object> dentist =
                        new HashMap<>();

                dentist.put(
                        "dentistId",
                        rs.getInt("dentist_id")
                );

                dentist.put(
                        "dentistName",
                        rs.getString("dentist_name")
                );

                dentists.add(dentist);
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load dentists."
            );
        }


        request.setAttribute(
                "dentists",
                dentists
        );
    }
}