/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer / XSLTTranformer.java 
 * Dec 4, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer;

import java.util.Properties;

import javax.jcr.Node;

/**
 * DITA Transformer Interface
 */
public interface DITATranformer {

	/**
	 * Initialize the DITA transformer 
	 * @param resourceResolver resolver for CRX access
	 * @param srcPath crx source node path
	 * @param properties custom properties configuration
	 * @throws Exception
	 */
	public void initialize(Node srcPath,Properties properties) throws Exception;
	
	
	/**
	 * Execute tranformation using master file (if available) and import content into destination path
	 * @param masterFile master file to use as map reference
	 * @param destPath crx destination path 
	 * @throws Exception
	 */
	public void execute(String masterFile, String destPath) throws Exception;
	
}
