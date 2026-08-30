import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Shared notification centre for every authenticated clinic persona. */
@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession(false);if(session==null||session.getAttribute("username")==null){response.sendRedirect("login.jsp");return;}
        int userId=((Number)session.getAttribute("user_id")).intValue();String role=(String)session.getAttribute("role");List<Map<String,Object>> notices=new ArrayList<>();
        try(Connection conn=DBConnection.getConnection()){
            loadShared(conn,userId,role,notices);if("Patient".equalsIgnoreCase(role))loadLegacyPatient(conn,userId,notices);
            notices.sort(Comparator.comparing(n->(Timestamp)n.get("createdAt"),Comparator.reverseOrder()));
            markPersonalRead(conn,userId);if("Patient".equalsIgnoreCase(role))markLegacyRead(conn,userId);
        }catch(SQLException e){getServletContext().log("Unable to load notifications.",e);request.setAttribute("error","Unable to load notifications. Import db/doctor_availability_notifications.sql if needed.");}
        request.setAttribute("notifications",notices);request.setAttribute("dashboard",dashboard(role));request.getRequestDispatcher("/notifications.jsp").forward(request,response);
    }
    private void loadShared(Connection conn,int userId,String role,List<Map<String,Object>> rows)throws SQLException{
        String sql="SELECT title,message,notification_type,CASE WHEN recipient_user_id IS NULL THEN 1 ELSE is_read END display_read,created_at FROM notifications WHERE recipient_user_id=? OR (recipient_user_id IS NULL AND LOWER(recipient_role)=LOWER(?)) ORDER BY created_at DESC LIMIT 50";
        try(PreparedStatement stmt=conn.prepareStatement(sql)){stmt.setInt(1,userId);stmt.setString(2,role);try(ResultSet rs=stmt.executeQuery()){while(rs.next())rows.add(row(rs));}}
    }
    private void loadLegacyPatient(Connection conn,int userId,List<Map<String,Object>> rows)throws SQLException{
        try(PreparedStatement stmt=conn.prepareStatement("SELECT title,message,notification_type,is_read display_read,created_at FROM patient_notifications WHERE recipient_user_id=? ORDER BY created_at DESC LIMIT 50")){stmt.setInt(1,userId);try(ResultSet rs=stmt.executeQuery()){while(rs.next())rows.add(row(rs));}}
    }
    private Map<String,Object> row(ResultSet rs)throws SQLException{Map<String,Object> row=new HashMap<>();row.put("title",html(rs.getString("title")));row.put("message",html(rs.getString("message")));row.put("type",html(rs.getString("notification_type")));row.put("read",rs.getBoolean("display_read"));row.put("createdAt",rs.getTimestamp("created_at"));return row;}
    private void markPersonalRead(Connection conn,int userId)throws SQLException{try(PreparedStatement stmt=conn.prepareStatement("UPDATE notifications SET is_read=1 WHERE recipient_user_id=? AND is_read=0")){stmt.setInt(1,userId);stmt.executeUpdate();}}
    private void markLegacyRead(Connection conn,int userId)throws SQLException{try(PreparedStatement stmt=conn.prepareStatement("UPDATE patient_notifications SET is_read=1 WHERE recipient_user_id=? AND is_read=0")){stmt.setInt(1,userId);stmt.executeUpdate();}}
    private String dashboard(String role){if("Admin".equalsIgnoreCase(role))return "adminDashboard.jsp";if("Dentist".equalsIgnoreCase(role))return "dentistDashboard.jsp";if("Cashier".equalsIgnoreCase(role))return "cashierDashboard.jsp";if("Patient".equalsIgnoreCase(role))return "patientDashboard.jsp";return "dashboard.jsp";}
    private String html(String value){return value==null?"":value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
