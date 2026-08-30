


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppointmentConflictHandler
        extends AppointmentValidationHandler {

    @Override
    public String validate(Appointment appointment) {

        String sql =
                "SELECT appointment_id "
                + "FROM appointments "
                + "WHERE dentist_id = ? "
                + "AND appointment_date = ? "
                + "AND appointment_time = ? "
                + "AND status NOT IN ('Rejected','Cancelled')";

        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    appointment.getDentistId()
            );

            stmt.setString(
                    2,
                    appointment.getAppointmentDate()
            );

            stmt.setString(
                    3,
                    appointment.getAppointmentTime()
            );

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                return "This dentist already has "
                        + "an appointment at this date and time.";
            }

        } catch (SQLException e) {

            e.printStackTrace();

            return "Unable to check dentist availability.";
        }

        if (nextHandler != null) {
            return nextHandler.validate(appointment);
        }

        return null;
    }
}
