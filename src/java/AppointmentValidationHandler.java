



/**
 * Chain of Responsibility Pattern: every validator handles one concern and
 * delegates to the next handler only when its own validation succeeds.
 */
public abstract class AppointmentValidationHandler {

    protected AppointmentValidationHandler nextHandler;

    public AppointmentValidationHandler setNext(
            AppointmentValidationHandler nextHandler) {

        this.nextHandler = nextHandler;

        return nextHandler;
    }

    public abstract String validate(
            Appointment appointment);
}
