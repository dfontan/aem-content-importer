/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer / DITATransformerHelper.java 
 * Dec 5, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DITATransformerHelper {
	
	public static final String CONFIG_PARAM_SRC 				= "src";
	public static final String CONFIG_PARAM_TARGET 			= "target";
	public static final String CONFIG_PARAM_TRANSFORMER = "dita-transformer";
	public static final String CONFIG_PARAM_MASTER_FILE = "masterFile";
	public static final String DEFAULT_CONFIG_PARAM_SRC = "/var/aem-importer/import";
	

	private static final Logger log = LoggerFactory.getLogger(DITATransformerHelper.class);
	private static final String CLASS_EXTENSION = ".class";
	private static Class<?>[] availableTransformers = {};
	
	
	/**
	 * Read and store available transformer class
	 * @param bundle
	 */
	public static void init(Bundle bundle) {
		try {
			ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
			String packageName = (DITATransformerHelper.class.getPackage().getName()+".impl");
			log.debug("Process the seeking of tranformer classes under []",packageName);
			for(Enumeration<URL> e = bundle.findEntries(packageName.replace(".", "/"), "*.class", false); e.hasMoreElements();) {
				Class<?> classObj = checkCompliantClassFile(e.nextElement().getPath());
				if (classObj!=null)
					classes.add(classObj);
			}
			DITATransformerHelper.availableTransformers = classes.toArray(new Class<?>[classes.size()]);
		} catch(Exception e) {
			log.error("An error has occurred retrieving available transformers",e);
		}
	}
	
	/**
	 * Get array of available transformers
	 * @return list of available transformers
	 */
	public static Class<?>[] getAvailableTransformers() {
		return DITATransformerHelper.availableTransformers;
	}
	
	/**
	 * Get DITATransformer from the className
	 * @param className (qualified name)
	 * @return the instatiated class or null in case of error
	 */
	public static DITATranformer getDITATransformer(String className) {
		try {
			Class<?> implClass = Class.forName(className);
			if (!DITATranformer.class.isAssignableFrom(implClass)) {
				log.warn("Requested "+className+" class doesn't implement "+DITATranformer.class.getName()+" interface");
				return null;
			}
			return (DITATranformer)implClass.newInstance();
		} catch(ClassNotFoundException e) {
			log.error("Class not found exception for "+className,e);
		} catch (InstantiationException e) {
			log.error("Error on instantiating class "+className,e);
		} catch (IllegalAccessException e) {
			log.error("Illegal access to class "+className,e);
		}
		return null;
	}
	
	/**
	 * 
	 * Check if the class is valid and compliant with DITATransformer interface
	 * @param pathName
	 * @return the resulting class or null in case of error
	 */
	private static Class<?> checkCompliantClassFile(String pathName) {
		String className = pathName.substring(1, pathName.length()-CLASS_EXTENSION.length()).replace("/", ".");
		try {
			Class<?> implClass = Class.forName(className);
			if (!DITATranformer.class.isAssignableFrom(implClass)) {
				log.warn(className+" class doesn't implement "+DITATranformer.class.getName()+" interface");
				return null;
			}
			return implClass;
		} catch(ClassNotFoundException clE) {
			log.error("Class not found exception for "+className,clE);
		}
		return null;
	}
	
}
