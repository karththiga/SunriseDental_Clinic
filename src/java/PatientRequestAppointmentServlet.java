import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

@WebServlet("/PatientRequestAppointmentServlet")
public class PatientRequestAppointmentServlet extends HttpServlet {

    private static final int SLOT_MINUTES = 30;
    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("h:mm a");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isPatient(request)) {
            response.sendRedirect("login.jsp");
            return;
        }

        loadReservationForm(request);
        request.getRequestDispatcher("/patientRequestAppointment.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isPatient(request)) {
            response.sendRedirect("login.jsp");
            return;
        }

        Integer treatmentId = parseInteger(request.getParameter("treatmentId"));
        Integer dentistId = parseInteger(request.getParameter("dentistId"));
        LocalDate appointmentDate = parseDate(
                request.getParameter("appointmentDate")
        );
        LocalTime appointmentTime = parseTime(
                request.getParameter("appointmentTime")
        );

        if (treatmentId == null || dentistId == null
                || appointmentDate == null || appointmentTime == null) {
            showFormError(
                    request,
                    response,
                    "Please select a treatment, dentist, date and available time slot."
            );
            return;
        }

        if (appointmentDate.isBefore(LocalDate.now())
                || (appointmentDate.equals(LocalDate.now())
                    && !appointmentTime.isAfter(LocalTime.now()))) {
            showFormError(
                    request,
                    response,
                    "Please select a future appointment slot."
            );
            return;
        }

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("user_id");

        PendingReservation pending = new PendingReservation(
                userId, treatmentId, dentistId, appointmentDate, appointmentTime);
        session.setAttribute("pendingReservation", pending);
        response.sendRedirect("PatientPaymentServlet?checkout="
                + pending.getCheckoutToken());
    }

    private void loadReservationForm(HttpServletRequest request) {
        loadTreatments(request);

        Integer treatmentId = parseInteger(request.getParameter("treatmentId"));
        Integer dentistId = parseInteger(request.getParameter("dentistId"));
        LocalDate appointmentDate = parseDate(
                request.getParameter("appointmentDate")
        );

        request.setAttribute("selectedTreatmentId", treatmentId);
        request.setAttribute("selectedDentistId", dentistId);
        request.setAttribute("selectedDate", appointmentDate);
        request.setAttribute("minimumDate", LocalDate.now().toString());

        if (treatmentId != null) {
            loadDentistsByTreatment(treatmentId, request);
        }

        if (treatmentId != null && dentistId != null
                && appointmentDate != null) {
            try {
                loadAvailableSlots(
                        treatmentId,
                        dentistId,
                        appointmentDate,
                        request
                );
            } catch (SQLException e) {
                getServletContext().log("Unable to load appointment slots.", e);
                request.setAttribute(
                        "error",
                        "Unable to load appointment slots. Please try again."
                );
            }
        }
    }

    private void loadAvailableSlots(
            int treatmentId,
            int dentistId,
            LocalDate date,
            HttpServletRequest request)
            throws SQLException {

        List<Map<String, String>> slots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            DentistAvailability availability = getDentistAvailability(
                    conn,
                    dentistId,
                    treatmentId,
                    false
            );

            if (availability == null) {
                request.setAttribute(
                        "error",
                        "The selected dentist is not available for this treatment."
                );
            } else if (date.isBefore(LocalDate.now())) {
                request.setAttribute("error", "Please select a future date.");
            } else if (availability.availableDay == null
                    || !date.getDayOfWeek().name()
                            .equalsIgnoreCase(availability.availableDay)) {
                request.setAttribute(
                        "scheduleMessage",
                        availability.dentistName + " visits on "
                        + availability.availableDay + "s. Please choose a "
                        + availability.availableDay + "."
                );
            } else {
                Set<LocalTime> reserved = loadReservedTimes(conn, dentistId, date);
                LocalTime slot = availability.availableFrom;

                while (slot != null
                        && availability.availableTo != null
                        && !slot.plusMinutes(SLOT_MINUTES)
                                .isAfter(availability.availableTo)) {
                    boolean isFuture = !date.equals(LocalDate.now())
                            || slot.isAfter(LocalTime.now());

                    if (isFuture && !reserved.contains(slot)) {
                        Map<String, String> row = new HashMap<>();
                        row.put("value", slot.toString());
                        row.put("label", slot.format(DISPLAY_TIME));
                        slots.add(row);
                    }
                    slot = slot.plusMinutes(SLOT_MINUTES);
                }

                request.setAttribute(
                        "selectedDentistName",
                        availability.dentistName
                );
                request.setAttribute(
                        "visitingWindow",
                        availability.availableFrom.format(DISPLAY_TIME)
                        + " - " + availability.availableTo.format(DISPLAY_TIME)
                );
            }
        }

        request.setAttribute("availableSlots", slots);
    }

    private Set<LocalTime> loadReservedTimes(
            Connection conn,
            int dentistId,
            LocalDate date)
            throws SQLException {

        Set<LocalTime> reserved = new HashSet<>();
        String sql =
                "SELECT appointment_time FROM appointments "
                + "WHERE dentist_id = ? AND appointment_date = ? "
                + "AND status <> 'Rejected'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId);
            stmt.setString(2, date.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reserved.add(rs.getTime("appointment_time").toLocalTime());
                }
            }
        }

        return reserved;
    }

    private DentistAvailability getDentistAvailability(
            Connection conn,
            int dentistId,
            int treatmentId,
            boolean lockForReservation)
            throws SQLException {

        String sql =
                "SELECT d.dentist_name, d.available_day, "
                + "d.available_from, d.available_to "
                + "FROM dentists d "
                + "INNER JOIN dentist_treatments dt "
                + "ON d.dentist_id = dt.dentist_id "
                + "WHERE d.dentist_id = ? AND dt.treatment_id = ? "
                + "AND d.status = 'Active' "
                + "AND d.available_day IS NOT NULL "
                + "AND d.available_from IS NOT NULL "
                + "AND d.available_to IS NOT NULL"
                + (lockForReservation ? " FOR UPDATE" : "");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId);
            stmt.setInt(2, treatmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Time from = rs.getTime("available_from");
                Time to = rs.getTime("available_to");

                return new DentistAvailability(
                        rs.getString("dentist_name"),
                        rs.getString("available_day"),
                        from == null ? null : from.toLocalTime(),
                        to == null ? null : to.toLocalTime()
                );
            }
        }
    }

    private void loadDentistsByTreatment(
            int treatmentId,
            HttpServletRequest request) {

        List<Map<String, Object>> dentists = new ArrayList<>();
        String sql =
                "SELECT d.dentist_id, d.dentist_name, d.specialization, "
                + "d.available_day, d.available_from, d.available_to "
                + "FROM dentists d "
                + "INNER JOIN dentist_treatments dt "
                + "ON d.dentist_id = dt.dentist_id "
                + "WHERE dt.treatment_id = ? AND d.status = 'Active' "
                + "AND d.available_day IS NOT NULL "
                + "AND d.available_from IS NOT NULL "
                + "AND d.available_to IS NOT NULL "
                + "ORDER BY d.dentist_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, treatmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dentist = new HashMap<>();
                    dentist.put("dentistId", rs.getInt("dentist_id"));
                    dentist.put("dentistName", rs.getString("dentist_name"));
                    dentist.put("specialization", rs.getString("specialization"));
                    dentist.put("availableDay", rs.getString("available_day"));
                    dentist.put("availableFrom", rs.getTime("available_from"));
                    dentist.put("availableTo", rs.getTime("available_to"));
                    dentists.add(dentist);
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load dentists.", e);
            request.setAttribute("error", "Unable to load dentists.");
        }

        request.setAttribute("dentists", dentists);
    }

    private Map<String, String> getPatientDetails(
            Connection conn,
            int userId)
            throws SQLException {

        String sql =
                "SELECT first_name, last_name, phone_number FROM users "
                + "WHERE user_id = ? AND role = 'Patient'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, String> patient = new HashMap<>();
                patient.put(
                        "name",
                        rs.getString("first_name") + " "
                        + rs.getString("last_name")
                );
                patient.put("phone", rs.getString("phone_number"));
                return patient;
            }
        }
    }

    private void loadTreatments(HttpServletRequest request) {
        List<Map<String, Object>> treatments = new ArrayList<>();
        String sql =
                "SELECT treatment_id, treatment_name, treatment_cost "
                + "FROM treatments ORDER BY treatment_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> treatment = new HashMap<>();
                treatment.put("treatmentId", rs.getInt("treatment_id"));
                treatment.put("treatmentName", rs.getString("treatment_name"));
                treatment.put("treatmentCost", rs.getBigDecimal("treatment_cost"));
                treatments.add(treatment);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load treatments.", e);
            request.setAttribute("error", "Unable to load treatments.");
        }

        request.setAttribute("treatments", treatments);
    }

    private void showFormError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);
        loadReservationForm(request);
        request.getRequestDispatcher("/patientRequestAppointment.jsp")
                .forward(request, response);
    }

    private Integer parseInteger(String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank()
                    ? null : LocalDate.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return value == null || value.isBlank()
                    ? null : LocalTime.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private boolean isPatient(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null
                && session.getAttribute("username") != null
                && "Patient".equalsIgnoreCase(
                        (String) session.getAttribute("role")
                );
    }

    private static class DentistAvailability {
        private final String dentistName;
        private final String availableDay;
        private final LocalTime availableFrom;
        private final LocalTime availableTo;

        DentistAvailability(
                String dentistName,
                String availableDay,
                LocalTime availableFrom,
                LocalTime availableTo) {
            this.dentistName = dentistName;
            this.availableDay = availableDay;
            this.availableFrom = availableFrom;
            this.availableTo = availableTo;
        }
    }
}
