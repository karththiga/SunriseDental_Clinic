import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Dentist self-service for recurring hours and date-specific leave. */
@WebServlet("/DoctorAvailabilityServlet")
public class DoctorAvailabilityServlet extends HttpServlet {
    private static final String[] DAYS = {"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"};
    private final ClinicNotificationService notifications = new ClinicNotificationService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session=request.getSession(false);
        if(!isDentist(session)){response.sendRedirect("login.jsp");return;}
        try(Connection conn=DBConnection.getConnection()){
            Integer dentistId=findDentistId(conn,((Number)session.getAttribute("user_id")).intValue());
            if(dentistId==null)request.setAttribute("error","Dentist profile not found.");
            else {request.setAttribute("dentistId",dentistId);loadSchedules(conn,dentistId,request);loadLeaves(conn,dentistId,request);}
            flash(session,request);
        }catch(SQLException e){getServletContext().log("Unable to load dentist availability.",e);request.setAttribute("error","Unable to load availability. Import db/doctor_availability_notifications.sql if it has not been imported.");}
        request.setAttribute("minimumDate",LocalDate.now());
        request.getRequestDispatcher("/doctorAvailability.jsp").forward(request,response);
    }

    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        HttpSession session=request.getSession(false);
        if(!isDentist(session)){response.sendRedirect("login.jsp");return;}
        String action=clean(request.getParameter("action"));
        try(Connection conn=DBConnection.getConnection()){
            int userId=((Number)session.getAttribute("user_id")).intValue();
            Integer dentistId=findDentistId(conn,userId);
            if(dentistId==null)throw new IllegalArgumentException("Dentist profile not found.");
            if("saveSchedule".equals(action))saveSchedule(conn,dentistId,userId,request);
            else if("deleteSchedule".equals(action))deleteSchedule(conn,dentistId,userId,request);
            else if("addLeave".equals(action))addLeave(conn,dentistId,userId,request);
            else if("removeLeave".equals(action))removeLeave(conn,dentistId,userId,request);
            else throw new IllegalArgumentException("Invalid availability action.");
        }catch(IllegalArgumentException e){session.setAttribute("availabilityError",e.getMessage());}
        catch(SQLException e){getServletContext().log("Unable to update dentist availability.",e);session.setAttribute("availabilityError","The availability change was not saved.");}
        response.sendRedirect("DoctorAvailabilityServlet");
    }

    private void saveSchedule(Connection conn,int dentistId,int userId,HttpServletRequest request)throws SQLException{
        String day=clean(request.getParameter("dayOfWeek"));LocalTime from=time(request.getParameter("availableFrom")),to=time(request.getParameter("availableTo"));
        if(!validDay(day)||from==null||to==null||!from.isBefore(to))throw new IllegalArgumentException("Select a valid weekday and visiting time range.");
        String sql="INSERT INTO dentist_availability (dentist_id,day_of_week,available_from,available_to,is_active) VALUES (?,?,?,?,1) "
                +"ON DUPLICATE KEY UPDATE available_from=VALUES(available_from),available_to=VALUES(available_to),is_active=1";
        try(PreparedStatement stmt=conn.prepareStatement(sql)){stmt.setInt(1,dentistId);stmt.setString(2,day);stmt.setTime(3,Time.valueOf(from));stmt.setTime(4,Time.valueOf(to));stmt.executeUpdate();}
        syncLegacySchedule(conn,dentistId);notifyRoles(conn,userId,"AVAILABILITY_UPDATED","Doctor availability updated","A dentist updated recurring visiting hours for "+title(day)+".");
        request.getSession().setAttribute("availabilitySuccess",title(day)+" visiting hours saved.");
    }

    private void deleteSchedule(Connection conn,int dentistId,int userId,HttpServletRequest request)throws SQLException{
        String day=clean(request.getParameter("dayOfWeek"));if(!validDay(day))throw new IllegalArgumentException("Invalid weekday.");
        try(PreparedStatement stmt=conn.prepareStatement("DELETE FROM dentist_availability WHERE dentist_id=? AND day_of_week=?")){stmt.setInt(1,dentistId);stmt.setString(2,day);stmt.executeUpdate();}
        syncLegacySchedule(conn,dentistId);notifyRoles(conn,userId,"AVAILABILITY_UPDATED","Doctor availability removed","A dentist removed recurring visiting hours for "+title(day)+".");
        request.getSession().setAttribute("availabilitySuccess",title(day)+" availability removed. New bookings are now blocked.");
    }

    private void addLeave(Connection conn,int dentistId,int userId,HttpServletRequest request)throws SQLException{
        LocalDate date=date(request.getParameter("leaveDate"));String reason=clean(request.getParameter("reason"));
        if(date==null||date.isBefore(LocalDate.now()))throw new IllegalArgumentException("Select today or a future leave date.");
        if(reason==null||reason.length()<3||reason.length()>255)throw new IllegalArgumentException("Enter a brief leave reason.");
        try(PreparedStatement stmt=conn.prepareStatement("INSERT INTO dentist_leaves (dentist_id,leave_date,reason) VALUES (?,?,?) ON DUPLICATE KEY UPDATE reason=VALUES(reason)")){stmt.setInt(1,dentistId);stmt.setString(2,date.toString());stmt.setString(3,reason);stmt.executeUpdate();}
        int affected=notifyAffectedPatients(conn,dentistId,userId,date,reason);
        notifyRoles(conn,userId,"DOCTOR_LEAVE","Doctor leave announced","A dentist announced leave on "+date+". "+affected+" existing appointment(s) require review.");
        request.getSession().setAttribute("availabilitySuccess","Leave announced for "+date+". New bookings are blocked"+(affected>0?"; "+affected+" existing appointment(s) were notified.":"."));
    }

    private void removeLeave(Connection conn,int dentistId,int userId,HttpServletRequest request)throws SQLException{
        LocalDate date=date(request.getParameter("leaveDate"));if(date==null)throw new IllegalArgumentException("Invalid leave date.");
        try(PreparedStatement stmt=conn.prepareStatement("DELETE FROM dentist_leaves WHERE dentist_id=? AND leave_date=?")){stmt.setInt(1,dentistId);stmt.setString(2,date.toString());stmt.executeUpdate();}
        notifyRoles(conn,userId,"DOCTOR_LEAVE_REMOVED","Doctor leave withdrawn","A dentist withdrew leave for "+date+"; live booking is available again.");
        request.getSession().setAttribute("availabilitySuccess","Leave for "+date+" removed.");
    }

    private int notifyAffectedPatients(Connection conn,int dentistId,int creator,LocalDate date,String reason)throws SQLException{
        int count=0;String select="SELECT appointment_id,patient_user_id,appointment_number FROM appointments WHERE dentist_id=? AND appointment_date=? AND status NOT IN ('Cancelled','Rejected')";
        try(PreparedStatement stmt=conn.prepareStatement(select)){stmt.setInt(1,dentistId);stmt.setString(2,date.toString());try(ResultSet rs=stmt.executeQuery()){
            while(rs.next()){count++;Object patient=rs.getObject("patient_user_id");if(patient!=null)notifications.notifyUser(conn,((Number)patient).intValue(),"DOCTOR_LEAVE","Doctor unavailable for your appointment","Your appointment "+rs.getString("appointment_number")+" is affected by doctor leave on "+date+". The clinic will contact you to reschedule. Reason: "+reason,rs.getInt("appointment_id"),creator);}
        }}return count;
    }

    private void notifyRoles(Connection conn,int creator,String type,String title,String message)throws SQLException{
        notifications.notifyRole(conn,"Admin",type,title,message,null,creator);notifications.notifyRole(conn,"Staff",type,title,message,null,creator);
    }

    private void syncLegacySchedule(Connection conn,int dentistId)throws SQLException{
        String select="SELECT day_of_week,available_from,available_to FROM dentist_availability WHERE dentist_id=? AND is_active=1 ORDER BY FIELD(day_of_week,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') LIMIT 1";
        try(PreparedStatement find=conn.prepareStatement(select)){find.setInt(1,dentistId);try(ResultSet rs=find.executeQuery();PreparedStatement update=conn.prepareStatement("UPDATE dentists SET available_day=?,available_from=?,available_to=? WHERE dentist_id=?")){
            if(rs.next()){update.setString(1,title(rs.getString(1)));update.setTime(2,rs.getTime(2));update.setTime(3,rs.getTime(3));}else{update.setNull(1,java.sql.Types.VARCHAR);update.setNull(2,java.sql.Types.TIME);update.setNull(3,java.sql.Types.TIME);}update.setInt(4,dentistId);update.executeUpdate();}}
    }
    private void loadSchedules(Connection conn,int dentistId,HttpServletRequest request)throws SQLException{List<Map<String,Object>> rows=new ArrayList<>();try(PreparedStatement stmt=conn.prepareStatement("SELECT day_of_week,available_from,available_to FROM dentist_availability WHERE dentist_id=? AND is_active=1 ORDER BY FIELD(day_of_week,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')")){stmt.setInt(1,dentistId);try(ResultSet rs=stmt.executeQuery()){while(rs.next()){Map<String,Object> row=new HashMap<>();row.put("day",title(rs.getString(1)));row.put("dayValue",rs.getString(1));row.put("from",rs.getTime(2).toLocalTime());row.put("to",rs.getTime(3).toLocalTime());rows.add(row);}}}request.setAttribute("schedules",rows);request.setAttribute("days",DAYS);}
    private void loadLeaves(Connection conn,int dentistId,HttpServletRequest request)throws SQLException{List<Map<String,Object>> rows=new ArrayList<>();try(PreparedStatement stmt=conn.prepareStatement("SELECT leave_date,reason FROM dentist_leaves WHERE dentist_id=? AND leave_date>=CURRENT_DATE ORDER BY leave_date")){stmt.setInt(1,dentistId);try(ResultSet rs=stmt.executeQuery()){while(rs.next()){Map<String,Object> row=new HashMap<>();row.put("date",rs.getDate(1).toLocalDate());row.put("reason",html(rs.getString(2)));rows.add(row);}}}request.setAttribute("leaves",rows);}
    private Integer findDentistId(Connection conn,int userId)throws SQLException{try(PreparedStatement stmt=conn.prepareStatement("SELECT dentist_id FROM dentists WHERE user_id=?")){stmt.setInt(1,userId);try(ResultSet rs=stmt.executeQuery()){return rs.next()?rs.getInt(1):null;}}}
    private void flash(HttpSession session,HttpServletRequest request){Object ok=session.getAttribute("availabilitySuccess"),bad=session.getAttribute("availabilityError");if(ok!=null)request.setAttribute("success",ok);if(bad!=null)request.setAttribute("error",bad);session.removeAttribute("availabilitySuccess");session.removeAttribute("availabilityError");}
    private boolean isDentist(HttpSession s){return s!=null&&s.getAttribute("username")!=null&&"Dentist".equalsIgnoreCase((String)s.getAttribute("role"));}
    private boolean validDay(String value){if(value==null)return false;for(String day:DAYS)if(day.equals(value))return true;return false;}
    private LocalTime time(String value){try{return value==null?null:LocalTime.parse(value);}catch(RuntimeException e){return null;}}
    private LocalDate date(String value){try{return value==null?null:LocalDate.parse(value);}catch(RuntimeException e){return null;}}
    private String clean(String value){return value==null||value.isBlank()?null:value.trim();}
    private String title(String value){return value==null?"":value.substring(0,1).toUpperCase()+value.substring(1).toLowerCase();}
    private String html(String value){return value==null?"":value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
