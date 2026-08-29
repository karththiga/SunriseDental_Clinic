/**
 * Appointment domain object.
 *
 * Builder Pattern: appointment registration contains several related fields.
 * The builder makes their construction readable and prevents partially
 * populated objects from being changed after validation begins.
 */
public class Appointment {

    private final String appointmentNumber;
    private final String patientName;
    private final String address;
    private final String contactNumber;
    private final int dentistId;
    private final int treatmentId;
    private final String appointmentDate;
    private final String appointmentTime;

    private Appointment(Builder builder) {
        this.appointmentNumber = builder.appointmentNumber;
        this.patientName = builder.patientName;
        this.address = builder.address;
        this.contactNumber = builder.contactNumber;
        this.dentistId = builder.dentistId;
        this.treatmentId = builder.treatmentId;
        this.appointmentDate = builder.appointmentDate;
        this.appointmentTime = builder.appointmentTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNumber() {
        return contactNumber;
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

    public static class Builder {

        private String appointmentNumber;
        private String patientName;
        private String address;
        private String contactNumber;
        private int dentistId;
        private int treatmentId;
        private String appointmentDate;
        private String appointmentTime;

        public Builder appointmentNumber(String appointmentNumber) {
            this.appointmentNumber = appointmentNumber;
            return this;
        }

        public Builder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder contactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
            return this;
        }

        public Builder dentistId(int dentistId) {
            this.dentistId = dentistId;
            return this;
        }

        public Builder treatmentId(int treatmentId) {
            this.treatmentId = treatmentId;
            return this;
        }

        public Builder appointmentDate(String appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public Builder appointmentTime(String appointmentTime) {
            this.appointmentTime = appointmentTime;
            return this;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }
}
