/**
 * CodeBay Innovation SL 2015
 * aem-content-importer-bundle-2
 * com.adobe.aem.importer2 / AnotherTransformerXML.java 
 * Jan 8, 2015
 * @author Gaetano
 */
package com.adobe.aem.importer2.impl;

import java.util.Properties;

import javax.jcr.Node;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;

import com.adobe.aem.importer.XMLTransformer;

@Component
@Service(value=XMLTransformer.class)
@org.apache.felix.scr.annotations.Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Adobe - No Action Transformer"),
	@Property(name = Constants.SERVICE_VENDOR, value = "Adobe") })
public class NoActionTransformer implements XMLTransformer {

	/* (non-Javadoc)
	 * @see com.adobe.aem.importer.XMLTransformer#transform(javax.jcr.Node, java.util.Properties)
	 */
	@Override
	public void transform(Node srcPath, Properties properties) throws Exception {
		// Do Nothing
	}

	
}
