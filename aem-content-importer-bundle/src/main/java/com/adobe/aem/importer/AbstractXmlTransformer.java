/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer;

import java.io.IOException;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.fs.api.ImportMode;
import org.apache.jackrabbit.vault.fs.config.ConfigurationException;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.fs.io.Importer;
import org.apache.jackrabbit.vault.fs.io.JcrArchive;
import org.xml.sax.XMLReader;

public abstract class AbstractXmlTransformer {

	protected static final String 		CONFIG_PARAM_TRANSFORMER_CLASS 		= "xslt-transformer";
	protected static final String 		CONFIG_PARAM_XSLT_FILE 						= "xslt-file";
	protected static final String 		CONFIG_PARAM_TEMP_FOLDER					= "tempFolder";
	protected static final String 		CONFIG_PARAM_PACKAGE_TPL 					= "packageTpl";
	protected static final String 		CONFIG_PARAM_GRAPHIC_FOLDERS			= "graphicFolders";
	
	protected static final String 	DEFAULT_TEMP_FOLDER								= "/var/aem-importer/tmp";
	protected static final String[] DEFAULT_GRAPHIC_FOLDERS						= {"images", "graphics", "Graphics"};
	protected static final String 	PACKAGE_FOLDER 										= "package";
	protected static final String 	PACKAGE_VAULT 										= "META-INF/vault/";
	protected static final String 	FILTER_XML_FILE 									= "filter.xml";
	protected static final String 	CONTENT_XML_MIME									= "application/xml";
	
	
	/**
	 * Get Mandatory property
	 * @param properties
	 * @param key
	 * @return
	 * @throws Exception
	 */
	protected String getMandatoryProperty(Properties properties, String key) throws Exception {
		final String xslt = properties.getProperty(key);
		if (xslt==null)
			throw new Exception("Mandatory property "+key+" not supplied");
		return xslt;
	}
	
	/**
	 * Initialize XSLT Transformer
	 * @param className
	 * @param xsltNode
	 * @param srcPathNode
	 * @param xmlReader
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws TransformerConfigurationException
	 * @throws RepositoryException
	 */
	protected Transformer initTransformer(String className, Node xsltNode, Node srcPathNode, XMLReader xmlReader, URIResolver uriResolver) throws InstantiationException, IllegalAccessException, ClassNotFoundException, TransformerConfigurationException, RepositoryException {
		Object transfInsance = Class.forName(className).newInstance();
		if (transfInsance instanceof TransformerFactoryImpl) {
			TransformerFactoryImpl transformFactory = (TransformerFactoryImpl)transfInsance;
			transformFactory.setURIResolver(uriResolver);
			return transformFactory.newTransformer(new StreamSource(JcrUtils.readFile(xsltNode)));
		} else
			throw new ClassNotFoundException("Class "+className+" is not an instance of "+TransformerFactoryImpl.class.getName());
	}
	
	
	/**
	 * 
	 * Create Archive & import contents
	 * @param packageNode
	 * @throws IOException
	 * @throws PathNotFoundException
	 * @throws RepositoryException
	 * @throws ConfigurationException
	 */
	protected void importArchive(Node packageNode) throws IOException, PathNotFoundException, RepositoryException, ConfigurationException {
		// Create Archive
    JcrArchive archive = new JcrArchive(packageNode, "/");
    archive.open(true);
    
    // Run importer
    Importer importer = new Importer();
    importer.getOptions().setImportMode(ImportMode.MERGE);
    importer.getOptions().setAccessControlHandling(AccessControlHandling.MERGE);
    
    importer.run(archive, packageNode.getSession().getNode("/"));
    
    // Save all
    packageNode.getSession().save();
	}
}
