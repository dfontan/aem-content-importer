package com.adobe.aem.importer.xml.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.DITATransformerHelper;
import com.adobe.aem.importer.xml.Config;

public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * putConfigFileToJCR
	 * @param request
	 * @param config
	 * @param encoding
	 */
	public static void putConfigFileToJCR(SlingHttpServletRequest request, Config config, String encoding)  {
		
		try {
			Resource resources = request.getResourceResolver().getResource(DITATransformerHelper.DEFAULT_CONFIG_PARAM_SRC);
			Node srcNode = resources.adaptTo(Node.class);
			
			Session session = srcNode.getSession();
			
			StringWriter w = new StringWriter();
			Properties p = new Properties();
			p.put(DITATransformerHelper.CONFIG_PARAM_TRANSFORMER, config.getTransformer());
			p.put(DITATransformerHelper.CONFIG_PARAM_SRC, config.getSrc());
			p.put(DITATransformerHelper.CONFIG_PARAM_TARGET, config.getTarget());
			p.put(DITATransformerHelper.CONFIG_PARAM_MASTER_FILE, config.getMasterFile());
			
			
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			p.storeToXML(baout,null,encoding);
			w.append(baout.toString(encoding));
			
			ByteArrayInputStream bis = new ByteArrayInputStream(w.toString().getBytes("UTF-8"));
			
			JcrUtils.putFile(srcNode, DITATransformerHelper.CONFIG_FILENAME, "text/xml", bis);
			
			session.save();
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		
		
		

	}

}
