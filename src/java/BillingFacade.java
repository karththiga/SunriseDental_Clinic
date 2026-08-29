import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Facade Pattern: presents one billing operation to the servlet while hiding
 * appointment lookup, charge composition and bill persistence.
 */
public class BillingFacade {

    /*
     * Main Facade method
     *
     * The servlet only needs to call this method.
     */
    public BillResult generateBill(String appointmentNumber)
            throws SQLException {

        /*
         * Step 1:
         * Get appointment and treatment information
         */
        BillResult bill =
                getAppointmentDetails(appointmentNumber);

        if (bill == null) {
            return null;
        }

        /*
         * Step 2: Decorator Pattern
         * Begin with the treatment charge and decorate it with the selected
         * dentist's consultation fee. Future charges can be added as new
         * decorators without changing the base charge class.
         */
        BillCharge totalCharge = new ConsultationFeeDecorator(
                new TreatmentCharge(bill.getTreatmentCost()),
                bill.getConsultationFee()
        );

        bill.setTotalAmount(totalCharge.getAmount());

        /*
         * Step 3:
         * Save bill
         */
        saveBill(bill);

        /*
         * Step 4:
         * Return completed bill
         */
        return bill;
    }


    /*
     * Find appointment information
     */
    private BillResult getAppointmentDetails(
            String appointmentNumber)
            throws SQLException {

        String sql =
                "SELECT "
                + "a.appointment_id, "
                + "a.appointment_number, "
                + "a.patient_name, "
                + "a.contact_number, "
                + "a.appointment_date, "
                + "a.appointment_time, "
                + "d.dentist_name, "
                + "d.consultation_fee, "
                + "t.treatment_name, "
                + "t.treatment_cost "
                + "FROM appointments a "
                + "INNER JOIN dentists d "
                + "ON a.dentist_id = d.dentist_id "
                + "INNER JOIN treatments t "
                + "ON a.treatment_id = t.treatment_id "
                + "WHERE a.appointment_number = ?";

        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    appointmentNumber
            );

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                BillResult bill =
                        new BillResult();

                bill.setAppointmentId(
                        rs.getInt(
                                "appointment_id"
                        )
                );

                bill.setAppointmentNumber(
                        rs.getString(
                                "appointment_number"
                        )
                );

                bill.setPatientName(
                        rs.getString(
                                "patient_name"
                        )
                );

                bill.setContactNumber(
                        rs.getString(
                                "contact_number"
                        )
                );

                bill.setDentistName(
                        rs.getString(
                                "dentist_name"
                        )
                );

                bill.setTreatmentName(
                        rs.getString(
                                "treatment_name"
                        )
                );

                bill.setTreatmentCost(
                        rs.getDouble(
                                "treatment_cost"
                        )
                );

                bill.setConsultationFee(
                        rs.getDouble(
                                "consultation_fee"
                        )
                );

                bill.setAppointmentDate(
                        rs.getString(
                                "appointment_date"
                        )
                );

                bill.setAppointmentTime(
                        rs.getString(
                                "appointment_time"
                        )
                );

                return bill;
            }
        }

        return null;
    }


    /*
     * Save calculated bill
     */
    private void saveBill(BillResult bill)
            throws SQLException {

        String sql =
                "INSERT INTO bills "
                + "(appointment_id, "
                + "treatment_cost, "
                + "consultation_fee, "
                + "total_amount, "
                + "payment_status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    bill.getAppointmentId()
            );

            stmt.setDouble(
                    2,
                    bill.getTreatmentCost()
            );

            stmt.setDouble(
                    3,
                    bill.getConsultationFee()
            );

            stmt.setDouble(
                    4,
                    bill.getTotalAmount()
            );

            stmt.setString(
                    5,
                    "Paid"
            );

            stmt.executeUpdate();
        }
    }
}
