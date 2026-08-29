import java.io.Serializable;

/**
 * Memento Pattern: immutable snapshot of the appointment fields that can be
 * edited. It exposes state for restoration without exposing mutation.
 */
public class AppointmentMemento implements Serializable {

    private final String appointmentNumber;
    private final int dentistId;
    private final int treatmentId;
    private final String appointmentDate;
    private final String appointmentTime;
    private final String status;

    public AppointmentMemento(
            String appointmentNumber,
            int dentistId,
            int treatmentId,
            String appointmentDate,
            String appointmentTime,
            String status) {

        this.appointmentNumber = appointmentNumber;
        this.dentistId = dentistId;
        this.treatmentId = treatmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public int getDentistId() {
        return dentistId;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public String getStatus() {
        return status;
    }
}
