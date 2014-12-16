package com.adobe.aem.importer.xml.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.constant.Constant;
import com.adobe.aem.importer.xml.Config;

public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * putConfigFileToJCR
	 * @param request
	 * @param config
	 */
	public static void putConfigFileToJCR(SlingHttpServletRequest request,
			Config config)  {
		
		try {
			Resource resources = request.getResourceResolver().getResource(Constant.DEFAULT_FOLDER_SRC);
			Node srcNode = resources.adaptTo(Node.class);
			
			Session session = srcNode.getSession();
			
			StringWriter w = new StringWriter();
			Properties p = new Properties();
			p.put(Constant.TRANSFORMER, config.getTransformer());
			p.put(Constant.SRC, config.getSrc());
			p.put(Constant.TARGET, config.getTarget());
			p.put(Constant.MASTER_FILE, config.getMasterFile());
			
			
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			p.storeToXML(baout,null,Constant.ENCODING);
			w.append(baout.toString(Constant.ENCODING));
			
			ByteArrayInputStream bis = new ByteArrayInputStream(w.toString().getBytes("UTF-8"));
			
			JcrUtils.putFile(srcNode, Constant.CONFIG_PARAMS_NAME, "text/xml", bis);
			
			session.save();
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		
		
		

	}

}
