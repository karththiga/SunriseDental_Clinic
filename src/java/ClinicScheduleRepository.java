import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Persistence layer for dentist schedules and live booking counts. */
public class ClinicScheduleRepository {
    public List<ScheduleEntry> findByDate(LocalDate date) throws SQLException {
        List<ScheduleEntry> schedule = new ArrayList<>();
        String sql = "SELECT d.dentist_id,d.dentist_name,d.specialization,da.available_from,"
                + "da.available_to,d.consultation_fee,COUNT(a.appointment_id) booked_slots "
                + "FROM dentists d JOIN dentist_availability da ON da.dentist_id=d.dentist_id AND da.is_active=1 LEFT JOIN appointments a ON a.dentist_id=d.dentist_id "
                + "AND a.appointment_date=? AND a.status NOT IN ('Rejected','Cancelled') "
                + "WHERE d.status='Active' AND da.day_of_week=? "
                + "AND NOT EXISTS (SELECT 1 FROM dentist_leaves dl WHERE dl.dentist_id=d.dentist_id AND dl.leave_date=?) "
                + "GROUP BY d.dentist_id,d.dentist_name,d.specialization,da.available_from,"
                + "da.available_to,d.consultation_fee ORDER BY da.available_from,d.dentist_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, date.toString());
            stmt.setString(2, date.getDayOfWeek().name());
            stmt.setString(3, date.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalTime from = rs.getTime("available_from").toLocalTime();
                    LocalTime to = rs.getTime("available_to").toLocalTime();
                    int booked = rs.getInt("booked_slots");
                    int capacity = Math.max(0, (int) (Duration.between(from, to).toMinutes() / 30));
                    schedule.add(new ScheduleEntry(
                            rs.getInt("dentist_id"), rs.getString("dentist_name"),
                            rs.getString("specialization"), from, to,
                            rs.getBigDecimal("consultation_fee"), booked,
                            Math.max(0, capacity - booked)));
                }
            }
        }
        return schedule;
    }
}
