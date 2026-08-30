import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Central notification mediator used by appointment and availability workflows. */
public class ClinicNotificationService {
    public void notifyUser(Connection conn,int userId,String type,String title,String message,Integer appointmentId,Integer creator)throws SQLException{
        insert(conn,userId,null,type,title,message,appointmentId,creator);
    }
    public void notifyRole(Connection conn,String role,String type,String title,String message,Integer appointmentId,Integer creator)throws SQLException{
        insert(conn,null,role,type,title,message,appointmentId,creator);
    }
    public void appointmentCreated(Connection conn,int appointmentId,Integer creator)throws SQLException{
        String sql="SELECT a.appointment_number,a.patient_user_id,a.appointment_date,a.appointment_time,d.user_id dentist_user_id,d.dentist_name "
                +"FROM appointments a JOIN dentists d ON d.dentist_id=a.dentist_id WHERE a.appointment_id=?";
        try(PreparedStatement stmt=conn.prepareStatement(sql)){stmt.setInt(1,appointmentId);try(ResultSet rs=stmt.executeQuery()){if(!rs.next())return;
            String number=rs.getString("appointment_number"),when=rs.getDate("appointment_date")+" at "+rs.getTime("appointment_time");
            Object dentistUser=rs.getObject("dentist_user_id");if(dentistUser!=null)notifyUser(conn,((Number)dentistUser).intValue(),"APPOINTMENT_CREATED","New appointment assigned",number+" has been assigned to you for "+when+".",appointmentId,creator);
            Object patient=rs.getObject("patient_user_id");if(patient!=null)notifyUser(conn,((Number)patient).intValue(),"APPOINTMENT_CREATED","Appointment reserved",number+" with "+rs.getString("dentist_name")+" is reserved for "+when+".",appointmentId,creator);
        }}
    }
    private void insert(Connection conn,Integer userId,String role,String type,String title,String message,Integer appointmentId,Integer creator)throws SQLException{
        String sql="INSERT INTO notifications (recipient_user_id,recipient_role,notification_type,title,message,related_appointment_id,created_by) VALUES (?,?,?,?,?,?,?)";
        try(PreparedStatement stmt=conn.prepareStatement(sql)){if(userId==null)stmt.setNull(1,java.sql.Types.INTEGER);else stmt.setInt(1,userId);stmt.setString(2,role);stmt.setString(3,type);stmt.setString(4,title);stmt.setString(5,message);if(appointmentId==null)stmt.setNull(6,java.sql.Types.INTEGER);else stmt.setInt(6,appointmentId);if(creator==null)stmt.setNull(7,java.sql.Types.INTEGER);else stmt.setInt(7,creator);stmt.executeUpdate();}
    }
}
