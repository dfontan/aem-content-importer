/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/


package com.adobe.aem.importer.xml.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.xml.Config;
import com.day.cq.commons.jcr.JcrUtil;

public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * putConfigFileToJCR
	 * @param request
	 * @param config
	 * @param encoding
	 */
	public static String putConfigFileToJCR(SlingHttpServletRequest request, Config config, String encoding)  {
		
		String nameConfigFile = "";
		try {
			Node srcNode = JcrUtil.createPath(XMLTransformerHelper.DEFAULT_CONFIG_PARAM_SRC, "nt:folder", request.getResourceResolver().adaptTo(Session.class));
			
			StringWriter w = new StringWriter();
			Properties p = new Properties();
			p.put(XMLTransformerHelper.CONFIG_PARAM_TRANSFORMER, config.getTransformer());
			p.put(XMLTransformerHelper.CONFIG_PARAM_SRC, config.getSrc());
			p.put(XMLTransformerHelper.CONFIG_PARAM_TARGET, config.getTarget());
			p.put(XMLTransformerHelper.CONFIG_PARAM_MASTER_FILE, config.getMasterFile());
			Utils.appendCustomProperties(p, config.getCustomProps());
			
			
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			p.storeToXML(baout,null,encoding);
			w.append(baout.toString(encoding));
			
			ByteArrayInputStream bis = new ByteArrayInputStream(w.toString().getBytes("UTF-8"));
			
			nameConfigFile = System.currentTimeMillis()+".xml";
			JcrUtils.putFile(srcNode, nameConfigFile, "text/xml", bis);
			
			srcNode.getSession().save();
			
			return srcNode.getPath() + "/" +nameConfigFile;
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		
		return nameConfigFile;
	}

	
	/**
	 * appendCustomProperties
	 * @param configProp
	 * @param customProperties
	 */
	public static void appendCustomProperties(Properties configProp, String customProperties) {
		Properties custom = new Properties();
		try {
			custom.load(new StringReader(customProperties));
		} catch(Exception e) {
			log.error("Error on loading custom properties: "+customProperties);
		}
		for(Entry<Object, Object> entry : custom.entrySet())
			configProp.put(entry.getKey(), entry.getValue());
		
	}
}
