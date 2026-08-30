import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dependency-free automated regression suite for core business rules.
 * Run with scripts/run-tests.sh locally or through the GitHub Actions workflow.
 */
public class AutomatedTestSuite {
    private static int passed;

    public static void main(String[] args) throws Exception {
        testAppointmentBuilder();
        testRequiredFieldValidation();
        testPhoneValidationRejectsInvalidNumber();
        testPhoneValidationAcceptsTenDigits();
        testDecoratorCalculatesBillTotal();
        testPasswordHashing();
        testScheduleServiceRejectsOutOfRangeDate();
        System.out.println("All " + passed + " automated tests passed.");
    }

    private static Appointment validAppointment() {
        return Appointment.builder().appointmentNumber("APT-TEST-001")
                .patientName("Test Patient").address("Colombo")
                .contactNumber("0771234567").dentistId(1).treatmentId(1)
                .appointmentDate("2026-09-10").appointmentTime("10:00").build();
    }

    private static void testAppointmentBuilder() {
        Appointment item = validAppointment();
        check("APT-TEST-001".equals(item.getAppointmentNumber()), "Builder stores appointment number");
        check(item.getDentistId() == 1 && item.getTreatmentId() == 1, "Builder stores linked IDs");
    }

    private static void testRequiredFieldValidation() {
        Appointment incomplete = Appointment.builder().patientName("Patient").build();
        String result = new RequiredFieldHandler().validate(incomplete);
        check("Please fill in all required fields.".equals(result), "Required validator rejects incomplete data");
    }

    private static void testPhoneValidationRejectsInvalidNumber() {
        Appointment invalid = Appointment.builder().appointmentNumber("APT-1")
                .patientName("Patient").address("Colombo").contactNumber("077-12")
                .appointmentDate("2026-09-10").appointmentTime("10:00").build();
        check(new PhoneValidationHandler().validate(invalid) != null,
                "Phone validator rejects non-10-digit input");
    }

    private static void testPhoneValidationAcceptsTenDigits() {
        check(new PhoneValidationHandler().validate(validAppointment()) == null,
                "Phone validator accepts exactly 10 digits");
    }

    private static void testDecoratorCalculatesBillTotal() {
        BillCharge charge = new ConsultationFeeDecorator(
                new TreatmentCharge(6500.00), 2500.00);
        check(Math.abs(charge.getAmount() - 9000.00) < 0.001,
                "Billing decorator adds treatment and consultation fees");
    }

    private static void testPasswordHashing() {
        String hash = PasswordUtil.hash("Dental#123");
        check(!"Dental#123".equals(hash), "Password is not stored as plaintext");
        check(PasswordUtil.verify("Dental#123", hash), "Correct password verifies against PBKDF2 hash");
        check(!PasswordUtil.verify("incorrect", hash), "Incorrect password is rejected");
    }

    private static void testScheduleServiceRejectsOutOfRangeDate() throws Exception {
        ClinicScheduleService service = new ClinicScheduleService(new ClinicScheduleRepository());
        boolean rejected = false;
        try {
            service.getSchedule(LocalDate.now().plusYears(2));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "Schedule service rejects unsupported dates before database access");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAILED: " + name);
        passed++;
        System.out.println("PASS: " + name);
    }
}
