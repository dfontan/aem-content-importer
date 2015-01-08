package com.adobe.aem.importer.test.transformer;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.NotActiveException;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.adobe.aem.importer.XMLTransformer;
import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.impl.DITATransformerXSLTImpl;
import com.adobe.aem.importer.test.integration.upload.UploadContentIntegrationTest;
import com.day.jcr.vault.util.RejectingEntityResolver;

public class TransformerTest {
	
	private static final Logger log = LoggerFactory.getLogger(TransformerTest.class);
	
	private static TransformerHelper th = new TransformerHelper();
	
	
	@BeforeClass
	public static void init() {
		th.addTransformer(new DITATransformerXSLTImpl());
	}
	
//	@Test
	public void retrieveDITATransformerXSLT() {
		
		try {
			XMLTransformer ditaTransformer = XMLTransformerHelper.getXMLTransformer(DITATransformerXSLTImpl.class.getName());
			boolean exist = false;
			if (ditaTransformer != null) {
				exist = true;
			}
			assertTrue(exist);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			assertTrue(false);
		}
		
	}
	
	@Test
	public void makeDITATransformerXSLT() {
		try {
			XMLTransformer ditaTransformer = XMLTransformerHelper.getXMLTransformer(DITATransformerXSLTImpl.class.getName());
			boolean success = false;
			if (ditaTransformer != null) {
				ClassLoader classLoader = UploadContentIntegrationTest.class.getClassLoader();
				
				File xsltFile = new File(classLoader.getResource("dita-to-content.xsl").getFile());
				InputStream xsltInput = new FileInputStream(xsltFile);
				
				Transformer xsltTransformer = new TransformerFactoryImpl().newTransformer(new StreamSource(xsltInput));
				
				File configExpectedParams = new File(classLoader.getResource(
						"config_params.xml").getFile());
				
				FileInputStream fis = new FileInputStream(configExpectedParams);
				Properties configProperties = new Properties();
				configProperties.loadFromXML(fis);
				
				/* Pass all properties to XSLT transformer */
				for(Entry<Object, Object> entry : configProperties.entrySet())
					xsltTransformer.setParameter(entry.getKey().toString(), entry.getValue());
				
				// Create XML Reader
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				xmlReader.setEntityResolver(new RejectingEntityResolver());
				
				// Transform
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
				File masterFile = new File(classLoader.getResource("xsltFiles/mcloud.ditamap").getFile());
				InputStream input = new FileInputStream(masterFile);
				
				xsltTransformer.transform(new SAXSource(xmlReader, new InputSource(input)), new StreamResult(output));
				
			}
			assertTrue(success);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			assertTrue(false);
		}
	}
	
	
	
	
	

}
