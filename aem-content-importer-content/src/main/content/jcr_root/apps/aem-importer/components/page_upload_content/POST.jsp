<%@page import="org.apache.sling.api.request.RequestParameter"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.fileupload.util.Streams"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.InputStream"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@include file="/libs/foundation/global.jsp"%>

<% 
//Check that we have a file upload request
final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 String error = "";
try {
	PrintWriter pout = response.getWriter();
if (isMultipart) {
	final Map<String, RequestParameter[]> params = slingRequest.getRequestParameterMap();
    for (final Map.Entry<String, RequestParameter[]> pairs : params.entrySet()) {
      final String k = pairs.getKey();
      final RequestParameter[] pArr = pairs.getValue();
      final RequestParameter param = pArr[0];
      final InputStream stream = param.getInputStream();
      /*if (param.isFormField()) {
        out.println("Form field " + k + " with value " + Streams.asString(stream) + " detected.");
      } else {
        out.println("File field " + k + " with file name " + param.getFileName() + " detected.");
      }*/
	}
    
    //TODO: Invoke the corresponding transformer
    
    %>
    
    {"error": "false"}
    
    <%
    
    
    
}
} catch(Exception e) {
	%>
	
	{"error": "true", "message": "Define here different info"}
	
	<%
}

 %>



<%

//    response.setStatus(response.SC_MOVED_TEMPORARILY);
//    response.setHeader("Location", currentPage.getPath() + ".html"); 
%>
