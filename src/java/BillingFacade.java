import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Facade Pattern: cashier appointment search, payment collection and receipt retrieval. */
public class BillingFacade {
    private static final BigDecimal HOSPITAL_CHARGE = new BigDecimal("800.00");

    public List<BillResult> searchAppointments(String query) throws SQLException {
        List<BillResult> results = new ArrayList<>();
        String clean = query == null ? "" : query.trim();
        boolean searching = !clean.isEmpty();
        String sql = baseSelect() + (searching ? " WHERE a.appointment_number LIKE ? OR a.patient_name LIKE ? OR a.contact_number LIKE ?" : "")
                + " ORDER BY a.appointment_date DESC,a.appointment_time DESC LIMIT 100";
        try (Connection conn=DBConnection.getConnection();PreparedStatement stmt=conn.prepareStatement(sql)) {
            if(searching){String value="%"+clean+"%";stmt.setString(1,value);stmt.setString(2,value);stmt.setString(3,value);}
            try(ResultSet rs=stmt.executeQuery()){while(rs.next())results.add(map(rs));}
        }
        return results;
    }

    public BillResult findByAppointmentNumber(String number) throws SQLException {
        if(number==null||number.isBlank())return null;
        try(Connection conn=DBConnection.getConnection();PreparedStatement stmt=conn.prepareStatement(
                baseSelect()+" WHERE a.appointment_number=?")){
            stmt.setString(1,number.trim());try(ResultSet rs=stmt.executeQuery()){return rs.next()?map(rs):null;}
        }
    }

    public BillResult collectCounterPayment(String number,String method,int cashierUserId) throws SQLException {
        if(!"Cash".equals(method)&&!"Card - Counter".equals(method))
            throw new IllegalArgumentException("Select Cash or Card - Counter as the payment method.");
        try(Connection conn=DBConnection.getConnection()){
            boolean original=conn.getAutoCommit();conn.setAutoCommit(false);
            try{
                PaymentRecord record=lockPaymentRecord(conn,number);
                if(record==null)throw new IllegalArgumentException("Appointment not found.");
                if("Cancelled".equalsIgnoreCase(record.appointmentStatus)||"Rejected".equalsIgnoreCase(record.appointmentStatus))
                    throw new IllegalArgumentException("Cancelled or rejected appointments cannot be paid.");
                if(record.billStatus!=null){
                    if("Paid".equalsIgnoreCase(record.billStatus)){conn.commit();return findByAppointmentNumber(number);}
                    if("Refunded".equalsIgnoreCase(record.billStatus))throw new IllegalArgumentException("This payment was refunded and cannot be collected again.");
                }
                BigDecimal total=record.treatmentCost.add(HOSPITAL_CHARGE);
                String reference="COUNTER-"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.ROOT);
                if(record.billId==null){
                    String sql="INSERT INTO bills (appointment_id,treatment_cost,consultation_fee,hospital_charge,"
                            +"total_amount,payment_status,payment_reference,payment_method,processed_by) VALUES (?,?,0,?,?,'Paid',?,?,?)";
                    try(PreparedStatement stmt=conn.prepareStatement(sql)){stmt.setInt(1,record.appointmentId);stmt.setBigDecimal(2,record.treatmentCost);
                        stmt.setBigDecimal(3,HOSPITAL_CHARGE);stmt.setBigDecimal(4,total);stmt.setString(5,reference);stmt.setString(6,method);stmt.setInt(7,cashierUserId);stmt.executeUpdate();}
                }else{
                    try(PreparedStatement stmt=conn.prepareStatement("UPDATE bills SET treatment_cost=?,consultation_fee=0,hospital_charge=?,"
                            +"total_amount=?,payment_status='Paid',payment_reference=?,payment_method=?,processed_by=?,bill_date=CURRENT_TIMESTAMP WHERE bill_id=?")){
                        stmt.setBigDecimal(1,record.treatmentCost);stmt.setBigDecimal(2,HOSPITAL_CHARGE);stmt.setBigDecimal(3,total);
                        stmt.setString(4,reference);stmt.setString(5,method);stmt.setInt(6,cashierUserId);stmt.setInt(7,record.billId);stmt.executeUpdate();}
                }
                conn.commit();return findByAppointmentNumber(number);
            }catch(SQLException|RuntimeException e){conn.rollback();throw e;}finally{conn.setAutoCommit(original);}
        }
    }

    private PaymentRecord lockPaymentRecord(Connection conn,String number)throws SQLException{
        String sql="SELECT a.appointment_id,a.status,t.treatment_cost,b.bill_id,b.payment_status FROM appointments a "
                +"JOIN treatments t ON a.treatment_id=t.treatment_id LEFT JOIN bills b ON a.appointment_id=b.appointment_id "
                +"WHERE a.appointment_number=? FOR UPDATE";
        try(PreparedStatement stmt=conn.prepareStatement(sql)){stmt.setString(1,number);try(ResultSet rs=stmt.executeQuery()){
            if(!rs.next())return null;Integer bill=(Integer)rs.getObject("bill_id");return new PaymentRecord(rs.getInt("appointment_id"),
                    rs.getString("status"),rs.getBigDecimal("treatment_cost"),bill,rs.getString("payment_status"));}}
    }

    private String baseSelect(){return "SELECT a.appointment_id,a.appointment_number,a.patient_name,a.contact_number,a.appointment_date,"
            +"a.appointment_time,a.status appointment_status,d.dentist_name,t.treatment_name,"
            +"COALESCE(b.treatment_cost,t.treatment_cost) treatment_cost,COALESCE(b.hospital_charge,800.00) hospital_charge,"
            +"COALESCE(b.total_amount,t.treatment_cost+800.00) total_amount,COALESCE(b.payment_status,'Unpaid') payment_status,"
            +"b.payment_reference,b.payment_method,b.card_last_four,b.bill_date,b.refund_reference,b.refunded_amount,b.refunded_at "
            +"FROM appointments a JOIN dentists d ON a.dentist_id=d.dentist_id JOIN treatments t ON a.treatment_id=t.treatment_id "
            +"LEFT JOIN bills b ON a.appointment_id=b.appointment_id";}

    private BillResult map(ResultSet rs)throws SQLException{BillResult b=new BillResult();b.setAppointmentId(rs.getInt("appointment_id"));
        b.setAppointmentNumber(rs.getString("appointment_number"));b.setPatientName(rs.getString("patient_name"));b.setContactNumber(rs.getString("contact_number"));
        b.setAppointmentDate(rs.getString("appointment_date"));b.setAppointmentTime(rs.getString("appointment_time"));b.setAppointmentStatus(rs.getString("appointment_status"));
        b.setDentistName(rs.getString("dentist_name"));b.setTreatmentName(rs.getString("treatment_name"));b.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        b.setHospitalCharge(rs.getBigDecimal("hospital_charge"));b.setTotalAmount(rs.getBigDecimal("total_amount"));b.setPaymentStatus(rs.getString("payment_status"));
        b.setPaymentReference(rs.getString("payment_reference"));b.setPaymentMethod(rs.getString("payment_method"));b.setCardLastFour(rs.getString("card_last_four"));
        b.setPaymentDate(rs.getTimestamp("bill_date"));b.setRefundReference(rs.getString("refund_reference"));b.setRefundedAmount(rs.getBigDecimal("refunded_amount"));
        b.setRefundedAt(rs.getTimestamp("refunded_at"));return b;}

    private static class PaymentRecord{final int appointmentId;final String appointmentStatus,billStatus;final BigDecimal treatmentCost;final Integer billId;
        PaymentRecord(int id,String status,BigDecimal cost,Integer bill,String payment){appointmentId=id;appointmentStatus=status;treatmentCost=cost;billId=bill;billStatus=payment;}}
}
