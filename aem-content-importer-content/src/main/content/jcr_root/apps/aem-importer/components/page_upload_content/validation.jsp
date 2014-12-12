<%@page import="org.apache.commons.fileupload.util.Streams"%>
<%@page import="java.io.InputStream"%>
<%@page import="org.apache.sling.api.request.RequestParameter"%>
<%@page import="java.util.Map"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@ page import="java.util.Locale,
java.util.ResourceBundle,
javax.jcr.NodeIterator,
javax.jcr.Session,
javax.jcr.Node, javax.jcr.Value, javax.jcr.NodeIterator,
com.day.cq.commons.jcr.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/libs/foundation/global.jsp"%>

<% 

	String src = (String) request.getParameter("src");
	String target = (String) request.getParameter("target");
	String transformer = (String) request.getParameter("transformer");
	Boolean docToUpload = Boolean.parseBoolean(request.getParameter("docToUpload"));
	
	boolean error = false;
	String messageError = "";
	
	if (!docToUpload) {
		if (src.length() > 0) {
			try {
				Resource resources = slingRequest.getResourceResolver().getResource(src);
				Node node = resources.adaptTo(Node.class);
			} catch(Exception e) {
				error = true;
				messageError = "Check src/target. Some of them don't exist in repository.";
			}
		}
		
		if (target.length() > 0) {
			try {
				Resource resources = slingRequest.getResourceResolver().getResource(target);
				Node node = resources.adaptTo(Node.class);
			} catch(Exception e) {
				error = true;
				messageError = "Check src/target. Some of them don't exist in repository.";
			}
		}
		
		if (src.length() == 0 || target.length() == 0 || transformer.length() == 0) {
			error = true;
			messageError = "If you don't add a zip file or params transformer type, src, target are required. Check them";
		}
		
		
	} 
	
	
	
	
	if (!error) {

%>
{"error": "false"}
<%

	} else {
		%>
		
{"error": "true", "message":"<%=messageError %>"}
		<%
	}

%>