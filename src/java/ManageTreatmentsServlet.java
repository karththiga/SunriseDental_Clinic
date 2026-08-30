import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

/** Admin CRUD screen for treatment names and prices used by appointments. */
@WebServlet("/ManageTreatmentsServlet")
public class ManageTreatmentsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request.getSession(false))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String editId = request.getParameter("edit");
        if (editId != null && !editId.isBlank()) {
            loadTreatmentForEdit(request, editId);
        }
        if ("saved".equals(request.getParameter("success"))) {
            request.setAttribute("success", "Treatment saved successfully.");
        } else if ("deleted".equals(request.getParameter("success"))) {
            request.setAttribute("success", "Treatment deleted successfully.");
        }
        loadTreatments(request);
        request.getRequestDispatcher("/manageTreatments.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request.getSession(false))) {
            response.sendRedirect("login.jsp");
            return;
        }

        if ("delete".equals(request.getParameter("action"))) {
            String deleteError = deleteTreatment(request.getParameter("treatmentId"));
            if (deleteError == null) {
                response.sendRedirect("ManageTreatmentsServlet?success=deleted");
                return;
            }
            request.setAttribute("error", deleteError);
            loadTreatments(request);
            request.getRequestDispatcher("/manageTreatments.jsp").forward(request, response);
            return;
        }

        String treatmentId = clean(request.getParameter("treatmentId"));
        String treatmentName = clean(request.getParameter("treatmentName"));
        String costValue = clean(request.getParameter("treatmentCost"));
        String error = null;
        BigDecimal cost = null;

        if (treatmentName == null || costValue == null) {
            error = "Enter the treatment name and cost.";
        } else {
            try {
                cost = new BigDecimal(costValue);
                if (cost.signum() < 0) {
                    error = "Treatment cost cannot be negative.";
                }
            } catch (NumberFormatException e) {
                error = "Enter a valid treatment cost.";
            }
        }

        if (error == null) {
            try (Connection conn = DBConnection.getConnection()) {
                if (nameExists(conn, treatmentName, treatmentId)) {
                    error = "A treatment with this name already exists.";
                } else if (treatmentId == null) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO treatments (treatment_name,treatment_cost) VALUES (?,?)")) {
                        stmt.setString(1, treatmentName);
                        stmt.setBigDecimal(2, cost);
                        stmt.executeUpdate();
                    }
                } else {
                    int id = Integer.parseInt(treatmentId);
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE treatments SET treatment_name=?,treatment_cost=? WHERE treatment_id=?")) {
                        stmt.setString(1, treatmentName);
                        stmt.setBigDecimal(2, cost);
                        stmt.setInt(3, id);
                        if (stmt.executeUpdate() == 0) {
                            error = "The selected treatment no longer exists.";
                        }
                    }
                }
            } catch (NumberFormatException e) {
                error = "Invalid treatment identifier.";
            } catch (SQLException e) {
                getServletContext().log("Unable to save treatment.", e);
                error = "Unable to save the treatment. Please try again.";
            }
        }

        if (error == null) {
            response.sendRedirect("ManageTreatmentsServlet?success=saved");
            return;
        }

        request.setAttribute("error", error);
        request.setAttribute("editTreatmentId", treatmentId);
        request.setAttribute("editTreatmentName", escapeHtml(treatmentName));
        request.setAttribute("editTreatmentCost", escapeHtml(costValue));
        loadTreatments(request);
        request.getRequestDispatcher("/manageTreatments.jsp").forward(request, response);
    }

    private void loadTreatmentForEdit(HttpServletRequest request, String idValue) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT treatment_id,treatment_name,treatment_cost FROM treatments WHERE treatment_id=?")) {
            stmt.setInt(1, Integer.parseInt(idValue));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    request.setAttribute("editTreatmentId", rs.getString("treatment_id"));
                    request.setAttribute("editTreatmentName", escapeHtml(rs.getString("treatment_name")));
                    request.setAttribute("editTreatmentCost", rs.getString("treatment_cost"));
                } else {
                    request.setAttribute("error", "Treatment not found.");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            request.setAttribute("error", "Unable to load the selected treatment.");
        }
    }

    /**
     * Preserves historical appointment and billing records by allowing hard
     * deletion only when a treatment has never been used by an appointment.
     */
    private String deleteTreatment(String idValue) {
        int id;
        try {
            id = Integer.parseInt(idValue);
        } catch (NumberFormatException | NullPointerException e) {
            return "Invalid treatment identifier.";
        }

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement used = conn.prepareStatement(
                    "SELECT 1 FROM appointments WHERE treatment_id=? LIMIT 1")) {
                used.setInt(1, id);
                try (ResultSet rs = used.executeQuery()) {
                    if (rs.next()) {
                        return "This treatment has appointment history and cannot be deleted. Update its name or price instead.";
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM treatments WHERE treatment_id=?")) {
                stmt.setInt(1, id);
                if (stmt.executeUpdate() == 0) {
                    return "The selected treatment no longer exists.";
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to delete treatment.", e);
            return "Unable to delete the treatment. Please try again.";
        }
        return null;
    }

    private void loadTreatments(HttpServletRequest request) {
        List<Map<String, Object>> treatments = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT treatment_id,treatment_name,treatment_cost FROM treatments ORDER BY treatment_name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("treatment_id"));
                row.put("name", escapeHtml(rs.getString("treatment_name")));
                row.put("cost", rs.getBigDecimal("treatment_cost"));
                treatments.add(row);
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to load treatments.", e);
            request.setAttribute("error", "Unable to load treatments.");
        }
        request.setAttribute("treatments", treatments);
    }

    private boolean nameExists(Connection conn, String name, String excludedId)
            throws SQLException {
        String sql = "SELECT treatment_id FROM treatments WHERE LOWER(treatment_name)=LOWER(?)"
                + (excludedId == null ? "" : " AND treatment_id<>?");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            if (excludedId != null) {
                stmt.setInt(2, Integer.parseInt(excludedId));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isAdmin(HttpSession session) {
        return session != null && session.getAttribute("username") != null
                && "Admin".equalsIgnoreCase((String) session.getAttribute("role"));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
