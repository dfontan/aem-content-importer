/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.fs.io.Importer;
import org.apache.jackrabbit.vault.fs.io.JcrArchive;
import org.osgi.framework.Constants;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.adobe.aem.importer.XMLTransformer;
import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.xml.FilterXmlBuilder;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.jcr.vault.util.RejectingEntityResolver;

@Component
@Service(value=XMLTransformer.class)
@org.apache.felix.scr.annotations.Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Adobe - DITA XSLT Transformer Service"),
	@Property(name = Constants.SERVICE_VENDOR, value = "Adobe") })
public class DITATransformerXSLTImpl implements XMLTransformer {

	public static final String 		CONFIG_PARAM_TRANSFORMER_CLASS 		= "xslt-transformer";
	public static final String 		CONFIG_PARAM_XSLT_FILE 						= "xslt-file";
	public static final String 		CONFIG_PARAM_TEMP_FOLDER					= "tempFolder";
	public static final String 		CONFIG_PARAM_PACKAGE_TPL 					= "packageTpl";
	public static final String 		CONFIG_PARAM_GRAPHIC_FOLDERS			= "graphicFolders";
	
	private static final String 	DEFAULT_TEMP_FOLDER								= "/var/aem-importer/tmp";
	private static final String[] DEFAULT_GRAPHIC_FOLDERS						= {"images", "graphics", "Graphics"};
	private static final String 	PACKAGE_FOLDER 										= "package";
	private static final String 	PACKAGE_VAULT 										= "META-INF/vault/";
	private static final String 	FILTER_XML_FILE 									= "filter.xml";
	private static final String 	CONTENT_XML_MIME									= "application/xml";
	
	
	/* (non-Javadoc)
	 * @see com.adobe.aem.importer.XMLTransformer#transform(javax.jcr.Node, java.util.Properties)
	 */
	@Override
	public void transform(Node srcPath, Properties properties) throws Exception {
		// Source Path check
		if (properties==null)
			throw new Exception("Properties file cannot be NULL");
		
		// Properties check
		final String xslt = getMandatoryProperty(properties, CONFIG_PARAM_XSLT_FILE);
		final String transformerClass = getMandatoryProperty(properties, CONFIG_PARAM_TRANSFORMER_CLASS);
		final String packageTpl = getMandatoryProperty(properties, CONFIG_PARAM_PACKAGE_TPL);
		final String masterFile = getMandatoryProperty(properties, XMLTransformerHelper.CONFIG_PARAM_MASTER_FILE);
		if (!srcPath.hasNode(masterFile))
			throw new Exception("Master File "+masterFile+" not available in the folder "+srcPath.getPath());
		final String destPath = getMandatoryProperty(properties, XMLTransformerHelper.CONFIG_PARAM_TARGET);
		
		// Optional properties
		final String tmpFolder = properties.getProperty(CONFIG_PARAM_TEMP_FOLDER, DEFAULT_TEMP_FOLDER);
		String graphicFolderList = properties.getProperty(CONFIG_PARAM_GRAPHIC_FOLDERS);
		final String[] graphicFolders = (graphicFolderList!=null) ? graphicFolderList.split(",") : DEFAULT_GRAPHIC_FOLDERS;
		
		// XSLT File Check
		Session session = srcPath.getSession();
		if (!session.itemExists(xslt))
			throw new Exception("XSLT Node File not available ("+xslt+")");
		final Node xsltNode = session.getNode(xslt);
		// Package Template Check
		if (!session.itemExists(packageTpl))
			throw new Exception("Package Template Node File not available ("+packageTpl+")");
		Node packageTplNode = session.getNode(packageTpl);
		
		// Create XML Reader
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setEntityResolver(new RejectingEntityResolver());
		
		/* XSLT Transformer init */
		Transformer xsltTransformer = initTranformer(transformerClass, xsltNode, srcPath, xmlReader);
		
		/* Pass all properties to XSLT transformer */
		for(Entry<Object, Object> entry : properties.entrySet())
			xsltTransformer.setParameter(entry.getKey().toString(), entry.getValue());
		
		
		// Tmp Destination Folder
		Node tmpFolderNode = JcrUtil.createPath(tmpFolder, "nt:folder", "nt:folder", srcPath.getSession(), true);
		tmpFolderNode = JcrUtil.createUniqueNode(tmpFolderNode, srcPath.getName(), "nt:folder", srcPath.getSession());
		srcPath.getSession().save();
		
		// Transform
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		xsltTransformer.transform(new SAXSource(xmlReader, new InputSource(JcrUtils.readFile(srcPath.getNode(masterFile)))), new StreamResult(output));

		// Copy transformed output stream to input
    InputStream stream = new ByteArrayInputStream(output.toByteArray());

    // Prepare package folders and copy transformed content stream
    final Node packageFolderNode = JcrUtil.copy(packageTplNode, tmpFolderNode, PACKAGE_FOLDER);
    JcrUtils.putFile(packageFolderNode.getNode(PACKAGE_VAULT), FILTER_XML_FILE, CONTENT_XML_MIME, FilterXmlBuilder.fromRoot(destPath+"/").toStream(srcPath.getName()));
    Node contentFolder = JcrUtil.createPath(packageFolderNode.getPath()+"/jcr_root"+destPath, "nt:folder", "nt:folder", srcPath.getSession(), true);
    JcrUtils.putFile(contentFolder, ".content.xml", CONTENT_XML_MIME, stream);

    // Copy graphic resources
    for(String candidate : graphicFolders)
      if(srcPath.hasNode(candidate)) 
          JcrUtil.copy(srcPath.getNode(candidate), contentFolder, candidate);
    
    // Create Archive
    JcrArchive archive = new JcrArchive(packageFolderNode, "/");
    archive.open(true);

    // Run importer
    Importer importer = new Importer();
    importer.run(archive, srcPath.getSession().getNode("/"));
    
    // Delete tmp folder
    tmpFolderNode.remove();
    
    // Save all
    srcPath.getSession().save();
			
	}

	/**
	 * Get Mandatory property
	 * @param properties
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private String getMandatoryProperty(Properties properties, String key) throws Exception {
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
	private Transformer initTranformer(String className, Node xsltNode, Node srcPathNode, XMLReader xmlReader) throws InstantiationException, IllegalAccessException, ClassNotFoundException, TransformerConfigurationException, RepositoryException {
		Object transfInsance = Class.forName(className).newInstance();
		if (transfInsance instanceof TransformerFactoryImpl) {
			TransformerFactoryImpl transformFactory = (TransformerFactoryImpl)transfInsance;
			transformFactory.setURIResolver(new DITATransformerXSLTResolver(xsltNode, srcPathNode, xmlReader));
			return transformFactory.newTransformer(new StreamSource(JcrUtils.readFile(xsltNode)));
		} else
			throw new ClassNotFoundException("Class "+className+" is not an instance of "+TransformerFactoryImpl.class.getName());
	}
	
	
	
	
	
	/*********************************************
	 *                                           *
	 *        DITATransformerXSLTResolver        *
	 *                                           *
	 *********************************************/
	private class DITATransformerXSLTResolver implements URIResolver {
		/* XSLT Node */
		private Node xsltNode;
		/* Source Node */
		private Node srcNode;
		/* XML Reader */
		private XMLReader xmlReader;
		
		/**
		 * Constructor
		 * @param xsltNode
		 * @param srcNode
		 * @param xmlReader
		 */
		public DITATransformerXSLTResolver(Node xsltNode, Node srcNode, XMLReader xmlReader) {
			this.xsltNode = xsltNode;
			this.srcNode = srcNode;
			this.xmlReader = xmlReader;
		}

		/* (non-Javadoc)
		 * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
		 */
		@Override
		public Source resolve(String href, String base) throws TransformerException {
			try {
	      final Node node = (href.endsWith("xsl") ?  this.xsltNode.getParent().getNode(href) : this.srcNode.getNode(href)); 
	      return new SAXSource(this.xmlReader, new InputSource(JcrUtils.readFile(node)));
		  } catch (RepositoryException e) {
		      throw new TransformerException("Cannot resolve " + href + " in either [parent of " + this.xsltNode + " or " + this.srcNode + "]");
		  }
		}
		
		
	}
}
