/**
 * Factory Pattern: owns construction of the standard appointment validation
 * pipeline. Servlets ask for a ready-to-use validator instead of knowing which
 * concrete handlers exist or how they must be ordered.
 */
public final class AppointmentValidationFactory {

    private AppointmentValidationFactory() {
        // Utility factory; instances are unnecessary.
    }

    public static AppointmentValidationHandler createRegistrationChain() {
        AppointmentValidationHandler required = new RequiredFieldHandler();
        AppointmentValidationHandler phone = new PhoneValidationHandler();
        AppointmentValidationHandler conflict = new AppointmentConflictHandler();

        required.setNext(phone).setNext(conflict);
        return required;
    }
}
