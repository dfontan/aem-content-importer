package com.adobe.aem.importer.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adobe.aem.importer.xml.Config;

public class ZipParser {

	private static final Logger log = LoggerFactory.getLogger(ZipParser.class);
	
	private static final int BUFFER_SIZE = 4096;
	public static final String SRC = "src";
	public static final String TARGET = "target";
	public static final String TRANSFORMER = "transformer";
	public static final String MASTER_FILE = "masterFile";

	private ZipInputStream source;
	private SlingHttpServletRequest request;

	private String src;
	private String transformer;
	private String target;
	private String masterFile;

	public ZipParser(InputStream source, SlingHttpServletRequest request) {
		this.source = new ZipInputStream(source);
		this.request = request;
	}

	public void unzip() {
		ZipEntry entry;
		try {
			entry = source.getNextEntry();
			ByteArrayInputStream configFile = null;
			// First file of zip must to be the config file
			if (entry != null) {
				configFile = extractConfigFile(source);
				entry = source.getNextEntry();

			}

			Resource resources = request.getResourceResolver().getResource(src);

			Node srcNode = resources.adaptTo(Node.class);
			
			Session session = srcNode.getSession();

			while (entry != null) {
				JcrUtils.putFile(srcNode, entry.getName(), "text/xml", extractFile(source));
				entry = source.getNextEntry();
			}
			
			resources = request.getResourceResolver().getResource(Config.DEFAULT_FOLDER_SRC);
			Node workflowNode = resources.adaptTo(Node.class);
			
			JcrUtils.putFile(workflowNode, Config.CONFIG_PARAMS_NAME, "text/xml", configFile);
			
			session.save();

			source.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * extractFile
	 * @param zipIn
	 * @return
	 * @throws IOException
	 */
	private ByteArrayInputStream extractFile(ZipInputStream zipIn)
			throws IOException {
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(
				baout);
		byte[] bytesIn = new byte[ZipParser.BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
		zipIn.closeEntry();
		
		return new ByteArrayInputStream(baout.toByteArray());
	}

	/**
	 * extractConfigFile
	 * @param configFile
	 * @return
	 * @throws IOException
	 */
	private ByteArrayInputStream extractConfigFile(ZipInputStream configFile)
			throws IOException {
		// get the factory
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		StringWriter writer = null;
		ByteArrayInputStream bai = null;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document config = db.parse(extractFile(configFile));

			NodeList nl = config.getElementsByTagName(ZipParser.SRC);
			if (nl != null && nl.getLength() > 0) {
				Element srcElement = (Element) nl.item(0);
				src = srcElement.getTextContent();
			}

			nl = config.getElementsByTagName(ZipParser.TARGET);
			if (nl != null && nl.getLength() > 0) {
				Element targetElement = (Element) nl.item(0);
				target = targetElement.getTextContent();
			}

			nl = config.getElementsByTagName(ZipParser.TRANSFORMER);
			if (nl != null && nl.getLength() > 0) {
				Element transformerElement = (Element) nl.item(0);
				transformer = transformerElement.getTextContent();
			}

			nl = config.getElementsByTagName(ZipParser.MASTER_FILE);
			if (nl != null && nl.getLength() > 0) {
				Element masterFileElement = (Element) nl.item(0);
				masterFile = masterFileElement.getTextContent();
			}

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			writer = new StringWriter();
			transformer.transform(new DOMSource(config), new StreamResult(
					writer));
			
			String xml = writer.toString();
			log.info("CONFIG FILE: " + xml);

			bai = new ByteArrayInputStream(xml.getBytes("UTF-8"));

		} catch (ParserConfigurationException pce) {
			log.error(pce.getMessage(), pce);
		} catch (SAXException se) {
			log.error(se.getMessage(), se);
		} catch (IOException ioe) {
			log.error(ioe.getMessage(), ioe);
		} catch (TransformerException e) {
			log.error(e.getMessage(), e);
		}

		return bai;

	}

	public InputStream getSource() {
		return source;
	}

	public void setSource(ZipInputStream source) {
		this.source = source;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getTransformer() {
		return transformer;
	}

	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getMasterFile() {
		return masterFile;
	}

	public void setMasterFile(String masterFile) {
		this.masterFile = masterFile;
	}

}
