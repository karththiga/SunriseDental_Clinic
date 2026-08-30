import java.math.BigDecimal;
import java.time.LocalTime;

/** Immutable schedule DTO shared by the web UI and JSON service. */
public class ScheduleEntry {
    private final int dentistId;
    private final String dentistName;
    private final String specialization;
    private final LocalTime availableFrom;
    private final LocalTime availableTo;
    private final BigDecimal consultationFee;
    private final int bookedSlots;
    private final int availableSlots;

    public ScheduleEntry(int dentistId, String dentistName, String specialization,
            LocalTime availableFrom, LocalTime availableTo, BigDecimal consultationFee,
            int bookedSlots, int availableSlots) {
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.specialization = specialization;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
        this.consultationFee = consultationFee;
        this.bookedSlots = bookedSlots;
        this.availableSlots = availableSlots;
    }

    public int getDentistId() { return dentistId; }
    public String getDentistName() { return dentistName; }
    public String getSpecialization() { return specialization; }
    public LocalTime getAvailableFrom() { return availableFrom; }
    public LocalTime getAvailableTo() { return availableTo; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public int getBookedSlots() { return bookedSlots; }
    public int getAvailableSlots() { return availableSlots; }
}
