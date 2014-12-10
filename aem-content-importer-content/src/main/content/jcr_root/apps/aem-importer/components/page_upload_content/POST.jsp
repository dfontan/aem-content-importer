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
 
try {
	PrintWriter pout = response.getWriter();
if (isMultipart) {
	final Map<String, RequestParameter[]> params = slingRequest.getRequestParameterMap();
    for (final Map.Entry<String, RequestParameter[]> pairs : params.entrySet()) {
      final String k = pairs.getKey();
      final RequestParameter[] pArr = pairs.getValue();
      final RequestParameter param = pArr[0];
      final InputStream stream = param.getInputStream();
      if (param.isFormField()) {
    	  
    	  if ("src".equalsIgnoreCase(k)) {
    		  src = Streams.asString(stream);
    	  }
    	  
    	  if ("target".equalsIgnoreCase(k)) {
    		  target = Streams.asString(stream);
    	  }
    	  
    	  if ("transformer".equalsIgnoreCase(k)) {
    		  transformer = Streams.asString(stream);
    	  }
    	  
    	  if ("master".equalsIgnoreCase(k)) {
    		  masterFile = Streams.asString(stream);
    	  }
      } else {
//         out.println("File field " + k + " with file name " + param.getFileName() + " detected.");
      }
	}
    
    //TODO: Invoke the corresponding transformer
    DITATranformer dt = DITATransformerHelper.getDITATransformer(transformer);
	
    Resource resources = slingRequest.getResourceResolver().getResource(src);

    Node srcPath = resources.adaptTo(Node.class);

    Properties p = new Properties();

    //TODO: Prepare the right manner to assign the template and the other params
    p.put("xslt", "/apps/aem-importer/resources/dita-to-content.xsl");
    p.put("transformer","net.sf.saxon.TransformerFactoryImpl");
    p.put("packageTpl","/apps/aem-importer/resources/package-tpl");

    dt.initialize(srcPath, p);

    dt.execute(masterFile,target);
    
    %>
    
    {"error": "false"}
    
    <%
    
    
    
}
} catch(Exception e) {
	%>
	
	{"error": "true", "message": "<%=e.getMessage() %>"}
	
	<%
}

 %>



<%

//    response.setStatus(response.SC_MOVED_TEMPORARILY);
//    response.setHeader("Location", currentPage.getPath() + ".html"); 
%>
