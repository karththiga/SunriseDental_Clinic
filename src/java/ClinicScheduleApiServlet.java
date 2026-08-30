import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP JSON web service for external clients.
 * Example: GET /api/schedules?date=2026-08-31
 */
@WebServlet("/api/schedules")
public class ClinicScheduleApiServlet extends HttpServlet {
    private final ClinicScheduleService service = new ClinicScheduleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String value = request.getParameter("date");
        try {
            LocalDate date = value == null || value.isBlank()
                    ? LocalDate.now() : LocalDate.parse(value.trim());
            List<ScheduleEntry> entries = service.getSchedule(date);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(toJson(date, entries));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + json(e.getMessage()) + "\"}");
        } catch (SQLException e) {
            getServletContext().log("Schedule API database failure.", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"error\":\"Schedule data is temporarily unavailable.\"}");
        }
    }

    private String toJson(LocalDate date, List<ScheduleEntry> entries) {
        StringBuilder out = new StringBuilder();
        out.append("{\"date\":\"").append(date).append("\",\"dentists\":[");
        for (int i = 0; i < entries.size(); i++) {
            ScheduleEntry item = entries.get(i);
            if (i > 0) out.append(',');
            out.append("{\"dentistId\":").append(item.getDentistId())
                    .append(",\"name\":\"").append(json(item.getDentistName()))
                    .append("\",\"specialization\":\"").append(json(item.getSpecialization()))
                    .append("\",\"availableFrom\":\"").append(item.getAvailableFrom())
                    .append("\",\"availableTo\":\"").append(item.getAvailableTo())
                    .append("\",\"consultationFee\":").append(item.getConsultationFee())
                    .append(",\"bookedSlots\":").append(item.getBookedSlots())
                    .append(",\"availableSlots\":").append(item.getAvailableSlots()).append('}');
        }
        return out.append("]}").toString();
    }

    private String json(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
