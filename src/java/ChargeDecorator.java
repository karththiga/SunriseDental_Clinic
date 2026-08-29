/**
 * Decorator Pattern: delegates to an existing charge and allows another
 * billing component to be added without changing the base treatment charge.
 */
public abstract class ChargeDecorator implements BillCharge {

    protected final BillCharge wrappedCharge;

    protected ChargeDecorator(BillCharge wrappedCharge) {
        this.wrappedCharge = wrappedCharge;
    }
}
