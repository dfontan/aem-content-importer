/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer / XSLTTranformer.java 
 * Dec 4, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer;

import java.util.Properties;

/**
 * DITA Transformer Interface
 */
public interface DITATranformer {

	/**
	 * Initialize the transformer setting configuration parameters
	 * @param masterFile
	 * @param srcPath
	 * @param destPath
	 * @param properties
	 * @throws Exception
	 */
	public void initialize(String masterFile, String srcPath, String destPath, Properties properties) throws Exception;
	
	
	/**
	 * Execute tranformation and import resulting content
	 * @throws Exception
	 */
	public void execute() throws Exception;
	
}
