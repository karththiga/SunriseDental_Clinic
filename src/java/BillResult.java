import java.math.BigDecimal;
import java.sql.Timestamp;

/** Billing DTO used for cashier search, payment collection and receipts. */
public class BillResult {
    private int appointmentId;
    private String appointmentNumber, patientName, contactNumber, dentistName,
            treatmentName, appointmentDate, appointmentTime, appointmentStatus,
            paymentStatus, paymentReference, paymentMethod, cardLastFour,
            refundReference;
    private BigDecimal treatmentCost, hospitalCharge, totalAmount, refundedAmount;
    private Timestamp paymentDate, refundedAt;

    public int getAppointmentId(){return appointmentId;} public void setAppointmentId(int v){appointmentId=v;}
    public String getAppointmentNumber(){return appointmentNumber;} public void setAppointmentNumber(String v){appointmentNumber=v;}
    public String getPatientName(){return patientName;} public void setPatientName(String v){patientName=v;}
    public String getContactNumber(){return contactNumber;} public void setContactNumber(String v){contactNumber=v;}
    public String getDentistName(){return dentistName;} public void setDentistName(String v){dentistName=v;}
    public String getTreatmentName(){return treatmentName;} public void setTreatmentName(String v){treatmentName=v;}
    public String getAppointmentDate(){return appointmentDate;} public void setAppointmentDate(String v){appointmentDate=v;}
    public String getAppointmentTime(){return appointmentTime;} public void setAppointmentTime(String v){appointmentTime=v;}
    public String getAppointmentStatus(){return appointmentStatus;} public void setAppointmentStatus(String v){appointmentStatus=v;}
    public String getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(String v){paymentStatus=v;}
    public String getPaymentReference(){return paymentReference;} public void setPaymentReference(String v){paymentReference=v;}
    public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;}
    public String getCardLastFour(){return cardLastFour;} public void setCardLastFour(String v){cardLastFour=v;}
    public String getRefundReference(){return refundReference;} public void setRefundReference(String v){refundReference=v;}
    public BigDecimal getTreatmentCost(){return treatmentCost;} public void setTreatmentCost(BigDecimal v){treatmentCost=v;}
    public BigDecimal getHospitalCharge(){return hospitalCharge;} public void setHospitalCharge(BigDecimal v){hospitalCharge=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public BigDecimal getRefundedAmount(){return refundedAmount;} public void setRefundedAmount(BigDecimal v){refundedAmount=v;}
    public Timestamp getPaymentDate(){return paymentDate;} public void setPaymentDate(Timestamp v){paymentDate=v;}
    public Timestamp getRefundedAt(){return refundedAt;} public void setRefundedAt(Timestamp v){refundedAt=v;}
    public boolean isPaid(){return "Paid".equalsIgnoreCase(paymentStatus);}
    public boolean isUnpaid(){return "Unpaid".equalsIgnoreCase(paymentStatus);}
}
