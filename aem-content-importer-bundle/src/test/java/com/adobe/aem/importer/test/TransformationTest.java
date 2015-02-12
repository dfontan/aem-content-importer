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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.adobe.aem.importer.XMLTransformer;
import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.impl.XMLTransformerDITAImpl;
import com.adobe.aem.importer.impl.XMLTransformerDocBookImpl;
import com.adobe.aem.importer.test.integration.DITAIntegrationTest;
import com.adobe.aem.importer.xml.RejectingEntityResolver;

@FixMethodOrder(MethodSorters.JVM)
public class TransformationTest {

	private static final Logger log = LoggerFactory
			.getLogger(TransformationTest.class);

	private static TransformerHelper th = new TransformerHelper();

	@BeforeClass
	public static void init() {
		th.addTransformer(new XMLTransformerDITAImpl());
		th.addTransformer(new XMLTransformerDocBookImpl());
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
					.getXMLTransformer(XMLTransformerDocBookImpl.class
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
		XMLTransformerHelper.getXMLTransformer(TransformationTest.class.getName());
	}

	@Test
	public void makeInternalDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/internal/", "0_config_params.xml", "internal.ditamap","ditaExamples/contents/internal/content.xml"));
	}

	@Test
	public void makeMcloudDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/mcloud/", "0_config_params.xml", "mcloud.ditamap","ditaExamples/contents/mcloud/content.xml"));
	}

	@Test
	public void makeReferenceDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/reference/", "0_config_params.xml", "reference.ditamap","ditaExamples/contents/reference/content.xml"));
	}

	@Test
	public void makeScAppMeasurementPhpDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/appmeasurement/php/", "0_config_params.xml", "php.ditamap","ditaExamples/contents/sc/appmeasurement/php/content.xml"));
	}

	@Test
	public void makeScAppMeasurementReleaseDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/appmeasurement/release/", "0_config_params.xml", "release_notes_appmeasurement.ditamap","ditaExamples/contents/sc/appmeasurement/release/content.xml"));
	}

	@Test
	public void makeScAppMeasurementSymbianDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/appmeasurement/symbian/", "0_config_params.xml", "symbian.ditamap","ditaExamples/contents/sc/appmeasurement/symbian/content.xml"));
	}

	@Test
	public void makeScAppMeasurementVideoDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/appmeasurement/video/", "0_config_params.xml", "sc-video-measurement.ditamap","ditaExamples/contents/sc/appmeasurement/video/content.xml"));
	}

	@Test
	public void makeScDataSourcesDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/datasources/", "0_config_params.xml", "oms_sc_data_sources.ditamap","ditaExamples/contents/sc/datasources/content.xml"));
	}

	@Test
	public void makeScImplementDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/implement/", "0_config_params.xml", "integrate.ditamap","ditaExamples/contents/sc/implement/content.xml"));
	}

	@Test
	public void makeScUpgradeDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/upgrade/", "0_config_params.xml", "SiteCatalyst_15_Upgrade.ditamap","ditaExamples/contents/sc/upgrade/content.xml"));
	}

	@Test
	public void makeScUserDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/sc/user/", "0_config_params.xml", "oms_sc_user.ditamap","ditaExamples/contents/sc/user/content.xml"));
	}

	@Test
	public void makeTntDitaTransformerXSLT() {
		assertTrue(makeDITATransformation("ditaExamples/tnt/", "0_config_params.xml", "index.ditamap","ditaExamples/contents/tnt/content.xml"));
	}


	//TODO: Develop that test with the corresponding specifications
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
								"docBookExamples/example", xmlReader));

				transformFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
				Transformer xsltTransformer = transformFactory
						.newTransformer(new StreamSource(xsltInput));

				File configExpectedParams = new File(classLoader.getResource(
						"docBookExamples/example/0_config_params.xml").getFile());

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
						"docBookExamples/example/mcloud.ditamap").getFile());
				InputStream input = new FileInputStream(masterFile);

				xsltTransformer.transform(new SAXSource(xmlReader,
						new InputSource(input)), new StreamResult(output));

				XMLUnit.setIgnoreWhitespace(true);
				File content = new File(classLoader.getResource("docBookExamples/contents/example/content.xml")
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

	private Boolean makeDITATransformation(String srcFiles, String configFileName, String ditaMapName, String expectedContextPath) {
		boolean success = false;
		try {
			XMLTransformer ditaTransformer = XMLTransformerHelper
					.getXMLTransformer(XMLTransformerDITAImpl.class.getName());
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
						.setURIResolver(new DITATransformerXSLTResolverTest(srcFiles, xmlReader));

				Transformer xsltTransformer = transformFactory
						.newTransformer(new StreamSource(xsltInput));

				File configExpectedParams = new File(classLoader.getResource(
						srcFiles + configFileName).getFile());

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
						srcFiles + ditaMapName).getFile());
				InputStream input = new FileInputStream(masterFile);

				xsltTransformer.transform(new SAXSource(xmlReader,
						new InputSource(input)), new StreamResult(output));

				XMLUnit.setIgnoreWhitespace(true);
				File content = new File(classLoader.getResource(expectedContextPath)
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
		}

		return success;
	}

	private class DITATransformerXSLTResolverTest implements URIResolver {
		/* Source Files */
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
		public DITATransformerXSLTResolverTest(String src,
				XMLReader xmlReader) {
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
						src + href).getFile());

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
		/* Source Files */
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
		public DOCBOOKTransformerXSLTResolverTest(String src,
				XMLReader xmlReader) {
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

			    	  File resource = new File(classLoader.getResource(src + href).getFile());
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
