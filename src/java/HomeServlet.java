import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/HomeServlet")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        LocalDate today = LocalDate.now();
        DayOfWeek day = today.getDayOfWeek();

        request.setAttribute(
                "today",
                today.format(DateTimeFormatter.ofPattern(
                        "EEEE, dd MMMM yyyy",
                        Locale.ENGLISH
                ))
        );

        request.setAttribute(
                "clinicOpen",
                day != DayOfWeek.SUNDAY
        );

        request.setAttribute(
                "visitingHours",
                getVisitingHours(day)
        );

        loadActiveDentists(request);

        request.getRequestDispatcher("/home.jsp")
                .forward(request, response);
    }

    private String getVisitingHours(DayOfWeek day) {
        if (day == DayOfWeek.SUNDAY) {
            return "Closed today";
        }

        if (day == DayOfWeek.SATURDAY) {
            return "8:30 AM - 1:00 PM";
        }

        return "9:00 AM - 1:00 PM & 3:00 PM - 7:00 PM";
    }

    private void loadActiveDentists(HttpServletRequest request) {
        List<Map<String, String>> dentists = new ArrayList<>();

        String sql =
                "SELECT dentist_name, specialization "
                + "FROM dentists "
                + "WHERE status = 'Active' "
                + "ORDER BY dentist_name";

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, String> dentist = new HashMap<>();
                dentist.put(
                        "name",
                        escapeHtml(rs.getString("dentist_name"))
                );

                String specialization = rs.getString("specialization");
                dentist.put(
                        "specialization",
                        specialization == null || specialization.trim().isEmpty()
                                ? "General Dentistry"
                                : escapeHtml(specialization)
                );

                dentists.add(dentist);
            }
        } catch (SQLException e) {
            // The public homepage must remain available even when the
            // database is temporarily unavailable.
            getServletContext().log(
                    "Unable to load dentists for the public homepage.",
                    e
            );
            request.setAttribute("scheduleUnavailable", true);
        }

        request.setAttribute("dentists", dentists);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
