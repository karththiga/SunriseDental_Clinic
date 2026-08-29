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


@WebServlet("/ManageAppointmentRequestsServlet")
public class ManageAppointmentRequestsServlet
        extends HttpServlet {


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {

            response.sendRedirect("login.jsp");
            return;
        }

        loadPendingRequests(request);

        request.getRequestDispatcher(
                "manageAppointmentRequests.jsp"
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

        if (!isAdmin(request)) {

            response.sendRedirect("login.jsp");
            return;
        }


        String appointmentIdValue =
                request.getParameter("appointmentId");


        if (appointmentIdValue == null
                || appointmentIdValue.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Invalid appointment request."
            );

            loadPendingRequests(request);

            request.getRequestDispatcher(
                    "manageAppointmentRequests.jsp"
            ).forward(
                    request,
                    response
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

            request.setAttribute(
                    "error",
                    "Invalid appointment request."
            );

            loadPendingRequests(request);

            request.getRequestDispatcher(
                    "manageAppointmentRequests.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        try {

            String appointmentNumber =
                    String.format(
                            "APT%04d",
                            appointmentId
                    );


            String sql =
                    "UPDATE appointments "
                    + "SET appointment_number = ? "
                    + "WHERE appointment_id = ? "
                    + "AND appointment_number IS NULL "
                    + "AND status = 'Pending'";


            try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
            ) {

                stmt.setString(
                        1,
                        appointmentNumber
                );

                stmt.setInt(
                        2,
                        appointmentId
                );


                int rows =
                        stmt.executeUpdate();


                if (rows > 0) {

                    request.setAttribute(
                            "success",
                            "Appointment assigned successfully. "
                            + "Appointment Number: "
                            + appointmentNumber
                    );

                } else {

                    request.setAttribute(
                            "error",
                            "Appointment may already have been assigned."
                    );
                }
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Database error while assigning appointment."
            );
        }


        loadPendingRequests(request);


        request.getRequestDispatcher(
                "manageAppointmentRequests.jsp"
        ).forward(
                request,
                response
        );
    }


    private boolean isAdmin(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);


        return session != null
                && session.getAttribute("username") != null
                && "Admin".equalsIgnoreCase(
                        (String) session.getAttribute("role")
                );
    }


    private void loadPendingRequests(
            HttpServletRequest request) {


        List<Map<String, Object>> requests =
                new ArrayList<>();


        String sql =
                "SELECT "
                + "a.appointment_id, "
                + "a.appointment_number, "
                + "a.patient_name, "
                + "a.contact_number, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "a.status, "
                + "t.treatment_name, "
                + "d.dentist_name "
                + "FROM appointments a "
                + "LEFT JOIN treatments t "
                + "ON a.treatment_id = t.treatment_id "
                + "LEFT JOIN dentists d "
                + "ON a.dentist_id = d.dentist_id "
                + "WHERE a.status = 'Pending' "
                + "AND a.appointment_number IS NULL "
                + "ORDER BY a.appointment_date, "
                + "a.appointment_time";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    stmt.executeQuery()
        ) {


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
                        "contactNumber",
                        rs.getString(
                                "contact_number"
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


                row.put(
                        "treatmentName",
                        rs.getString(
                                "treatment_name"
                        )
                );


                row.put(
                        "dentistName",
                        rs.getString(
                                "dentist_name"
                        )
                );


                requests.add(row);
            }


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to load appointment requests."
            );
        }


        request.setAttribute(
                "requests",
                requests
        );
    }
}