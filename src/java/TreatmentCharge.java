/**
 * Concrete component representing the base treatment price.
 */
public class TreatmentCharge implements BillCharge {

    private final double treatmentCost;

    public TreatmentCharge(double treatmentCost) {
        this.treatmentCost = treatmentCost;
    }

    @Override
    public double getAmount() {
        return treatmentCost;
    }
}
