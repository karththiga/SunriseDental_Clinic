import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Read-only report model passed from the service layer to the JSP. */
public class ClinicReport {
    private int totalAppointments;
    private int todaysAppointments;
    private int upcomingAppointments;
    private int confirmedAppointments;
    private int paidBills;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private final List<MetricRow> dentistWorkload = new ArrayList<>();
    private final List<MetricRow> treatmentPopularity = new ArrayList<>();
    private final List<MetricRow> dailyAppointments = new ArrayList<>();

    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int value) { totalAppointments = value; }
    public int getTodaysAppointments() { return todaysAppointments; }
    public void setTodaysAppointments(int value) { todaysAppointments = value; }
    public int getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(int value) { upcomingAppointments = value; }
    public int getConfirmedAppointments() { return confirmedAppointments; }
    public void setConfirmedAppointments(int value) { confirmedAppointments = value; }
    public int getPaidBills() { return paidBills; }
    public void setPaidBills(int value) { paidBills = value; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal value) { totalRevenue = value == null ? BigDecimal.ZERO : value; }
    public List<MetricRow> getDentistWorkload() { return dentistWorkload; }
    public List<MetricRow> getTreatmentPopularity() { return treatmentPopularity; }
    public List<MetricRow> getDailyAppointments() { return dailyAppointments; }

    public static class MetricRow {
        private final String label;
        private final int count;
        public MetricRow(String label, int count) { this.label = label; this.count = count; }
        public String getLabel() { return label; }
        public int getCount() { return count; }
    }
}
