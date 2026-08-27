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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/UpdateAppointmentServlet")
public class UpdateAppointmentServlet
        extends HttpServlet {

    /*
     * GET
     *
     * Used for:
     * 1. Loading appointment
     * 2. Undo operation
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("username") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        String action =
                request.getParameter("action");

        if ("undo".equals(action)) {

            undoAppointment(
                    request,
                    response,
                    session
            );

            return;
        }

        String appointmentNumber =
                request.getParameter(
                        "appointmentNumber"
                );

        if (appointmentNumber != null
                && !appointmentNumber.trim().isEmpty()) {

            try {
                loadAppointment(
                        appointmentNumber.trim(),
                        request
                );
            } catch (SQLException ex) {
                Logger.getLogger(UpdateAppointmentServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        request.getRequestDispatcher(
                "updateAppointment.jsp"
        ).forward(
                request,
                response
        );
    }


    /*
     * POST
     *
     * Used for updating appointment.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("username") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        String appointmentNumber =
                request.getParameter(
                        "appointmentNumber"
                );

        int dentistId;
        int treatmentId;

        try {

            dentistId =
                    Integer.parseInt(
                            request.getParameter(
                                    "dentistId"
                            )
                    );

            treatmentId =
                    Integer.parseInt(
                            request.getParameter(
                                    "treatmentId"
                            )
                    );

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    "updateAppointment.jsp"
            );

            return;
        }

        String appointmentDate =
                request.getParameter(
                        "appointmentDate"
                );

        String appointmentTime =
                request.getParameter(
                        "appointmentTime"
                );

        String status =
                request.getParameter(
                        "status"
                );


        try {

            /*
             * STEP 1
             *
             * Save existing appointment
             * before changing it.
             */
            AppointmentMemento oldState =
                    getCurrentState(
                            appointmentNumber
                    );

            if (oldState == null) {

                request.setAttribute(
                        "error",
                        "Appointment not found."
                );

                request.getRequestDispatcher(
                        "updateAppointment.jsp"
                ).forward(
                        request,
                        response
                );

                return;
            }


            /*
             * STEP 2
             *
             * Caretaker keeps Memento.
             */
            AppointmentCaretaker caretaker =
                    new AppointmentCaretaker();

            caretaker.save(oldState);

            session.setAttribute(
                    "appointmentCaretaker",
                    caretaker
            );


            /*
             * STEP 3
             *
             * Update database.
             */
            String sql =
                    "UPDATE appointments "
                    + "SET dentist_id=?, "
                    + "treatment_id=?, "
                    + "appointment_date=?, "
                    + "appointment_time=?, "
                    + "status=? "
                    + "WHERE appointment_number=?";

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

                stmt.setInt(
                        2,
                        treatmentId
                );

                stmt.setString(
                        3,
                        appointmentDate
                );

                stmt.setString(
                        4,
                        appointmentTime
                );

                stmt.setString(
                        5,
                        status
                );

                stmt.setString(
                        6,
                        appointmentNumber
                );

                stmt.executeUpdate();
            }


            request.setAttribute(
                    "success",
                    "Appointment updated successfully."
            );

            request.setAttribute(
                    "canUndo",
                    true
            );

            loadAppointment(
                    appointmentNumber,
                    request
            );


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to update appointment."
            );
        }


        request.getRequestDispatcher(
                "updateAppointment.jsp"
        ).forward(
                request,
                response
        );
    }


    /*
     * Read current appointment and
     * create Memento.
     */
    private AppointmentMemento getCurrentState(
            String appointmentNumber)
            throws SQLException {

        String sql =
                "SELECT dentist_id, "
                + "treatment_id, "
                + "appointment_date, "
                + "appointment_time, "
                + "status "
                + "FROM appointments "
                + "WHERE appointment_number=?";

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

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                return new AppointmentMemento(

                        appointmentNumber,

                        rs.getInt(
                                "dentist_id"
                        ),

                        rs.getInt(
                                "treatment_id"
                        ),

                        rs.getString(
                                "appointment_date"
                        ),

                        rs.getString(
                                "appointment_time"
                        ),

                        rs.getString(
                                "status"
                        )
                );
            }
        }

        return null;
    }


    /*
     * Restore saved Memento.
     */
    private void undoAppointment(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session)
            throws ServletException, IOException {

        AppointmentCaretaker caretaker =
                (AppointmentCaretaker)
                session.getAttribute(
                        "appointmentCaretaker"
                );

        if (caretaker == null
                || !caretaker.hasSavedState()) {

            request.setAttribute(
                    "error",
                    "There is no previous update to undo."
            );

            request.getRequestDispatcher(
                    "updateAppointment.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        AppointmentMemento oldState =
                caretaker.getSavedState();


        String sql =
                "UPDATE appointments "
                + "SET dentist_id=?, "
                + "treatment_id=?, "
                + "appointment_date=?, "
                + "appointment_time=?, "
                + "status=? "
                + "WHERE appointment_number=?";


        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    oldState.getDentistId()
            );

            stmt.setInt(
                    2,
                    oldState.getTreatmentId()
            );

            stmt.setString(
                    3,
                    oldState.getAppointmentDate()
            );

            stmt.setString(
                    4,
                    oldState.getAppointmentTime()
            );

            stmt.setString(
                    5,
                    oldState.getStatus()
            );

            stmt.setString(
                    6,
                    oldState.getAppointmentNumber()
            );

            stmt.executeUpdate();


            caretaker.clear();

            request.setAttribute(
                    "success",
                    "Previous appointment details restored successfully."
            );

            loadAppointment(
                    oldState.getAppointmentNumber(),
                    request
            );


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to restore appointment."
            );
        }


        request.getRequestDispatcher(
                "updateAppointment.jsp"
        ).forward(
                request,
                response
        );
    }


    /*
     * Load appointment information
     * for the JSP.
     */
    private void loadAppointment(
            String appointmentNumber,
            HttpServletRequest request)
            throws SQLException {

        String sql =
                "SELECT "
                + "a.appointment_number, "
                + "a.patient_name, "
                + "a.address, "
                + "a.contact_number, "
                + "a.dentist_id, "
                + "a.treatment_id, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "a.status "
                + "FROM appointments a "
                + "WHERE a.appointment_number=?";


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

            ResultSet rs =
                    stmt.executeQuery();


            if (rs.next()) {

                request.setAttribute(
                        "found",
                        true
                );

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
                        "dentistId",
                        rs.getInt(
                                "dentist_id"
                        )
                );

                request.setAttribute(
                        "treatmentId",
                        rs.getInt(
                                "treatment_id"
                        )
                );

                request.setAttribute(
                        "appointmentDate",
                        rs.getString(
                                "appointment_date"
                        )
                );

                request.setAttribute(
                        "appointmentTime",
                        rs.getString(
                                "appointment_time"
                        )
                );

                request.setAttribute(
                        "status",
                        rs.getString(
                                "status"
                        )
                );

            } else {

                request.setAttribute(
                        "error",
                        "Appointment not found."
                );
            }
        }
    }
}