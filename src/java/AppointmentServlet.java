import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Creates unpaid phone/walk-in reservations for administrators and staff. */
@WebServlet("/AppointmentServlet")
public class AppointmentServlet extends HttpServlet {
    private static final int SLOT_MINUTES = 30;
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("h:mm a");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!canBook(request.getSession(false))) { response.sendRedirect("login.jsp"); return; }
        request.setAttribute("success", request.getParameter("success"));
        loadReservationForm(request);
        request.getRequestDispatcher("/registerAppointment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!canBook(request.getSession(false))) { response.sendRedirect("login.jsp"); return; }
        Integer dentistId = integer(request.getParameter("dentistId"));
        Integer treatmentId = integer(request.getParameter("treatmentId"));
        String patientName = clean(request.getParameter("patientName"));
        String address = clean(request.getParameter("address"));
        String contact = clean(request.getParameter("contactNumber"));
        String dateValue = clean(request.getParameter("appointmentDate"));
        String timeValue = clean(request.getParameter("appointmentTime"));
        String error = null;

        if (dentistId == null || treatmentId == null || patientName == null || address == null
                || contact == null || dateValue == null || timeValue == null) {
            error = "Complete all appointment fields.";
        } else if (!contact.matches("\\d{10}")) {
            error = "Phone number must contain exactly 10 digits.";
        }

        if (error == null) {
            try {
                LocalDate date = LocalDate.parse(dateValue);
                LocalTime time = LocalTime.parse(timeValue);
                String result = reserve(patientName, address, contact, dentistId,
                        treatmentId, date, time, request);
                if (result.startsWith("SUCCESS:")) {
                    response.sendRedirect("AppointmentServlet?success=" + result.substring(8));
                    return;
                }
                error = result;
            } catch (RuntimeException e) {
                error = "Enter a valid appointment date and time.";
            } catch (SQLException e) {
                getServletContext().log("Unable to create unpaid reservation.", e);
                error = "Unable to reserve the appointment. Please try again.";
            }
        }
        request.setAttribute("error", error);
        preserveForm(request);
        loadReservationForm(request);
        request.getRequestDispatcher("/registerAppointment.jsp").forward(request, response);
    }

    private String reserve(String patientName, String address, String contact,
            int dentistId, int treatmentId, LocalDate date, LocalTime time,
            HttpServletRequest request) throws SQLException, IOException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String validation = validateSlot(conn, dentistId, treatmentId, date, time);
                if (validation != null) { conn.rollback(); return validation; }
                Integer patientUserId = findPatient(conn, contact);
                String sql = "INSERT INTO appointments (patient_user_id,appointment_number,"
                        + "patient_name,address,contact_number,dentist_id,treatment_id,"
                        + "appointment_date,appointment_time,status) VALUES (?,NULL,?,?,?,?,?,?,?,'Confirmed')";
                int appointmentId;
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    if (patientUserId == null) stmt.setNull(1, java.sql.Types.INTEGER); else stmt.setInt(1, patientUserId);
                    stmt.setString(2, patientName); stmt.setString(3, address); stmt.setString(4, contact);
                    stmt.setInt(5, dentistId); stmt.setInt(6, treatmentId);
                    stmt.setString(7, date.toString()); stmt.setTime(8, Time.valueOf(time)); stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Appointment ID was not generated.");
                        appointmentId = keys.getInt(1);
                    }
                }
                String number = String.format("APT%04d", appointmentId);
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE appointments SET appointment_number=? WHERE appointment_id=?")) {
                    stmt.setString(1, number); stmt.setInt(2, appointmentId); stmt.executeUpdate();
                }
                conn.commit();
                request.getSession().setAttribute("adminReservationMessage",
                        number + " reserved successfully. Payment status: Unpaid. The cashier must collect payment and print the receipt.");
                return "SUCCESS:" + number;
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); throw e;
            } finally { conn.setAutoCommit(true); }
        }
    }

    private String validateSlot(Connection conn, int dentistId, int treatmentId,
            LocalDate date, LocalTime time) throws SQLException {
        if (date.isBefore(LocalDate.now()) || (date.equals(LocalDate.now()) && !time.isAfter(LocalTime.now())))
            return "Choose a future appointment time.";
        String sql = "SELECT d.dentist_name,d.available_day,d.available_from,d.available_to "
                + "FROM dentists d JOIN dentist_treatments dt ON d.dentist_id=dt.dentist_id "
                + "WHERE d.dentist_id=? AND dt.treatment_id=? AND d.status='Active'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId); stmt.setInt(2, treatmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return "The selected dentist does not provide this treatment.";
                String day = rs.getString("available_day");
                Time fromValue = rs.getTime("available_from"), toValue = rs.getTime("available_to");
                if (day == null || fromValue == null || toValue == null) return "The dentist has no visiting schedule.";
                LocalTime from = fromValue.toLocalTime(), to = toValue.toLocalTime();
                long minutes = (time.toSecondOfDay() - from.toSecondOfDay()) / 60;
                if (!date.getDayOfWeek().name().equalsIgnoreCase(day))
                    return rs.getString("dentist_name") + " visits on " + day + "s.";
                if (time.isBefore(from) || time.plusMinutes(SLOT_MINUTES).isAfter(to) || minutes % SLOT_MINUTES != 0)
                    return "Choose a 30-minute slot within the dentist's visiting hours.";
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM appointments WHERE dentist_id=? AND appointment_date=? AND appointment_time=? "
                + "AND status NOT IN ('Rejected','Cancelled') LIMIT 1")) {
            stmt.setInt(1, dentistId); stmt.setString(2, date.toString()); stmt.setTime(3, Time.valueOf(time));
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return "That appointment slot is already reserved."; }
        }
        return null;
    }

    private Integer findPatient(Connection conn, String contact) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id FROM users WHERE phone_number=? AND role='Patient' ORDER BY user_id LIMIT 1")) {
            stmt.setString(1, contact); try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
        }
    }

    /** Loads the same staged treatment, eligible-dentist and live-slot data used by patient booking. */
    private void loadReservationForm(HttpServletRequest request) {
        request.setAttribute("treatments", rows("SELECT treatment_id id,treatment_name name,"
                + "CONCAT('LKR ',FORMAT(treatment_cost,2)) detail FROM treatments ORDER BY treatment_name"));
        Integer treatmentId=integer(request.getParameter("treatmentId"));
        Integer dentistId=integer(request.getParameter("dentistId"));
        LocalDate date=parseDate(request.getParameter("appointmentDate"));
        request.setAttribute("selectedTreatmentId",treatmentId);request.setAttribute("selectedDentistId",dentistId);
        request.setAttribute("selectedDate",date);request.setAttribute("minimumDate",LocalDate.now());
        if(treatmentId!=null)loadEligibleDentists(treatmentId,request);
        if(treatmentId!=null&&dentistId!=null&&date!=null)loadAvailableSlots(treatmentId,dentistId,date,request);
        Object message = request.getSession().getAttribute("adminReservationMessage");
        if (message != null) { request.setAttribute("successMessage", message); request.getSession().removeAttribute("adminReservationMessage"); }
    }

    private void loadEligibleDentists(int treatmentId,HttpServletRequest request){List<Map<String,Object>> values=new ArrayList<>();
        String sql="SELECT d.dentist_id,d.dentist_name,d.specialization,d.available_day,d.available_from,d.available_to "
                +"FROM dentists d JOIN dentist_treatments dt ON d.dentist_id=dt.dentist_id WHERE dt.treatment_id=? "
                +"AND d.status='Active' AND d.available_day IS NOT NULL AND d.available_from IS NOT NULL AND d.available_to IS NOT NULL ORDER BY d.dentist_name";
        try(Connection conn=DBConnection.getConnection();PreparedStatement stmt=conn.prepareStatement(sql)){stmt.setInt(1,treatmentId);
            try(ResultSet rs=stmt.executeQuery()){while(rs.next()){Map<String,Object> d=new HashMap<>();d.put("dentistId",rs.getInt("dentist_id"));
                d.put("dentistName",html(rs.getString("dentist_name")));d.put("specialization",html(rs.getString("specialization")));
                d.put("availableDay",html(rs.getString("available_day")));d.put("availableFrom",rs.getTime("available_from").toLocalTime().format(DISPLAY_TIME));
                d.put("availableTo",rs.getTime("available_to").toLocalTime().format(DISPLAY_TIME));values.add(d);}}}
        catch(SQLException e){getServletContext().log("Unable to load eligible dentists.",e);request.setAttribute("error","Unable to load available doctors.");}
        request.setAttribute("dentists",values);}

    private void loadAvailableSlots(int treatmentId,int dentistId,LocalDate date,HttpServletRequest request){List<Map<String,String>> slots=new ArrayList<>();
        String sql="SELECT d.dentist_name,d.available_day,d.available_from,d.available_to FROM dentists d JOIN dentist_treatments dt "
                +"ON d.dentist_id=dt.dentist_id WHERE d.dentist_id=? AND dt.treatment_id=? AND d.status='Active'";
        try(Connection conn=DBConnection.getConnection();PreparedStatement stmt=conn.prepareStatement(sql)){stmt.setInt(1,dentistId);stmt.setInt(2,treatmentId);
            try(ResultSet rs=stmt.executeQuery()){if(!rs.next()){request.setAttribute("error","The selected doctor is not available for this treatment.");request.setAttribute("availableSlots",slots);return;}
                String name=rs.getString("dentist_name"),day=rs.getString("available_day");LocalTime from=rs.getTime("available_from").toLocalTime(),to=rs.getTime("available_to").toLocalTime();
                request.setAttribute("selectedDentistName",html(name));request.setAttribute("visitingWindow",from.format(DISPLAY_TIME)+" - "+to.format(DISPLAY_TIME));
                if(date.isBefore(LocalDate.now()))request.setAttribute("scheduleMessage","Please select a future date.");
                else if(!date.getDayOfWeek().name().equalsIgnoreCase(day))request.setAttribute("scheduleMessage",name+" visits on "+day+"s. Please choose a "+day+".");
                else{Set<LocalTime> reserved=reservedTimes(conn,dentistId,date);for(LocalTime slot=from;!slot.plusMinutes(SLOT_MINUTES).isAfter(to);slot=slot.plusMinutes(SLOT_MINUTES)){
                    boolean future=!date.equals(LocalDate.now())||slot.isAfter(LocalTime.now());if(future&&!reserved.contains(slot)){Map<String,String> row=new HashMap<>();row.put("value",slot.toString());row.put("label",slot.format(DISPLAY_TIME));slots.add(row);}}}}}
        catch(SQLException|RuntimeException e){getServletContext().log("Unable to load live slots.",e);request.setAttribute("error","Unable to load available time slots.");}
        request.setAttribute("availableSlots",slots);}

    private Set<LocalTime> reservedTimes(Connection conn,int dentistId,LocalDate date)throws SQLException{Set<LocalTime> values=new HashSet<>();
        try(PreparedStatement stmt=conn.prepareStatement("SELECT appointment_time FROM appointments WHERE dentist_id=? AND appointment_date=? AND status NOT IN ('Rejected','Cancelled')")){
            stmt.setInt(1,dentistId);stmt.setString(2,date.toString());try(ResultSet rs=stmt.executeQuery()){while(rs.next())values.add(rs.getTime(1).toLocalTime());}}return values;}

    private List<Map<String, Object>> rows(String sql) {
        List<Map<String, Object>> values = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) { Map<String,Object> row=new HashMap<>(); row.put("id",rs.getInt("id"));
                row.put("name",html(rs.getString("name"))); row.put("detail",html(rs.getString("detail"))); values.add(row); }
        } catch (SQLException e) { getServletContext().log("Unable to load appointment choices.", e); }
        return values;
    }

    private void preserveForm(HttpServletRequest request) {
        for (String name : new String[]{"patientName","address","contactNumber","dentistId","treatmentId","appointmentDate","appointmentTime"})
            request.setAttribute("form_" + name, html(request.getParameter(name)));
    }
    private boolean canBook(HttpSession s) { if (s==null||s.getAttribute("username")==null)return false;
        String role=(String)s.getAttribute("role"); return "Admin".equalsIgnoreCase(role)||"Staff".equalsIgnoreCase(role); }
    private Integer integer(String v){try{return v==null?null:Integer.valueOf(v);}catch(NumberFormatException e){return null;}}
    private LocalDate parseDate(String v){try{return v==null||v.isBlank()?null:LocalDate.parse(v);}catch(RuntimeException e){return null;}}
    private String clean(String v){return v==null||v.isBlank()?null:v.trim();}
    private String html(String v){if(v==null)return "";return v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
