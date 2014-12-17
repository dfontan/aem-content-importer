<%@page import="javax.jcr.Session"%>
<%@page import="com.day.cq.commons.jcr.JcrUtil"%>
<%@page import="org.apache.jackrabbit.commons.JcrUtils"%>
<%@page import="com.adobe.aem.importer.xml.Config"%>
<%@page import="com.adobe.aem.importer.xml.utils.Utils"%>
<%@page import="com.adobe.aem.importer.xml.utils.ZipParser"%>
<%@page import="java.util.Properties"%>
<%@page import="javax.jcr.Node"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="com.adobe.aem.importer.DITATransformerHelper"%>
<%@page import="com.adobe.aem.importer.DITATranformer"%>
<%@page import="org.apache.sling.api.request.RequestParameter"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.fileupload.util.Streams"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.InputStream"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/libs/foundation/global.jsp"%>

<% 
//Check that we have a file upload request
final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 String error = "";
 
 String src = "";
 String target = "";
 String transformer = "";
 String masterFile = "";
 
 ZipParser zipParser = null;
 
 boolean uploadZip = false;
 
try {
	PrintWriter pout = response.getWriter();
if (isMultipart) {
	final Map<String, RequestParameter[]> params = slingRequest.getRequestParameterMap();
	final InputStream zip = null; 
    for (final Map.Entry<String, RequestParameter[]> pairs : params.entrySet()) {
      final String k = pairs.getKey();
      final RequestParameter[] pArr = pairs.getValue();
      final RequestParameter param = pArr[0];
      final InputStream stream = param.getInputStream();
      if (param.isFormField()) {
    	  
    	  if ("src".equalsIgnoreCase(k) && !uploadZip) {
    		  src = Streams.asString(stream);
    	  }
    	  
    	  if ("target".equalsIgnoreCase(k) && !uploadZip) {
    		  target = Streams.asString(stream);
    	  }
    	  
    	  if ("transformer".equalsIgnoreCase(k) && !uploadZip) {
    		  transformer = Streams.asString(stream);
    	  }
    	  
    	  if ("masterFile".equalsIgnoreCase(k) && !uploadZip) {
    		  masterFile = Streams.asString(stream);
    	  }
      } else {
//         out.println("File field " + k + " with file name " + param.getFileName() + " detected.");
		    zipParser = new ZipParser(param.getInputStream(),slingRequest);
			
		    zipParser.unzipAndUploadJCR("UTF-8");
		    
		    src = zipParser.getSrc();
			target = zipParser.getTarget();
			transformer = zipParser.getTransformer();
			masterFile = zipParser.getMasterFile();
						
			uploadZip = true;
      }
	}
    
    
		
    if (!uploadZip) {
	    Config configFileXml = new Config();
	    
	    configFileXml.setSrc(src);
	    configFileXml.setTransformer(transformer);
	    configFileXml.setTarget(target);
	    configFileXml.setMasterFile(masterFile);
    	Utils.putConfigFileToJCR(slingRequest, configFileXml, "UTF-8");
    }
	
    
    %>

{"error": "false"}

<%
    
    
    
}
} catch(Exception e) {
	
	if (zipParser != null && zipParser.getSource() != null) {
		zipParser.getSource().close();
	}
	
	%>

{"error": "true"}

<%
}

 %>
