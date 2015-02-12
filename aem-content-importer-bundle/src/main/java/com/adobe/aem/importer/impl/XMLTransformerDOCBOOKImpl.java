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
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.adobe.aem.importer.XMLTransformer;
import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.AbstractXmlTransformer;
import com.adobe.aem.importer.exception.AemImporterException;
import com.adobe.aem.importer.exception.AemImporterException.AEM_IMPORTER_EXCEPTION_TYPE;
import com.adobe.aem.importer.xml.FilterXmlBuilder;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.jcr.vault.util.RejectingEntityResolver;

@Component
@Service(value=XMLTransformer.class)
@org.apache.felix.scr.annotations.Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Adobe - DOCBOOK XSLT Transformer Service"),
	@Property(name = Constants.SERVICE_VENDOR, value = "Adobe") })
public class XMLTransformerDocBookImpl extends AbstractXmlTransformer implements XMLTransformer  {

	private static final Logger log = LoggerFactory.getLogger(XMLTransformerDocBookImpl.class);

	/* (non-Javadoc)
	 * @see com.adobe.aem.importer.XMLTransformer#transform(javax.jcr.Node, java.util.Properties)
	 */
	@Override
	public void transform(Node srcPath, Properties properties) throws AemImporterException {
		log.info("XMLTransformerDITA transformer starts to check out input parameters");
		// Source Path check
		if (properties==null)
			throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS, "Properties file cannot be NULL");

		// Properties check
		final String xslt = getMandatoryProperty(properties, CONFIG_PARAM_XSLT_FILE);
		final String transformerClass = getMandatoryProperty(properties, CONFIG_PARAM_TRANSFORMER_CLASS);
		final String packageTpl = getMandatoryProperty(properties, CONFIG_PARAM_PACKAGE_TPL);
		final String masterFile = getMandatoryProperty(properties, XMLTransformerHelper.CONFIG_PARAM_MASTER_FILE);

		try {
			if (!srcPath.hasNode(masterFile))
				throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS, "Master File "+masterFile+" not available in the folder "+srcPath.getPath());
			final String destPath = getMandatoryProperty(properties, XMLTransformerHelper.CONFIG_PARAM_TARGET);

			// Optional properties
			final String tmpFolder = properties.getProperty(CONFIG_PARAM_TEMP_FOLDER, DEFAULT_TEMP_FOLDER);
			log.debug("Get optional property {}: {}",CONFIG_PARAM_TEMP_FOLDER, tmpFolder);
			String graphicFolderList = properties.getProperty(CONFIG_PARAM_GRAPHIC_FOLDERS);
			log.debug("Get optional property {}: {}",CONFIG_PARAM_GRAPHIC_FOLDERS, graphicFolderList);
			final String[] graphicFolders = (graphicFolderList!=null) ? graphicFolderList.split(",") : DEFAULT_GRAPHIC_FOLDERS;

			// XSLT File Check
			Session session = srcPath.getSession();
			if (!session.itemExists(xslt))
				throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS, "XSLT Node File not available ("+xslt+")");
			final Node xsltNode = session.getNode(xslt);
			// Package Template Check
			if (!session.itemExists(packageTpl))
				throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS,"Package Template Node File not available ("+packageTpl+")");
			Node packageTplNode = session.getNode(packageTpl);

			// Create XML Reader
			log.debug("Create XML Reader");
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setEntityResolver(new RejectingEntityResolver());

			/* XSLT Transformer init */

			URIResolver uriResolver = new XMLTransformerDOCBOOKResolver(xsltNode, srcPath, xmlReader);
			Transformer xsltTransformer = initTransformer(transformerClass, xsltNode, srcPath, xmlReader, uriResolver);

			/* Pass all properties to XSLT transformer */
			for(Entry<Object, Object> entry : properties.entrySet()) {
				log.debug("Pass to transformer the property {}: {}",entry.getKey().toString(), entry.getValue());
				xsltTransformer.setParameter(entry.getKey().toString(), entry.getValue());
			}


			// Tmp Destination Folder
			Node tmpFolderNode = JcrUtil.createPath(tmpFolder, "nt:folder", "nt:folder", srcPath.getSession(), true);
			tmpFolderNode = JcrUtil.createUniqueNode(tmpFolderNode, srcPath.getName(), "nt:folder", srcPath.getSession());
			srcPath.getSession().save();
			log.debug("Create tmp destination folder {}",tmpFolderNode.getPath());

			// Transform
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			xsltTransformer.transform(new SAXSource(xmlReader, new InputSource(JcrUtils.readFile(srcPath.getNode(masterFile)))), new StreamResult(output));
			log.debug("Start transformation process reading master file {}",masterFile);

			// Copy transformed output stream to input
			InputStream stream = new ByteArrayInputStream(output.toByteArray());

			// Prepare package folders and copy transformed content stream
			final Node packageFolderNode = JcrUtil.copy(packageTplNode, tmpFolderNode, PACKAGE_FOLDER);
			Node contentFolder = JcrUtil.createPath(packageFolderNode.getPath()+"/jcr_root"+destPath, "nt:folder", "nt:folder", srcPath.getSession(), true);
			log.debug("Create package folder on {} using the template {}",contentFolder.getPath(), packageFolderNode.getPath());
			JcrUtils.putFile(contentFolder, ".content.xml", CONTENT_XML_MIME, stream);

			// Copy graphic resources
			for(String candidate : graphicFolders)
				if(srcPath.hasNode(candidate)) {
					log.debug("Add graphic folder {}",srcPath.getPath()+"/"+candidate);
					JcrUtil.copy(srcPath.getNode(candidate), contentFolder, candidate);
					JcrUtils.putFile(packageFolderNode.getNode(PACKAGE_VAULT), FILTER_XML_FILE, CONTENT_XML_MIME, FilterXmlBuilder.fromRoot(destPath+"/").toStream(candidate));
				}

			importArchive(packageFolderNode);

			// Delete tmp folder
			tmpFolderNode.remove();
			tmpFolderNode.getSession().save();
			log.info("XMLTransformerDITA transformation is completed");
		} catch (Exception e) {

			if (e instanceof AemImporterException) {
				throw (AemImporterException) e;
			} else {
				throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.UNEXPECTED, e.getMessage(), e);
			}


		}
	}

	/*********************************************
	 *                                           *
	 *        XMLTransformerDITAResolver        *
	 *                                           *
	 *********************************************/
	private class XMLTransformerDOCBOOKResolver implements URIResolver {
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
		public XMLTransformerDOCBOOKResolver(Node xsltNode, Node srcNode, XMLReader xmlReader) {
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
	      XMLTransformerDocBookImpl.log.debug("Resolving resource {}",node.getPath());
	      return new SAXSource(this.xmlReader, new InputSource(JcrUtils.readFile(node)));
		  } catch (RepositoryException e) {
		      throw new TransformerException("Cannot resolve " + href + " in either [parent of " + this.xsltNode + " or " + this.srcNode + "]");
		  }
		}


	}

}
