<%@page import="org.apache.sling.commons.json.JSONObject"%>
<%@page import="com.day.cq.commons.JS"%>
<%@page import="com.adobe.aem.importer.DITATransformerHelper"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/libs/foundation/global.jsp"%>

<%
	Class<?>[] availableTransformers = DITATransformerHelper.getAvailableTransformers();
	JSONObject jsonObject = new JSONObject();
	if (availableTransformers.length > 0) {
		
		for (Class<?> cl : availableTransformers) {
		 	jsonObject.put(cl.getSimpleName().replace("Impl", ""), cl.getSimpleName());
		}
		
	}
 	
%>

<%=jsonObject.toString()%>

