import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AppointmentServlet")
public class AppointmentServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int dentistId;
        int treatmentId;

        try {
            dentistId = Integer.parseInt(request.getParameter("dentistId"));
            treatmentId = Integer.parseInt(request.getParameter("treatmentId"));

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    "registerAppointment.jsp"
                    + "?error=Please+select+a+dentist+and+treatment."
            );

            return;
        }

        /*
         * Builder Pattern: assemble the multi-field domain object in one
         * readable expression before it enters validation and persistence.
         */
        Appointment appointment = Appointment.builder()
                .appointmentNumber(request.getParameter("appointmentNumber"))
                .patientName(request.getParameter("patientName"))
                .address(request.getParameter("address"))
                .contactNumber(request.getParameter("contactNumber"))
                .dentistId(dentistId)
                .treatmentId(treatmentId)
                .appointmentDate(request.getParameter("appointmentDate"))
                .appointmentTime(request.getParameter("appointmentTime"))
                .build();

        /*
         * Factory + Chain of Responsibility:
         * the factory owns handler creation/order; this servlet only invokes
         * the returned validation pipeline.
         */
        AppointmentValidationHandler validationChain =
                AppointmentValidationFactory.createRegistrationChain();

        String validationError =
                validationChain.validate(appointment);

        if (validationError != null) {

            String encodedError =
                    URLEncoder.encode(
                            validationError,
                            StandardCharsets.UTF_8
                    );

            response.sendRedirect(
                    "registerAppointment.jsp"
                    + "?error="
                    + encodedError
            );

            return;
        }

        String sql =
                "INSERT INTO appointments "
                + "(appointment_number, patient_name, "
                + "address, contact_number, "
                + "dentist_id, treatment_id, "
                + "appointment_date, appointment_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    appointment.getAppointmentNumber()
            );

            stmt.setString(
                    2,
                    appointment.getPatientName()
            );

            stmt.setString(
                    3,
                    appointment.getAddress()
            );

            stmt.setString(
                    4,
                    appointment.getContactNumber()
            );

            stmt.setInt(
                    5,
                    appointment.getDentistId()
            );

            stmt.setInt(
                    6,
                    appointment.getTreatmentId()
            );

            stmt.setString(
                    7,
                    appointment.getAppointmentDate()
            );

            stmt.setString(
                    8,
                    appointment.getAppointmentTime()
            );

            stmt.executeUpdate();

            response.sendRedirect(
                    "registerAppointment.jsp?success=true"
            );

        } catch (SQLException e) {

            e.printStackTrace();

            String message =
                    "Unable to save appointment.";

            if (e.getMessage() != null
                    && e.getMessage().contains(
                            "Duplicate entry"
                    )) {

                message =
                        "Appointment number already exists.";
            }

            response.sendRedirect(
                    "registerAppointment.jsp?error="
                    + URLEncoder.encode(
                            message,
                            StandardCharsets.UTF_8
                    )
            );
        }
    }
}
