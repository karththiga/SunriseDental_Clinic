



public class RequiredFieldHandler
        extends AppointmentValidationHandler {

    @Override
    public String validate(Appointment appointment) {

        if (appointment.getAppointmentNumber() == null
                || appointment.getAppointmentNumber().trim().isEmpty()
                || appointment.getPatientName() == null
                || appointment.getPatientName().trim().isEmpty()
                || appointment.getAddress() == null
                || appointment.getAddress().trim().isEmpty()
                || appointment.getContactNumber() == null
                || appointment.getContactNumber().trim().isEmpty()
                || appointment.getAppointmentDate() == null
                || appointment.getAppointmentDate().trim().isEmpty()
                || appointment.getAppointmentTime() == null
                || appointment.getAppointmentTime().trim().isEmpty()) {

            return "Please fill in all required fields.";
        }

        if (nextHandler != null) {
            return nextHandler.validate(appointment);
        }

        return null;
    }
}