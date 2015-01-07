<%@page session="false"%>
<%@ page contentType="text/html"
             pageEncoding="utf-8"
             import="java.util.Iterator,
                    com.day.cq.i18n.I18n,
                    com.day.text.Text" %><%
%><%@include file="/libs/foundation/global.jsp"%><%
I18n i18n = new I18n(slingRequest);
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN">
<html>
<head>
    <title><%=i18n.get("AEM Importer")%></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<body>
    <h1><%=i18n.get("Importer")%></h1>
    <cq:include path="upload-content" resourceType="/apps/aem-importer/components/upload-content"/>
</body>
</html>
