import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

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
        testDummyPaymentGateway();
        testDummyRefundGateway();
        testOnlinePaymentBreakdown();
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

    private static void testDummyPaymentGateway() {
        DummyPaymentGateway gateway = new DummyPaymentGateway();
        String expiry = YearMonth.now().plusYears(2)
                .format(DateTimeFormatter.ofPattern("MM/yy"));
        DummyPaymentGateway.Authorization result = gateway.authorize(
                "Test Patient", "4242 4242 4242 4242", expiry, "123",
                new BigDecimal("9000.00"));
        check(result.getReference().startsWith("PAY-"),
                "Dummy gateway returns a payment reference");
        check("4242".equals(result.getLastFour()),
                "Dummy gateway retains only the final four card digits");
        boolean rejected = false;
        try {
            gateway.authorize("Test Patient", "1111", expiry, "123",
                    new BigDecimal("9000.00"));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "Dummy gateway rejects an invalid card number");
    }

    private static void testDummyRefundGateway() {
        DummyPaymentGateway.Refund refund = new DummyPaymentGateway().refund(
                "PAY-TEST123", new BigDecimal("18800.00"));
        check(refund.getReference().startsWith("REF-"),
                "Dummy gateway returns a refund reference");
        check(new BigDecimal("18800.00").compareTo(refund.getAmount()) == 0,
                "Dummy gateway refunds the full paid amount");
    }

    private static void testOnlinePaymentBreakdown() {
        PaymentSummary summary = new PaymentSummary("Patient", "Dentist",
                "Root Canal", new BigDecimal("18000.00"));
        check(new BigDecimal("800.00").compareTo(summary.getHospitalCharge()) == 0,
                "Online checkout applies the constant Rs. 800 hospital charge");
        check(new BigDecimal("18800.00").compareTo(summary.getTotalAmount()) == 0,
                "Online total contains treatment charge plus hospital charge only");
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
