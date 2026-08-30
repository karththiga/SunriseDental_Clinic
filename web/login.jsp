<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String suffix = "?auth=login";
    if (request.getParameter("error") != null) {
        suffix += "&error=" + java.net.URLEncoder.encode(
                request.getParameter("error"), "UTF-8");
    }
    if (request.getParameter("registered") != null) {
        suffix += "&registered=true";
    }
    response.sendRedirect(request.getContextPath() + "/HomeServlet" + suffix);
    return;
%>
