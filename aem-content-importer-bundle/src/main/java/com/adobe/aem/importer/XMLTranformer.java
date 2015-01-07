/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer;

import java.util.Properties;

import javax.jcr.Node;

/**
 * XML Transformer Interface
 */
public interface XMLTranformer {

	/**
	 * Initialize the XML transformer 
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
