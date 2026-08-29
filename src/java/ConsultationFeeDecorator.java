/**
 * Concrete decorator that adds the selected dentist's consultation fee.
 */
public class ConsultationFeeDecorator extends ChargeDecorator {

    private final double consultationFee;

    public ConsultationFeeDecorator(
            BillCharge wrappedCharge,
            double consultationFee) {
        super(wrappedCharge);
        this.consultationFee = consultationFee;
    }

    @Override
    public double getAmount() {
        return wrappedCharge.getAmount() + consultationFee;
    }
}
