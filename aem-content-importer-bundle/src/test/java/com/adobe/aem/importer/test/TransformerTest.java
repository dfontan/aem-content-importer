package com.adobe.aem.importer.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.adobe.aem.importer.XMLTransformer;
import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.impl.XMLTransformerDITAImpl;
import com.adobe.aem.importer.impl.XMLTransformerDOCBOOKImpl;
import com.adobe.aem.importer.test.integration.DITAIntegrationTest;
import com.adobe.aem.importer.xml.RejectingEntityResolver;

public class TransformerTest {

	private static final Logger log = LoggerFactory
			.getLogger(TransformerTest.class);

	private static TransformerHelper th = new TransformerHelper();

	@BeforeClass
	public static void init() {
		th.addTransformer(new XMLTransformerDITAImpl());
		th.addTransformer(new XMLTransformerDOCBOOKImpl());
	}

	@Test
	public void retrieveDITATransformerXSLT() {

		try {
			XMLTransformer ditaTransformer = XMLTransformerHelper
					.getXMLTransformer(XMLTransformerDITAImpl.class.getName());
			boolean exist = false;
			if (ditaTransformer != null) {
				exist = true;
			}
			assertTrue(exist);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			assertTrue(false);
		}

	}

	@Test
	public void retrieveDOCBOOKTransformerXSLT() {

		try {
			XMLTransformer docBookTransformer = XMLTransformerHelper
					.getXMLTransformer(XMLTransformerDOCBOOKImpl.class
							.getName());
			boolean exist = false;
			if (docBookTransformer != null) {
				exist = true;
			}
			assertTrue(exist);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			assertTrue(false);
		}

	}

	@Test(expected = Exception.class)
	public void nonExistsTransformerXSLT() throws Exception {
		XMLTransformerHelper.getXMLTransformer(TransformerTest.class.getName());
	}
	
	@Test
	public void makeDitaTransformerXSLT() {
		try {
			XMLTransformer ditaTransformer = XMLTransformerHelper
					.getXMLTransformer(XMLTransformerDITAImpl.class.getName());
			boolean success = false;
			if (ditaTransformer != null) {
				ClassLoader classLoader = DITAIntegrationTest.class
						.getClassLoader();

				String currentPath = DITAIntegrationTest.class
						.getProtectionDomain().getCodeSource().getLocation()
						.getPath();
				File file = new File(currentPath);

				File xsltFile = new File(
						file.getParentFile().getParentFile().getParentFile()
								.getPath()
								+ "/aem-content-importer-content/src/main/content/jcr_root/apps/aem-importer/resources/dita-to-content.xsl");

				InputStream xsltInput = new FileInputStream(xsltFile);

				// Create XML Reader
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				xmlReader.setEntityResolver(new RejectingEntityResolver());

				TransformerFactoryImpl transformFactory = new TransformerFactoryImpl();

				transformFactory
						.setURIResolver(new DITATransformerXSLTResolverTest(
								xsltFile.getPath(), "", xmlReader));

				Transformer xsltTransformer = transformFactory
						.newTransformer(new StreamSource(xsltInput));

				File configExpectedParams = new File(classLoader.getResource(
						"config_params_DITA.xml").getFile());

				FileInputStream fis = new FileInputStream(configExpectedParams);
				Properties configProperties = new Properties();
				configProperties.loadFromXML(fis);

				/* Pass all properties to XSLT transformer */
				for (Entry<Object, Object> entry : configProperties.entrySet())
					xsltTransformer.setParameter(entry.getKey().toString(),
							entry.getValue());

				// Transform
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
				File masterFile = new File(classLoader.getResource(
						"xsltDITAFiles/mcloud.ditamap").getFile());
				InputStream input = new FileInputStream(masterFile);

				xsltTransformer.transform(new SAXSource(xmlReader,
						new InputSource(input)), new StreamResult(output));

				XMLUnit.setIgnoreWhitespace(true);
				File content = new File(classLoader.getResource("content.xml")
						.getFile());
				InputStream contentInput = new FileInputStream(content);

				InputStream bis = new ByteArrayInputStream(output.toByteArray());

				Diff diff = XMLUnit.compareXML(new InputSource(contentInput),
						new InputSource(bis));
				success = diff.identical();

			}
			assertTrue(success);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			assertTrue(false);
		}
	}

//	@Test
	public void makeDocBookTransformerXSLT() {
		try {
			XMLTransformer ditaTransformer = XMLTransformerHelper
					.getXMLTransformer(XMLTransformerDITAImpl.class.getName());
			boolean success = false;
			if (ditaTransformer != null) {
				ClassLoader classLoader = DITAIntegrationTest.class
						.getClassLoader();

				String currentPath = DITAIntegrationTest.class
						.getProtectionDomain().getCodeSource().getLocation()
						.getPath();
				File file = new File(currentPath);

				File xsltFile = new File(
						file.getParentFile().getParentFile().getParentFile()
								.getPath()
								+ "/aem-content-importer-content/src/main/content/jcr_root/apps/aem-importer/resources/docbook-to-content.xsl");

				InputStream xsltInput = new FileInputStream(xsltFile);

				// Create XML Reader
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				xmlReader.setEntityResolver(new RejectingEntityResolver());

				TransformerFactoryImpl transformFactory = new TransformerFactoryImpl();

				transformFactory
						.setURIResolver(new DOCBOOKTransformerXSLTResolverTest(
								xsltFile.getPath(), "", xmlReader));

				Transformer xsltTransformer = transformFactory
						.newTransformer(new StreamSource(xsltInput));

				File configExpectedParams = new File(classLoader.getResource(
						"config_params_DOCBOOK.xml").getFile());

				FileInputStream fis = new FileInputStream(configExpectedParams);
				Properties configProperties = new Properties();
				configProperties.loadFromXML(fis);

				/* Pass all properties to XSLT transformer */
				for (Entry<Object, Object> entry : configProperties.entrySet())
					xsltTransformer.setParameter(entry.getKey().toString(),
							entry.getValue());

				// Transform
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
				File masterFile = new File(classLoader.getResource(
						"xsltDOCBOOKFiles/mcloud.ditamap").getFile());
				InputStream input = new FileInputStream(masterFile);

				xsltTransformer.transform(new SAXSource(xmlReader,
						new InputSource(input)), new StreamResult(output));

				XMLUnit.setIgnoreWhitespace(true);
				File content = new File(classLoader.getResource("content.xml")
						.getFile());
				InputStream contentInput = new FileInputStream(content);

				InputStream bis = new ByteArrayInputStream(output.toByteArray());

				Diff diff = XMLUnit.compareXML(new InputSource(contentInput),
						new InputSource(bis));
				success = diff.identical();

			}
			assertTrue(success);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			assertTrue(false);
		}
	}

	private class DITATransformerXSLTResolverTest implements URIResolver {
		/* XSLT Node */
		private String xslt;
		/* Source Node */
		private String src;
		/* XML Reader */
		private XMLReader xmlReader;

		/**
		 * Constructor
		 * 
		 * @param xsltNode
		 * @param src
		 * @param xmlReader
		 */
		public DITATransformerXSLTResolverTest(String xslt, String src,
				XMLReader xmlReader) {
			this.xslt = xslt;
			this.src = src;
			this.xmlReader = xmlReader;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
		 * java.lang.String)
		 */
		@Override
		public Source resolve(String href, String base)
				throws TransformerException {
			try {

				ClassLoader classLoader = DITAIntegrationTest.class
						.getClassLoader();

				File resource = new File(classLoader.getResource(
						"xsltDITAFiles/" + href).getFile());
				
				InputStream inputResource = new FileInputStream(resource);

				return new SAXSource(this.xmlReader, new InputSource(
						inputResource));
			} catch (FileNotFoundException e) {
				throw new TransformerException("Cannot resolve href=[" + href
						+ "]");
			}
		}

	}

	private class DOCBOOKTransformerXSLTResolverTest implements URIResolver {
		/* XSLT Node */
		private String xslt;
		/* Source Node */
		private String src;
		/* XML Reader */
		private XMLReader xmlReader;

		/**
		 * Constructor
		 * 
		 * @param xsltNode
		 * @param src
		 * @param xmlReader
		 */
		public DOCBOOKTransformerXSLTResolverTest(String xslt, String src,
				XMLReader xmlReader) {
			this.xslt = xslt;
			this.src = src;
			this.xmlReader = xmlReader;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
		 * java.lang.String)
		 */
		@Override
		public Source resolve(String href, String base)
				throws TransformerException {
			try {
				
				if (href != null) {
			      boolean isEmptyHRef = href.equals("");
			      if (!isEmptyHRef) {
			    	  ClassLoader classLoader = DITAIntegrationTest.class
			    			  .getClassLoader();
			    	  
			    	  File resource = new File(classLoader.getResource("xsltDOCBOOKFiles/" + href).getFile());
			    	  InputStream inputResource = new FileInputStream(resource);
			    	  
			    	  return new SAXSource(this.xmlReader, new InputSource(
			    			  inputResource));
			      }
				}
				
				return new SAXSource();
				
			} catch (FileNotFoundException e) {
				throw new TransformerException("Cannot resolve href=[" + href
						+ "]");
			}
		}

	}

}
