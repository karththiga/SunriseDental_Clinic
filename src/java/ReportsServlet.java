import java.io.IOException;
import java.sql.SQLException;
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

/** Presentation controller for the management decision-support report. */
@WebServlet("/ReportsServlet")
public class ReportsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null
                || !("Admin".equalsIgnoreCase((String) session.getAttribute("role"))
                     || "Staff".equalsIgnoreCase((String) session.getAttribute("role")))) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            ClinicReport report = new ClinicReportService().generateReport();
            request.setAttribute("reportAvailable", true);
            request.setAttribute("totalAppointments", report.getTotalAppointments());
            request.setAttribute("todaysAppointments", report.getTodaysAppointments());
            request.setAttribute("upcomingAppointments", report.getUpcomingAppointments());
            request.setAttribute("confirmedAppointments", report.getConfirmedAppointments());
            request.setAttribute("paidBills", report.getPaidBills());
            request.setAttribute("totalRevenue", report.getTotalRevenue());
            request.setAttribute("dentistWorkload", rows(report.getDentistWorkload()));
            request.setAttribute("treatmentPopularity", rows(report.getTreatmentPopularity()));
            request.setAttribute("dailyAppointments", rows(report.getDailyAppointments()));
        } catch (SQLException e) {
            getServletContext().log("Unable to generate clinic report.", e);
            request.setAttribute("error", "Unable to generate the report. Please check the database connection.");
        }
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private List<Map<String, Object>> rows(List<ClinicReport.MetricRow> source) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ClinicReport.MetricRow item : source) {
            Map<String, Object> row = new HashMap<>();
            row.put("label", item.getLabel());
            row.put("count", item.getCount());
            result.add(row);
        }
        return result;
    }
}
