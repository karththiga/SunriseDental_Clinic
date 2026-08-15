



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