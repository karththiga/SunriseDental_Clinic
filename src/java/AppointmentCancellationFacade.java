import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * Facade Pattern: coordinates appointment cancellation, simulated refund and
 * patient notification as one database transaction.
 */
public class AppointmentCancellationFacade {
    private final DummyPaymentGateway paymentGateway;

    public AppointmentCancellationFacade() {
        this(new DummyPaymentGateway());
    }

    AppointmentCancellationFacade(DummyPaymentGateway gateway) {
        paymentGateway = gateway;
    }

    public CancellationResult cancel(int appointmentId, int adminUserId,
            String reason) throws SQLException {
        String cleanReason = reason == null ? "" : reason.trim();
        if (cleanReason.length() < 5 || cleanReason.length() > 255) {
            throw new IllegalArgumentException(
                    "Enter a cancellation reason between 5 and 255 characters.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                CancellationRecord record = lockAppointment(conn, appointmentId);
                if (record == null) {
                    throw new IllegalArgumentException("The selected appointment no longer exists.");
                }
                if ("Cancelled".equalsIgnoreCase(record.status)) {
                    throw new IllegalArgumentException("This appointment is already cancelled.");
                }
                if ("Completed".equalsIgnoreCase(record.status)
                        || "Rejected".equalsIgnoreCase(record.status)) {
                    throw new IllegalArgumentException(
                            "Completed or rejected appointments cannot be cancelled.");
                }

                RefundRecord refund = refundPaidBill(conn, record);
                updateAppointment(conn, appointmentId, adminUserId, cleanReason);
                createNotification(conn, record, cleanReason, refund);
                conn.commit();
                return new CancellationResult(record.appointmentNumber,
                        refund != null, refund == null ? null : refund.reference,
                        refund == null ? BigDecimal.ZERO : refund.amount,
                        record.patientUserId != null);
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private CancellationRecord lockAppointment(Connection conn, int appointmentId)
            throws SQLException {
        String sql = "SELECT a.appointment_number,a.patient_user_id,a.patient_name,"
                + "a.contact_number,a.appointment_date,a.appointment_time,a.status,"
                + "d.dentist_name FROM appointments a LEFT JOIN dentists d "
                + "ON a.dentist_id=d.dentist_id WHERE a.appointment_id=? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return new CancellationRecord(appointmentId,
                        (Integer) rs.getObject("patient_user_id"),
                        rs.getString("appointment_number"),
                        rs.getString("patient_name"), rs.getString("contact_number"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("appointment_time").toLocalTime(),
                        rs.getString("status"), rs.getString("dentist_name"));
            }
        }
    }

    private RefundRecord refundPaidBill(Connection conn, CancellationRecord record)
            throws SQLException {
        String sql = "SELECT bill_id,total_amount,payment_status,payment_reference "
                + "FROM bills WHERE appointment_id=? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || !"Paid".equalsIgnoreCase(rs.getString("payment_status"))) {
                    return null;
                }
                int billId = rs.getInt("bill_id");
                BigDecimal amount = rs.getBigDecimal("total_amount");
                String originalReference = rs.getString("payment_reference");
                if (originalReference == null || originalReference.isBlank()) {
                    originalReference = "LEGACY-BILL-" + billId;
                }
                DummyPaymentGateway.Refund refund = paymentGateway.refund(
                        originalReference, amount);
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE bills SET payment_status='Refunded',refund_reference=?,"
                        + "refunded_amount=?,refunded_at=CURRENT_TIMESTAMP WHERE bill_id=?")) {
                    update.setString(1, refund.getReference());
                    update.setBigDecimal(2, refund.getAmount());
                    update.setInt(3, billId);
                    update.executeUpdate();
                }
                return new RefundRecord(refund.getReference(), refund.getAmount());
            }
        }
    }

    private void updateAppointment(Connection conn, int appointmentId,
            int adminUserId, String reason) throws SQLException {
        String sql = "UPDATE appointments SET status='Cancelled',cancellation_reason=?,"
                + "cancelled_at=CURRENT_TIMESTAMP,cancelled_by=? WHERE appointment_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reason);
            stmt.setInt(2, adminUserId);
            stmt.setInt(3, appointmentId);
            stmt.executeUpdate();
        }
    }

    private void createNotification(Connection conn, CancellationRecord record,
            String reason, RefundRecord refund) throws SQLException {
        String date = record.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String message = "Appointment " + record.appointmentNumber + " with "
                + (record.dentistName == null ? "the clinic dentist" : record.dentistName)
                + " on " + date + " at " + record.time + " was cancelled. Reason: "
                + reason + ".";
        if (refund != null) {
            message += " A refund of LKR " + refund.amount.toPlainString()
                    + " was processed. Refund reference: " + refund.reference + ".";
        } else {
            message += " No paid bill was found, so no refund was required.";
        }

        String sql = "INSERT INTO patient_notifications (recipient_user_id,appointment_id,"
                + "notification_type,title,message,recipient_contact) VALUES (?,?,"
                + "'Appointment Cancellation','Appointment cancelled',?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (record.patientUserId == null) stmt.setNull(1, java.sql.Types.INTEGER);
            else stmt.setInt(1, record.patientUserId);
            stmt.setInt(2, record.appointmentId);
            stmt.setString(3, message);
            stmt.setString(4, record.contactNumber);
            stmt.executeUpdate();
        }
    }

    public static class CancellationResult {
        private final String appointmentNumber;
        private final boolean refunded;
        private final String refundReference;
        private final BigDecimal refundedAmount;
        private final boolean inAppNotificationAvailable;

        CancellationResult(String number, boolean hasRefund, String reference,
                BigDecimal amount, boolean notificationAvailable) {
            appointmentNumber = number; refunded = hasRefund;
            refundReference = reference; refundedAmount = amount;
            inAppNotificationAvailable = notificationAvailable;
        }
        public String getAppointmentNumber() { return appointmentNumber; }
        public boolean isRefunded() { return refunded; }
        public String getRefundReference() { return refundReference; }
        public BigDecimal getRefundedAmount() { return refundedAmount; }
        public boolean isInAppNotificationAvailable() { return inAppNotificationAvailable; }
    }

    private static class RefundRecord {
        final String reference; final BigDecimal amount;
        RefundRecord(String value, BigDecimal total) { reference = value; amount = total; }
    }

    private static class CancellationRecord {
        final int appointmentId; final Integer patientUserId;
        final String appointmentNumber, patientName, contactNumber, status, dentistName;
        final java.time.LocalDate date; final java.time.LocalTime time;
        CancellationRecord(int id, Integer userId, String number, String name,
                String contact, java.time.LocalDate day, java.time.LocalTime slot,
                String state, String dentist) {
            appointmentId = id; patientUserId = userId; appointmentNumber = number;
            patientName = name; contactNumber = contact; date = day; time = slot;
            status = state; dentistName = dentist;
        }
    }
}
