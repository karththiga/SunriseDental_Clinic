import java.math.BigDecimal;

/** Read-only checkout summary presented before payment authorization. */
public class PaymentSummary {
    public static final BigDecimal HOSPITAL_CHARGE = new BigDecimal("800.00");
    private final String patientName;
    private final String dentistName;
    private final String treatmentName;
    private final BigDecimal treatmentCost;

    public PaymentSummary(String patientName, String dentistName, String treatmentName,
            BigDecimal treatmentCost) {
        this.patientName = patientName;
        this.dentistName = dentistName;
        this.treatmentName = treatmentName;
        this.treatmentCost = treatmentCost;
    }

    public String getPatientName() { return patientName; }
    public String getDentistName() { return dentistName; }
    public String getTreatmentName() { return treatmentName; }
    public BigDecimal getTreatmentCost() { return treatmentCost; }
    public BigDecimal getHospitalCharge() { return HOSPITAL_CHARGE; }
    public BigDecimal getTotalAmount() { return treatmentCost.add(HOSPITAL_CHARGE); }
}
