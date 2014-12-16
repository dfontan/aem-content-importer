/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer.impl / DITATransformerXSLTImpl.java 
 * Dec 5, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.fs.io.Importer;
import org.apache.jackrabbit.vault.fs.io.JcrArchive;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.adobe.aem.importer.DITATranformer;
import com.adobe.aem.importer.xml.FilterXmlBuilder;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.jcr.vault.util.RejectingEntityResolver;

public class DITATransformerXSLTImpl implements DITATranformer, URIResolver {

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
	
	/* crx source path */
	private Node srcPath = null;
	
	/* xslt file node */
	private Node xsltNode = null;
	/* XSLT Transformer */
	private Transformer xsltTransformer = null;
	/* package template node */
	private Node packageTplNode = null;
	/* tmp folder */
	private String tmpFolder = null;
	/* graphic folders */
	private String[] graphicFolders = null;
	
	/* XML Reader */
	private XMLReader xmlReader = null;
	/* init flag */
	private boolean init = false;
	
	

	/* (non-Javadoc)
	 * @see com.adobe.aem.importer.DITATranformer#initialize(javax.jcr.Node, java.util.Properties)
	 */
	@Override
	public void initialize(Node srcPath, Properties properties) throws Exception {
		// Source Path check
		this.srcPath = srcPath;
		if (properties==null)
			throw new Exception("Properties file cannot be NULL");
		
		// Properties check
		final String xslt = properties.getProperty(CONFIG_PARAM_XSLT_FILE);
		if (xslt==null)
			throw new Exception("Mandatory property "+CONFIG_PARAM_XSLT_FILE+" not supplied");
		final String transformerClass = properties.getProperty(CONFIG_PARAM_TRANSFORMER_CLASS);
		if (transformerClass==null)
			throw new Exception("Mandatory property "+CONFIG_PARAM_TRANSFORMER_CLASS+" not supplied");
		final String packageTpl = properties.getProperty(CONFIG_PARAM_PACKAGE_TPL);
		if (packageTpl==null)
			throw new Exception("Mandatory property "+CONFIG_PARAM_PACKAGE_TPL+" not supplied");
		
		// Optional properties
		this.tmpFolder = properties.getProperty(CONFIG_PARAM_TEMP_FOLDER, DEFAULT_TEMP_FOLDER);
		String graphicFolderList = properties.getProperty(CONFIG_PARAM_GRAPHIC_FOLDERS);
		if (graphicFolderList==null)
			this.graphicFolders = DEFAULT_GRAPHIC_FOLDERS;
		else
			this.graphicFolders = graphicFolderList.split(",");
		
		// XSLT File Check
		Session session = srcPath.getSession();
		if (!session.itemExists(xslt))
			throw new Exception("XSLT Node File not available ("+xslt+")");
		this.xsltNode = session.getNode(xslt);
		// Package Template Check
		if (!session.itemExists(packageTpl))
			throw new Exception("Package Template Node File not available ("+packageTpl+")");
		this.packageTplNode = session.getNode(packageTpl);
		
		// Create XML Reader
		this.xmlReader = XMLReaderFactory.createXMLReader();
		this.xmlReader.setEntityResolver(new RejectingEntityResolver());
		
		/* XSLT Transform init */
		initTranformer(transformerClass);
		
		// Init done
		this.init = true;
	}

	/* (non-Javadoc)
	 * @see com.adobe.aem.importer.DITATranformer#execute(java.lang.String, java.lang.String)
	 */
	@Override
	public void execute(String masterFile, String destPath) throws Exception {
		// Check initialization if done
		if (!init)
			throw new Exception("Initialization not called before");
		// Check Master File
		if (masterFile==null)
			throw new Exception("Master File cannot be NULL");
		if (!this.srcPath.hasNode(masterFile))
			throw new Exception("Master File "+masterFile+" not available in the folder "+this.srcPath.getPath());
		
		// Check Dest Path
		if (destPath==null)
			throw new Exception("Destination path cannot be NULL");
		
		// Tmp Destination Folder
		Node tmpFolderNode = JcrUtil.createPath(this.tmpFolder+"/"+srcPath.getName(), "nt:folder", "nt:folder", srcPath.getSession(), true);
		
		// Transform
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		this.xsltTransformer.transform(new SAXSource(xmlReader, new InputSource(JcrUtils.readFile(this.srcPath.getNode(masterFile)))), new StreamResult(output));

		// Copy transformed output stream to input
    InputStream stream = new ByteArrayInputStream(output.toByteArray());

    // Prepare package folders and copy transformed content stream
    final Node packageFolderNode = JcrUtil.copy(packageTplNode, tmpFolderNode, PACKAGE_FOLDER);
    JcrUtils.putFile(packageFolderNode.getNode(PACKAGE_VAULT), FILTER_XML_FILE, CONTENT_XML_MIME, FilterXmlBuilder.fromRoot(destPath+"/").toStream(srcPath.getName()));
    final Node contentFolder = tmpFolderNode.getNode(PACKAGE_FOLDER+"/jcr_root"+destPath).addNode(srcPath.getName(),"nt:folder");
    JcrUtils.putFile(contentFolder, ".content.xml", CONTENT_XML_MIME, stream);

    // Copy graphic resources
    for(String candidate : this.graphicFolders)
      if(srcPath.hasNode(candidate)) 
          JcrUtil.copy(srcPath.getNode(candidate), contentFolder, candidate);
    
    // Create Archive
    JcrArchive archive = new JcrArchive(packageFolderNode, "/");
    archive.open(true);

    // Run importer
    Importer importer = new Importer();
    importer.run(archive, srcPath.getSession().getNode("/"));
	}
	
	
	/* (non-Javadoc)
	 * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
	 */
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		try {
      final Node node = (href.endsWith("xsl") ?  this.xsltNode.getParent().getNode(href) : this.srcPath.getNode(href)); 
      return new SAXSource(this.xmlReader, new InputSource(JcrUtils.readFile(node)));
	  } catch (RepositoryException e) {
	      throw new TransformerException("Cannot resolve " + href + " in either [parent of " + this.xsltNode + " or " + this.srcPath + "]");
	  }
	}

	/**
	 * Initialize XSLT Transformer
	 * @param className
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws RepositoryException 
	 * @throws TransformerConfigurationException 
	 */
	private void initTranformer(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException, TransformerConfigurationException, RepositoryException {
		Object transfInsance = Class.forName(className).newInstance();
		if (transfInsance instanceof TransformerFactoryImpl) {
			TransformerFactoryImpl transformFactory = (TransformerFactoryImpl)transfInsance;
			transformFactory.setURIResolver(this);
			this.xsltTransformer = transformFactory.newTransformer(new StreamSource(JcrUtils.readFile(this.xsltNode)));
		} else
			throw new ClassNotFoundException("Class "+className+" is not an instance of "+TransformerFactoryImpl.class.getName());
	}

}
