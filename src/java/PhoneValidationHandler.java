



public class PhoneValidationHandler
        extends AppointmentValidationHandler {

    @Override
    public String validate(Appointment appointment) {

        String phone =
                appointment.getContactNumber();

        if (!phone.matches("\\d{10}")) {

            return "Contact number must contain exactly 10 digits.";
        }

        if (nextHandler != null) {
            return nextHandler.validate(appointment);
        }

        return null;
    }
}