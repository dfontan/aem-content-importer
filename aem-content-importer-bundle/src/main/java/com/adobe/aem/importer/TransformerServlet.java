/* CodeBay Innovation SL 2014*/
package com.adobe.aem.importer;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(
	    paths={"/bin/aem-importer/servlet/transformerlist"},
	    methods={"GET"}
	)
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "AEM Importer - Servlet for obtain transformers"),
		@Property(name = Constants.SERVICE_VENDOR, value = "CodeBay-Innovation") 
	})

public class TransformerServlet extends SlingAllMethodsServlet{

	private static final long serialVersionUID = 3827007172729941175L;

	private static Logger log = LoggerFactory.getLogger(TransformerServlet.class);
	
	
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		doLogic(request, response);
	}

	@Override
	protected void doGet(SlingHttpServletRequest request,SlingHttpServletResponse response) throws ServletException, IOException {
		doLogic(request, response);
	}
	
	
	/**
	 * doLogic
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void doLogic(SlingHttpServletRequest request, SlingHttpServletResponse response) {
		try {
			Class<?>[] availableTransformers = DITATransformerHelper.getAvailableTransformers();
			JSONObject jsonObject = new JSONObject();
			if (availableTransformers.length > 0) {
				
				for (Class<?> cl : availableTransformers) {
				 	jsonObject.put(cl.getSimpleName().replace("Impl", ""), cl.getSimpleName());
				}
				
			}
		 	
			String charEncoding = "UTF-8"; 
			response.setHeader("Accept-Charset", charEncoding); 
			response.setContentType("application/json; charset=" + charEncoding + ";");
				response.getWriter().println(jsonObject.toString());
		} catch (Exception e) {
			log.error(e.toString());
		}
		
		
		
	}
}
