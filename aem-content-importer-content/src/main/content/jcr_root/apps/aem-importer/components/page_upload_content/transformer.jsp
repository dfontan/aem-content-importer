<%@page import="java.util.Properties"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="com.adobe.aem.importer.DITATranformer"%>
<%@page import="com.adobe.aem.importer.DITATransformerHelper"%>
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

DITATranformer dt = DITATransformerHelper.getDITATransformer("com.adobe.aem.importer.impl.DITATransformerXSLTImpl");

Resource resources = slingRequest.getResourceResolver().getResource("/var/aem-importer/import");

Node srcPath = resources.adaptTo(Node.class);

Properties p = new Properties();

p.put("xslt", "/apps/aem-importer/resources/dita-to-content.xsl");
p.put("transformer","net.sf.saxon.TransformerFactoryImpl");
p.put("packageTpl","/apps/aem-importer/resources/package-tpl");

dt.initialize(srcPath, p);

dt.execute("mcloud.ditamap","/content/pando");


%>