import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Short-lived server-side checkout state; no card details are stored here. */
public class PendingReservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final long CHECKOUT_MINUTES = 15;

    private final String checkoutToken;
    private final int patientUserId;
    private final int treatmentId;
    private final int dentistId;
    private final LocalDate appointmentDate;
    private final LocalTime appointmentTime;
    private final Instant createdAt;

    public PendingReservation(int patientUserId, int treatmentId, int dentistId,
            LocalDate appointmentDate, LocalTime appointmentTime) {
        this.checkoutToken = UUID.randomUUID().toString();
        this.patientUserId = patientUserId;
        this.treatmentId = treatmentId;
        this.dentistId = dentistId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.createdAt = Instant.now();
    }

    public String getCheckoutToken() { return checkoutToken; }
    public int getPatientUserId() { return patientUserId; }
    public int getTreatmentId() { return treatmentId; }
    public int getDentistId() { return dentistId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public boolean isExpired() {
        return createdAt.plus(CHECKOUT_MINUTES, ChronoUnit.MINUTES).isBefore(Instant.now());
    }
}
