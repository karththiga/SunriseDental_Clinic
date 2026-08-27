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


@WebServlet("/DentistAppointmentsServlet")
public class DentistAppointmentsServlet extends HttpServlet {


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);


        if (session == null
                || session.getAttribute("username") == null
                || !"Dentist".equalsIgnoreCase(
                        (String) session.getAttribute("role"))) {

            response.sendRedirect("login.jsp");
            return;
        }


        int userId =
                (Integer) session.getAttribute("user_id");


        try {

            Integer dentistId =
                    getDentistId(userId);


            if (dentistId == null) {

                request.setAttribute(
                        "error",
                        "Dentist profile not found."
                );

            } else {

                loadAssignedAppointments(
                        dentistId,
                        request
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load appointments."
            );
        }


        request.getRequestDispatcher(
                "dentistAppointments.jsp"
        ).forward(
                request,
                response
        );
    }



    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);


        if (session == null
                || session.getAttribute("username") == null
                || !"Dentist".equalsIgnoreCase(
                        (String) session.getAttribute("role"))) {

            response.sendRedirect("login.jsp");
            return;
        }


        String appointmentIdValue =
                request.getParameter("appointmentId");

        String action =
                request.getParameter("action");


        if (appointmentIdValue == null
                || action == null) {

            response.sendRedirect(
                    "DentistAppointmentsServlet"
            );

            return;
        }


        int appointmentId;


        try {

            appointmentId =
                    Integer.parseInt(
                            appointmentIdValue
                    );

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    "DentistAppointmentsServlet"
            );

            return;
        }


        int userId =
                (Integer) session.getAttribute("user_id");


        try {

            Integer dentistId =
                    getDentistId(userId);


            if (dentistId == null) {

                request.setAttribute(
                        "error",
                        "Dentist profile not found."
                );

            } else {

                String newStatus;


                if ("confirm".equalsIgnoreCase(action)) {

                    newStatus =
                            "Confirmed";

                } else if ("reject".equalsIgnoreCase(action)) {

                    newStatus =
                            "Rejected";

                } else {

                    request.setAttribute(
                            "error",
                            "Invalid action."
                    );

                    loadAssignedAppointments(
                            dentistId,
                            request
                    );

                    request.getRequestDispatcher(
                            "dentistAppointments.jsp"
                    ).forward(
                            request,
                            response
                    );

                    return;
                }


                String sql =
                        "UPDATE appointments "
                        + "SET status = ? "
                        + "WHERE appointment_id = ? "
                        + "AND dentist_id = ? "
                        + "AND status = 'Pending'";


                try (
                    Connection conn =
                            DBConnection.getConnection();

                    PreparedStatement stmt =
                            conn.prepareStatement(sql)
                ) {

                    stmt.setString(
                            1,
                            newStatus
                    );

                    stmt.setInt(
                            2,
                            appointmentId
                    );

                    stmt.setInt(
                            3,
                            dentistId
                    );


                    int rows =
                            stmt.executeUpdate();


                    if (rows > 0) {

                        if ("Confirmed".equals(newStatus)) {

                            request.setAttribute(
                                    "success",
                                    "Appointment confirmed successfully."
                            );

                        } else {

                            request.setAttribute(
                                    "success",
                                    "Appointment rejected successfully."
                            );
                        }

                    } else {

                        request.setAttribute(
                                "error",
                                "Unable to update appointment."
                        );
                    }
                }


                loadAssignedAppointments(
                        dentistId,
                        request
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Database error while updating appointment."
            );
        }


        request.getRequestDispatcher(
                "dentistAppointments.jsp"
        ).forward(
                request,
                response
        );
    }



    /*
     * Find dentist profile connected
     * to the logged-in user.
     */
    private Integer getDentistId(
            int userId)
            throws SQLException {


        String sql =
                "SELECT dentist_id "
                + "FROM dentists "
                + "WHERE user_id = ?";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    userId
            );


            ResultSet rs =
                    stmt.executeQuery();


            if (rs.next()) {

                return rs.getInt(
                        "dentist_id"
                );
            }
        }


        return null;
    }



    /*
     * Load Pending appointments
     * assigned to this dentist.
     */
    private void loadAssignedAppointments(
            int dentistId,
            HttpServletRequest request)
            throws SQLException {


        List<Map<String, Object>> appointments =
                new ArrayList<>();


        String sql =
                "SELECT "
                + "a.appointment_id, "
                + "a.appointment_number, "
                + "a.patient_name, "
                + "a.address, "
                + "a.contact_number, "
                + "t.treatment_name, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "a.status "
                + "FROM appointments a "
                + "LEFT JOIN treatments t "
                + "ON a.treatment_id = t.treatment_id "
                + "WHERE a.dentist_id = ? "
                + "AND a.status = 'Pending' "
                + "AND a.appointment_number IS NOT NULL "
                + "ORDER BY a.appointment_date, "
                + "a.appointment_time";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    dentistId
            );


            ResultSet rs =
                    stmt.executeQuery();


            while (rs.next()) {

                Map<String, Object> row =
                        new HashMap<>();


                row.put(
                        "appointmentId",
                        rs.getInt(
                                "appointment_id"
                        )
                );


                row.put(
                        "appointmentNumber",
                        rs.getString(
                                "appointment_number"
                        )
                );


                row.put(
                        "patientName",
                        rs.getString(
                                "patient_name"
                        )
                );


                row.put(
                        "address",
                        rs.getString(
                                "address"
                        )
                );


                row.put(
                        "contactNumber",
                        rs.getString(
                                "contact_number"
                        )
                );


                row.put(
                        "treatmentName",
                        rs.getString(
                                "treatment_name"
                        )
                );


                row.put(
                        "appointmentDate",
                        rs.getDate(
                                "appointment_date"
                        )
                );


                row.put(
                        "appointmentTime",
                        rs.getTime(
                                "appointment_time"
                        )
                );


                row.put(
                        "status",
                        rs.getString(
                                "status"
                        )
                );


                appointments.add(row);
            }
        }


        request.setAttribute(
                "appointments",
                appointments
        );
    }
}