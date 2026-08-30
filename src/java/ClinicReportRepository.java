import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Data-access layer for aggregate clinic reporting queries. */
public class ClinicReportRepository {

    public ClinicReport loadReport() throws SQLException {
        ClinicReport report = new ClinicReport();
        try (Connection conn = DBConnection.getConnection()) {
            loadAppointmentSummary(conn, report);
            loadBillingSummary(conn, report);
            loadMetric(conn, report.getDentistWorkload(),
                    "SELECT d.dentist_name AS label,COUNT(a.appointment_id) AS total "
                    + "FROM dentists d LEFT JOIN appointments a ON d.dentist_id=a.dentist_id "
                    + "AND a.appointment_date>=CURRENT_DATE AND a.status NOT IN ('Rejected','Cancelled') "
                    + "GROUP BY d.dentist_id,d.dentist_name ORDER BY total DESC,d.dentist_name");
            loadMetric(conn, report.getTreatmentPopularity(),
                    "SELECT t.treatment_name AS label,COUNT(a.appointment_id) AS total "
                    + "FROM treatments t LEFT JOIN appointments a ON t.treatment_id=a.treatment_id "
                    + "GROUP BY t.treatment_id,t.treatment_name ORDER BY total DESC,t.treatment_name");
            loadMetric(conn, report.getDailyAppointments(),
                    "SELECT DATE_FORMAT(appointment_date,'%d %b %Y') AS label,COUNT(*) AS total "
                    + "FROM appointments WHERE appointment_date BETWEEN CURRENT_DATE-INTERVAL 6 DAY AND CURRENT_DATE "
                    + "GROUP BY appointment_date ORDER BY appointment_date");
        }
        return report;
    }

    private void loadAppointmentSummary(Connection conn, ClinicReport report) throws SQLException {
        String sql = "SELECT COUNT(*) total," 
                + "SUM(appointment_date=CURRENT_DATE) today_total," 
                + "SUM(appointment_date>=CURRENT_DATE AND status NOT IN ('Rejected','Cancelled')) upcoming_total," 
                + "SUM(status='Confirmed') confirmed_total FROM appointments";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                report.setTotalAppointments(rs.getInt("total"));
                report.setTodaysAppointments(rs.getInt("today_total"));
                report.setUpcomingAppointments(rs.getInt("upcoming_total"));
                report.setConfirmedAppointments(rs.getInt("confirmed_total"));
            }
        }
    }

    private void loadBillingSummary(Connection conn, ClinicReport report) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) paid_bills,COALESCE(SUM(total_amount),0) revenue FROM "
                + "(SELECT appointment_id,MAX(total_amount) total_amount FROM bills "
                + "WHERE payment_status='Paid' GROUP BY appointment_id) paid");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                report.setPaidBills(rs.getInt("paid_bills"));
                report.setTotalRevenue(rs.getBigDecimal("revenue"));
            }
        }
    }

    private void loadMetric(Connection conn, java.util.List<ClinicReport.MetricRow> target, String sql)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                target.add(new ClinicReport.MetricRow(rs.getString("label"), rs.getInt("total")));
            }
        }
    }
}
