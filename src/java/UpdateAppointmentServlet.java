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

@WebServlet("/UpdateAppointmentServlet")
public class UpdateAppointmentServlet extends HttpServlet {

    private static final int SLOT_MINUTES = 30;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect("login.jsp");
            return;
        }

        if ("undo".equals(request.getParameter("action"))) {
            undoAppointment(request, session);
        } else {
            String appointmentNumber = request.getParameter("appointmentNumber");
            if (appointmentNumber == null || appointmentNumber.isBlank()) {
                response.sendRedirect("ManageAppointmentsServlet");
                return;
            }

            try {
                loadAppointment(appointmentNumber.trim(), request);
            } catch (SQLException e) {
                log("Unable to load appointment for update.", e);
                request.setAttribute("error", "Unable to load appointment.");
            }
        }

        loadReferenceData(request);
        request.getRequestDispatcher("updateAppointment.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect("login.jsp");
            return;
        }

        String appointmentNumber = clean(request.getParameter("appointmentNumber"));
        String appointmentDate = clean(request.getParameter("appointmentDate"));
        String appointmentTime = clean(request.getParameter("appointmentTime"));
        String status = clean(request.getParameter("status"));
        Integer dentistId = parseInteger(request.getParameter("dentistId"));
        Integer treatmentId = parseInteger(request.getParameter("treatmentId"));

        if (appointmentNumber == null || appointmentDate == null
                || appointmentTime == null || status == null
                || dentistId == null || treatmentId == null) {
            request.setAttribute("error", "Please complete all appointment fields.");
            reloadAppointment(appointmentNumber, request);
            forwardForm(request, response);
            return;
        }

        if (!isAllowedStatus(status)) {
            request.setAttribute("error", "Invalid appointment status.");
            reloadAppointment(appointmentNumber, request);
            forwardForm(request, response);
            return;
        }

        if ("Cancelled".equalsIgnoreCase(status)) {
            request.setAttribute("error",
                    "Use Cancel from Manage Appointments so any payment is refunded and the patient is notified.");
            reloadAppointment(appointmentNumber, request);
            forwardForm(request, response);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(appointmentDate);
            LocalTime time = LocalTime.parse(appointmentTime);

            String validationError = validateSelection(
                    appointmentNumber,
                    dentistId,
                    treatmentId,
                    date,
                    time,
                    status
            );

            if (validationError != null) {
                request.setAttribute("error", validationError);
                loadAppointment(appointmentNumber, request);
                forwardForm(request, response);
                return;
            }

            AppointmentMemento oldState = getCurrentState(appointmentNumber);
            if (oldState == null) {
                request.setAttribute("error", "Appointment not found.");
                forwardForm(request, response);
                return;
            }

            // Memento Pattern: capture the current values before mutation so
            // the administrator can undo this update once.
            AppointmentCaretaker caretaker = new AppointmentCaretaker();
            caretaker.save(oldState);
            session.setAttribute("appointmentCaretaker", caretaker);

            String sql =
                    "UPDATE appointments SET dentist_id=?, treatment_id=?, "
                    + "appointment_date=?, appointment_time=?, status=? "
                    + "WHERE appointment_number=?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, dentistId);
                stmt.setInt(2, treatmentId);
                stmt.setString(3, appointmentDate);
                stmt.setTime(4, Time.valueOf(time));
                stmt.setString(5, status);
                stmt.setString(6, appointmentNumber);
                stmt.executeUpdate();
            }

            request.setAttribute("success", "Appointment updated successfully.");
            request.setAttribute("canUndo", true);
            loadAppointment(appointmentNumber, request);
        } catch (RuntimeException e) {
            request.setAttribute("error", "Invalid appointment date or time.");
            reloadAppointment(appointmentNumber, request);
        } catch (SQLException e) {
            log("Unable to update appointment.", e);
            request.setAttribute("error", "Unable to update appointment.");
            reloadAppointment(appointmentNumber, request);
        }

        forwardForm(request, response);
    }

    private String validateSelection(
            String appointmentNumber,
            int dentistId,
            int treatmentId,
            LocalDate date,
            LocalTime time,
            String status)
            throws SQLException {

        String dentistSql =
                "SELECT d.dentist_name, d.available_day, "
                + "d.available_from, d.available_to "
                + "FROM dentists d INNER JOIN dentist_treatments dt "
                + "ON d.dentist_id = dt.dentist_id "
                + "WHERE d.dentist_id=? AND dt.treatment_id=? "
                + "AND d.status='Active'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dentistSql)) {
            stmt.setInt(1, dentistId);
            stmt.setInt(2, treatmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return "The selected dentist does not provide this treatment.";
                }

                if (isActiveStatus(status)) {
                    String day = rs.getString("available_day");
                    Time fromValue = rs.getTime("available_from");
                    Time toValue = rs.getTime("available_to");

                    if (day == null || fromValue == null || toValue == null) {
                        return "The selected dentist has no visiting schedule configured.";
                    }

                    LocalTime from = fromValue.toLocalTime();
                    LocalTime to = toValue.toLocalTime();
                    long minutes = (time.toSecondOfDay() - from.toSecondOfDay()) / 60;

                    if (!date.getDayOfWeek().name().equalsIgnoreCase(day)) {
                        return rs.getString("dentist_name") + " visits on " + day + "s.";
                    }
                    if (date.isBefore(LocalDate.now())) {
                        return "An active appointment cannot be moved to a past date.";
                    }
                    if (time.isBefore(from)
                            || time.plusMinutes(SLOT_MINUTES).isAfter(to)
                            || minutes % SLOT_MINUTES != 0) {
                        return "Choose a 30-minute slot within the dentist's visiting hours.";
                    }
                }
            }

            if (isActiveStatus(status)) {
                String conflictSql =
                        "SELECT appointment_id FROM appointments "
                        + "WHERE dentist_id=? AND appointment_date=? "
                        + "AND appointment_time=? AND appointment_number<>? "
                        + "AND status NOT IN ('Rejected','Cancelled')";

                try (PreparedStatement conflict = conn.prepareStatement(conflictSql)) {
                    conflict.setInt(1, dentistId);
                    conflict.setString(2, date.toString());
                    conflict.setTime(3, Time.valueOf(time));
                    conflict.setString(4, appointmentNumber);
                    try (ResultSet rs = conflict.executeQuery()) {
                        if (rs.next()) {
                            return "That dentist already has an appointment in this slot.";
                        }
                    }
                }
            }
        }

        return null;
    }

    private AppointmentMemento getCurrentState(String appointmentNumber)
            throws SQLException {
        String sql =
                "SELECT dentist_id,treatment_id,appointment_date,"
                + "appointment_time,status FROM appointments "
                + "WHERE appointment_number=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, appointmentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AppointmentMemento(
                            appointmentNumber,
                            rs.getInt("dentist_id"),
                            rs.getInt("treatment_id"),
                            rs.getString("appointment_date"),
                            rs.getString("appointment_time"),
                            rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    private void undoAppointment(
            HttpServletRequest request,
            HttpSession session) {

        AppointmentCaretaker caretaker = (AppointmentCaretaker)
                session.getAttribute("appointmentCaretaker");

        if (caretaker == null || !caretaker.hasSavedState()) {
            request.setAttribute("error", "There is no previous update to undo.");
            return;
        }

        AppointmentMemento state = caretaker.getSavedState();
        String sql =
                "UPDATE appointments SET dentist_id=?,treatment_id=?,"
                + "appointment_date=?,appointment_time=?,status=? "
                + "WHERE appointment_number=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, state.getDentistId());
            stmt.setInt(2, state.getTreatmentId());
            stmt.setString(3, state.getAppointmentDate());
            stmt.setString(4, state.getAppointmentTime());
            stmt.setString(5, state.getStatus());
            stmt.setString(6, state.getAppointmentNumber());
            stmt.executeUpdate();
            caretaker.clear();
            request.setAttribute("success", "Previous appointment details restored.");
            loadAppointment(state.getAppointmentNumber(), request);
        } catch (SQLException e) {
            log("Unable to undo appointment update.", e);
            request.setAttribute("error", "Unable to restore the appointment.");
        }
    }

    private void loadAppointment(
            String appointmentNumber,
            HttpServletRequest request)
            throws SQLException {

        String sql =
                "SELECT appointment_number,patient_name,contact_number,"
                + "dentist_id,treatment_id,appointment_date,appointment_time,status "
                + "FROM appointments WHERE appointment_number=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, appointmentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    request.setAttribute("error", "Appointment not found.");
                    return;
                }
                request.setAttribute("found", true);
                request.setAttribute("appointmentNumber", rs.getString("appointment_number"));
                request.setAttribute("patientName", rs.getString("patient_name"));
                request.setAttribute("contactNumber", rs.getString("contact_number"));
                request.setAttribute("dentistId", rs.getInt("dentist_id"));
                request.setAttribute("treatmentId", rs.getInt("treatment_id"));
                request.setAttribute("appointmentDate", rs.getString("appointment_date"));
                request.setAttribute("appointmentTime", rs.getString("appointment_time"));
                request.setAttribute("status", rs.getString("status"));
            }
        }
    }

    private void loadReferenceData(HttpServletRequest request) {
        request.setAttribute(
                "dentists",
                loadRows(
                        "SELECT dentist_id,dentist_name,available_day "
                        + "FROM dentists WHERE status='Active' ORDER BY dentist_name",
                        "dentistId", "dentist_id",
                        "dentistName", "dentist_name",
                        "availableDay", "available_day"
                )
        );
        request.setAttribute(
                "treatments",
                loadRows(
                        "SELECT treatment_id,treatment_name FROM treatments "
                        + "ORDER BY treatment_name",
                        "treatmentId", "treatment_id",
                        "treatmentName", "treatment_name",
                        null, null
                )
        );
    }

    private List<Map<String, Object>> loadRows(
            String sql,
            String firstKey,
            String firstColumn,
            String secondKey,
            String secondColumn,
            String thirdKey,
            String thirdColumn) {

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put(firstKey, rs.getInt(firstColumn));
                row.put(secondKey, rs.getString(secondColumn));
                if (thirdKey != null) {
                    row.put(thirdKey, rs.getString(thirdColumn));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            log("Unable to load update form options.", e);
        }
        return rows;
    }

    private void reloadAppointment(
            String appointmentNumber,
            HttpServletRequest request) {
        if (appointmentNumber == null) {
            return;
        }
        try {
            loadAppointment(appointmentNumber, request);
        } catch (SQLException e) {
            log("Unable to reload appointment.", e);
        }
    }

    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        loadReferenceData(request);
        request.getRequestDispatcher("updateAppointment.jsp")
                .forward(request, response);
    }

    private boolean isAdmin(HttpSession session) {
        return session != null
                && session.getAttribute("username") != null
                && ("Admin".equalsIgnoreCase((String) session.getAttribute("role"))
                    || "Staff".equalsIgnoreCase((String) session.getAttribute("role")));
    }

    private boolean isActiveStatus(String status) {
        return !"Completed".equalsIgnoreCase(status)
                && !"Cancelled".equalsIgnoreCase(status)
                && !"Rejected".equalsIgnoreCase(status);
    }

    private boolean isAllowedStatus(String status) {
        return "Pending".equalsIgnoreCase(status)
                || "Confirmed".equalsIgnoreCase(status)
                || "Scheduled".equalsIgnoreCase(status)
                || "Completed".equalsIgnoreCase(status)
                || "Cancelled".equalsIgnoreCase(status)
                || "Rejected".equalsIgnoreCase(status);
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
