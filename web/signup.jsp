<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String suffix = "?auth=signup";
    if (request.getParameter("error") != null) {
        suffix += "&error=" + java.net.URLEncoder.encode(
                request.getParameter("error"), "UTF-8");
    }
    response.sendRedirect(request.getContextPath() + "/HomeServlet" + suffix);
    return;
%>
