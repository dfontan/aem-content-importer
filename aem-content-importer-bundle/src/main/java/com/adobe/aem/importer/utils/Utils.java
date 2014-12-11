package com.adobe.aem.importer.utils;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

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
			Resource resources = request.getResourceResolver().getResource(Config.DEFAULT_FOLDER_SRC);
			Node srcNode = resources.adaptTo(Node.class);
			
			Session session = srcNode.getSession();
			
			Thread.currentThread().setContextClassLoader(
					Marshaller.class.getClassLoader());
			JAXBContext jaxbCtx = null;
			Marshaller marshaller = null;
			try {
				jaxbCtx = JAXBContext.newInstance(Config.class);
				marshaller = jaxbCtx.createMarshaller();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			
			StringWriter w = new StringWriter();
			w.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			marshaller.marshal(config, w);
			
			log.debug("CONFIG CREATED: " + w.toString());
			
			ByteArrayInputStream bis = new ByteArrayInputStream(w.toString().getBytes("UTF-8"));
			
			JcrUtils.putFile(srcNode, Config.CONFIG_PARAMS_NAME, "text/xml", bis);
			
			session.save();
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		
		
		

	}

}
