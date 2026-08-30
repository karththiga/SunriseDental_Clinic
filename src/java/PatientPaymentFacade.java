import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Facade for checkout completion. Appointment and receipt persistence occur in
 * one transaction, so a confirmed patient appointment always has a payment.
 */
public class PatientPaymentFacade {
    private static final int SLOT_MINUTES = 30;

    public PaymentSummary loadSummary(PendingReservation pending)
            throws SQLException {
        String sql = "SELECT CONCAT(u.first_name,' ',u.last_name) patient_name," 
                + "d.dentist_name,t.treatment_name,t.treatment_cost "
                + "FROM users u JOIN dentists d ON d.dentist_id=? "
                + "JOIN dentist_treatments dt ON dt.dentist_id=d.dentist_id AND dt.treatment_id=? "
                + "JOIN treatments t ON t.treatment_id=dt.treatment_id "
                + "WHERE u.user_id=? AND u.role='Patient' AND d.status='Active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pending.getDentistId());
            stmt.setInt(2, pending.getTreatmentId());
            stmt.setInt(3, pending.getPatientUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return new PaymentSummary(rs.getString("patient_name"),
                        rs.getString("dentist_name"), rs.getString("treatment_name"),
                        rs.getBigDecimal("treatment_cost"));
            }
        }
    }

    public PaymentCompletion completePayment(PendingReservation pending,
            DummyPaymentGateway.Authorization authorization) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                CheckoutRecord record = lockCheckoutRecord(conn, pending);
                if (record == null) {
                    throw new IllegalArgumentException(
                            "The selected dentist or treatment is no longer available.");
                }
                validateSlot(record, pending.getAppointmentDate(),
                        pending.getAppointmentTime());
                if (slotIsTaken(conn, pending)) {
                    throw new IllegalArgumentException(
                            "That appointment slot was just reserved by another patient.");
                }

                int appointmentId = insertAppointment(conn, pending, record);
                String appointmentNumber = String.format("APT%04d", appointmentId);
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE appointments SET appointment_number=? WHERE appointment_id=?")) {
                    stmt.setString(1, appointmentNumber);
                    stmt.setInt(2, appointmentId);
                    stmt.executeUpdate();
                }

                BigDecimal hospitalCharge = PaymentSummary.HOSPITAL_CHARGE;
                BigDecimal total = record.treatmentCost.add(hospitalCharge);
                String billSql = "INSERT INTO bills (appointment_id,treatment_cost," 
                        + "consultation_fee,hospital_charge,total_amount,payment_status," 
                        + "payment_reference,payment_method,card_last_four) "
                        + "VALUES (?,?,0,?,?,'Paid',?,?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(billSql)) {
                    stmt.setInt(1, appointmentId);
                    stmt.setBigDecimal(2, record.treatmentCost);
                    stmt.setBigDecimal(3, hospitalCharge);
                    stmt.setBigDecimal(4, total);
                    stmt.setString(5, authorization.getReference());
                    stmt.setString(6, authorization.getCardBrand());
                    stmt.setString(7, authorization.getLastFour());
                    stmt.executeUpdate();
                }

                conn.commit();
                return new PaymentCompletion(appointmentNumber,
                        authorization.getReference());
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private CheckoutRecord lockCheckoutRecord(Connection conn,
            PendingReservation pending) throws SQLException {
        String sql = "SELECT CONCAT(u.first_name,' ',u.last_name) patient_name," 
                + "u.phone_number,d.dentist_name,d.available_day,d.available_from," 
                + "d.available_to,t.treatment_cost "
                + "FROM users u JOIN dentists d ON d.dentist_id=? "
                + "JOIN dentist_treatments dt ON dt.dentist_id=d.dentist_id AND dt.treatment_id=? "
                + "JOIN treatments t ON t.treatment_id=dt.treatment_id "
                + "WHERE u.user_id=? AND u.role='Patient' AND d.status='Active' FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pending.getDentistId());
            stmt.setInt(2, pending.getTreatmentId());
            stmt.setInt(3, pending.getPatientUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                Time from = rs.getTime("available_from");
                Time to = rs.getTime("available_to");
                return new CheckoutRecord(rs.getString("patient_name"),
                        rs.getString("phone_number"), rs.getString("dentist_name"),
                        rs.getString("available_day"),
                        from == null ? null : from.toLocalTime(),
                        to == null ? null : to.toLocalTime(),
                        rs.getBigDecimal("treatment_cost"));
            }
        }
    }

    private void validateSlot(CheckoutRecord record, LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())
                || (date.equals(LocalDate.now()) && !time.isAfter(LocalTime.now()))) {
            throw new IllegalArgumentException("The selected appointment time has passed.");
        }
        if (record.availableDay == null
                || !date.getDayOfWeek().name().equalsIgnoreCase(record.availableDay)) {
            throw new IllegalArgumentException(record.dentistName
                    + " is not visiting on the selected date.");
        }
        long minutes = record.availableFrom == null ? -1
                : (time.toSecondOfDay() - record.availableFrom.toSecondOfDay()) / 60;
        if (record.availableFrom == null || record.availableTo == null
                || time.isBefore(record.availableFrom)
                || time.plusMinutes(SLOT_MINUTES).isAfter(record.availableTo)
                || minutes % SLOT_MINUTES != 0) {
            throw new IllegalArgumentException(
                    "The selected time is outside the dentist's visiting hours.");
        }
    }

    private boolean slotIsTaken(Connection conn, PendingReservation pending)
            throws SQLException {
        String sql = "SELECT appointment_id FROM appointments WHERE dentist_id=? "
                + "AND appointment_date=? AND appointment_time=? "
                + "AND status NOT IN ('Rejected','Cancelled')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pending.getDentistId());
            stmt.setString(2, pending.getAppointmentDate().toString());
            stmt.setTime(3, Time.valueOf(pending.getAppointmentTime()));
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    private int insertAppointment(Connection conn, PendingReservation pending,
            CheckoutRecord record) throws SQLException {
        String sql = "INSERT INTO appointments (appointment_number,patient_user_id," 
                + "patient_name,address,contact_number,dentist_id,treatment_id," 
                + "appointment_date,appointment_time,status) "
                + "VALUES (NULL,?,?, '',?,?,?,?,?,'Confirmed')";
        try (PreparedStatement stmt = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pending.getPatientUserId());
            stmt.setString(2, record.patientName);
            stmt.setString(3, record.phoneNumber);
            stmt.setInt(4, pending.getDentistId());
            stmt.setInt(5, pending.getTreatmentId());
            stmt.setString(6, pending.getAppointmentDate().toString());
            stmt.setTime(7, Time.valueOf(pending.getAppointmentTime()));
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Appointment ID was not generated.");
                return keys.getInt(1);
            }
        }
    }

    public static class PaymentCompletion {
        private final String appointmentNumber;
        private final String paymentReference;
        PaymentCompletion(String number, String reference) {
            appointmentNumber = number;
            paymentReference = reference;
        }
        public String getAppointmentNumber() { return appointmentNumber; }
        public String getPaymentReference() { return paymentReference; }
    }

    private static class CheckoutRecord {
        private final String patientName;
        private final String phoneNumber;
        private final String dentistName;
        private final String availableDay;
        private final LocalTime availableFrom;
        private final LocalTime availableTo;
        private final BigDecimal treatmentCost;
        CheckoutRecord(String patientName, String phoneNumber, String dentistName,
                String day, LocalTime from, LocalTime to, BigDecimal treatmentCost) {
            this.patientName = patientName;
            this.phoneNumber = phoneNumber;
            this.dentistName = dentistName;
            this.availableDay = day;
            this.availableFrom = from;
            this.availableTo = to;
            this.treatmentCost = treatmentCost;
        }
    }
}
