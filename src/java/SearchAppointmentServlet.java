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

@WebServlet("/SearchAppointmentServlet")
public class SearchAppointmentServlet
        extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Check login session
         */
        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("username") == null) {

            response.sendRedirect("login.jsp");

            return;
        }

        /*
         * Get appointment number
         */
        String appointmentNumber =
                request.getParameter(
                        "appointmentNumber"
                );

        if (appointmentNumber == null
                || appointmentNumber.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Please enter an appointment number."
            );

            request.getRequestDispatcher(
                    "searchAppointment.jsp"
            ).forward(request, response);

            return;
        }

        /*
         * Search query
         */
        String sql =
                "SELECT "
                + "a.appointment_number, "
                + "a.patient_name, "
                + "a.address, "
                + "a.contact_number, "
                + "d.dentist_name, "
                + "t.treatment_name, "
                + "t.treatment_cost, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "a.status "
                + "FROM appointments a "
                + "INNER JOIN dentists d "
                + "ON a.dentist_id = d.dentist_id "
                + "INNER JOIN treatments t "
                + "ON a.treatment_id = t.treatment_id "
                + "WHERE a.appointment_number = ?";

        /*
         * Singleton Design Pattern
         */
        DBConnection database =
                DBConnection.getInstance();

        try (
            Connection conn =
                    database.getDatabaseConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    appointmentNumber.trim()
            );

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                request.setAttribute(
                        "appointmentNumber",
                        rs.getString(
                                "appointment_number"
                        )
                );

                request.setAttribute(
                        "patientName",
                        rs.getString(
                                "patient_name"
                        )
                );

                request.setAttribute(
                        "address",
                        rs.getString(
                                "address"
                        )
                );

                request.setAttribute(
                        "contactNumber",
                        rs.getString(
                                "contact_number"
                        )
                );

                request.setAttribute(
                        "dentistName",
                        rs.getString(
                                "dentist_name"
                        )
                );

                request.setAttribute(
                        "treatmentName",
                        rs.getString(
                                "treatment_name"
                        )
                );

                request.setAttribute(
                        "treatmentCost",
                        rs.getBigDecimal(
                                "treatment_cost"
                        )
                );

                request.setAttribute(
                        "appointmentDate",
                        rs.getDate(
                                "appointment_date"
                        )
                );

                request.setAttribute(
                        "appointmentTime",
                        rs.getTime(
                                "appointment_time"
                        )
                );

                request.setAttribute(
                        "status",
                        rs.getString(
                                "status"
                        )
                );

                request.setAttribute(
                        "found",
                        true
                );

            } else {

                request.setAttribute(
                        "error",
                        "No appointment found with number "
                        + appointmentNumber
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Database error. Unable to search appointment."
            );
        }

        request.getRequestDispatcher(
                "searchAppointment.jsp"
        ).forward(request, response);
    }
}