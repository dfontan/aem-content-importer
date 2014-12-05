/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer / DITATransformerHelper.java 
 * Dec 5, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DITATransformerHelper {

	private static final Logger log = LoggerFactory.getLogger(DITATransformerHelper.class);
	private static final String CLASS_EXTENSION = ".class";
	private static Class<?>[] availableTransformers = null;
	
	
	/**
	 * Get array of available transformers
	 * @return list of available transformers
	 */
	public static Class<?>[] getAvailableTransformers() {
		if (DITATransformerHelper.availableTransformers==null) {
			ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
			String packageName = (DITATransformerHelper.class.getPackage().getName()+".impl");
			log.debug("Process the seeking of tranformer classes under []",packageName);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			// FileFilter declaration for retrieving only classes
			FileFilter classFileFilter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(CLASS_EXTENSION);
				}
			};
			
			// Get package Resource and fetch own sub-files
			File implDir = new File(classLoader.getResource(packageName.replace(".", "/")).getFile());
			if (implDir.exists() && implDir.isDirectory())
				for(File classFile : implDir.listFiles(classFileFilter)) {
					Class<?> classObj = checkCompliantClassFile(packageName, classFile.getName());
					if (classObj!=null)
						classes.add(classObj);
				}
			
			// Save results on static variable
			DITATransformerHelper.availableTransformers = classes.toArray(new Class<?>[classes.size()]);
		}
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
	 * @param packageName
	 * @param fileName
	 * @return the resulting class or null in case of error
	 */
	private static Class<?> checkCompliantClassFile(String packageName, String fileName) {
		String className = packageName+"."+fileName.substring(0, fileName.length()-CLASS_EXTENSION.length());
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
