public class BillResult {

    private int appointmentId;

    private String appointmentNumber;

    private String patientName;

    private String contactNumber;

    private String dentistName;

    private String treatmentName;

    private String appointmentDate;

    private String appointmentTime;

    private double treatmentCost;

    private double consultationFee;

    private double totalAmount;


    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(
            int appointmentId) {

        this.appointmentId =
                appointmentId;
    }


    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(
            String appointmentNumber) {

        this.appointmentNumber =
                appointmentNumber;
    }


    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(
            String patientName) {

        this.patientName =
                patientName;
    }


    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(
            String contactNumber) {

        this.contactNumber =
                contactNumber;
    }


    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(
            String dentistName) {

        this.dentistName =
                dentistName;
    }


    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(
            String treatmentName) {

        this.treatmentName =
                treatmentName;
    }


    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(
            String appointmentDate) {

        this.appointmentDate =
                appointmentDate;
    }


    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(
            String appointmentTime) {

        this.appointmentTime =
                appointmentTime;
    }


    public double getTreatmentCost() {
        return treatmentCost;
    }

    public void setTreatmentCost(
            double treatmentCost) {

        this.treatmentCost =
                treatmentCost;
    }


    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(
            double consultationFee) {

        this.consultationFee =
                consultationFee;
    }


    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            double totalAmount) {

        this.totalAmount =
                totalAmount;
    }
}