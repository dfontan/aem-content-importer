/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/
package com.adobe.aem.importer.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.xml.Config;
import com.adobe.aem.importer.xml.utils.Utils;
import com.adobe.aem.importer.xml.utils.ZipParser;

@SlingServlet(resourceTypes = "aem-importer/components/upload-content", methods = { "POST" })
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "AEM Content Importer - Servlet for upload content"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Adobe") })
public class UploadContentServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -6981132289425368170L;
	private static Logger log = LoggerFactory
			.getLogger(UploadContentServlet.class);

	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		try {
			doLogic(request, response);
		} catch (JSONException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * doLogic
	 * 
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws Exception
	 */
	private void doLogic(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException, JSONException {
		final boolean isMultipart = ServletFileUpload
				.isMultipartContent(request);
		String src = "";
		String target = "";
		String transformer = "";
		String masterFile = "";
		String customProps = "";

		String configPathResult = "";

		ZipParser zipParser = null;

		boolean uploadZip = false;
		
		JSONObject result = new JSONObject();
		
		try {
			if (isMultipart) {

				Map<String, RequestParameter[]> params = request
						.getRequestParameterMap();
				for (final Map.Entry<String, RequestParameter[]> pairs : params
						.entrySet()) {
					String k = pairs.getKey();
					RequestParameter[] pArr = pairs.getValue();
					RequestParameter param = pArr[0];
					InputStream stream = param.getInputStream();
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

						if ("customProps".equalsIgnoreCase(k) && !uploadZip) {
							customProps = Streams.asString(stream);
						}

						if ("customCommandProps".equalsIgnoreCase(k)
								&& !uploadZip) {
							String customCommandProps = Streams
									.asString(stream);
							customProps = customCommandProps.replaceAll("#",
									"\r\n");
						}

					} else {
						zipParser = new ZipParser(param.getInputStream(),
								request);

						configPathResult = zipParser.unzipAndUploadJCR("UTF-8");
						uploadZip = true;
					}
				}

				if (!uploadZip) {
					Config configFileXml = new Config();

					configFileXml.setSrc(src);
					configFileXml.setTransformer(transformer);
					configFileXml.setTarget(target);
					configFileXml.setMasterFile(masterFile);

					configFileXml.setCustomProps(customProps);

					configPathResult = Utils.putConfigFileToJCR(request,
							configFileXml, "UTF-8");
				}
				
				result.put("error", "false");
				result.put("configPathResult", configPathResult);

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.put("error", "true");
		}
		
		response.getOutputStream().write(result.toString().getBytes());
	}
}
