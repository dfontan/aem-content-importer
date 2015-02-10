/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/


package com.adobe.aem.importer.test.integration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.adobe.aem.importer.test.utils.HttpClientUtils;
import com.adobe.aem.importer.xml.Config;
import com.adobe.aem.importer.xml.utils.ZipHelper;

@FixMethodOrder(MethodSorters.JVM)
public class DOCBOOKIntegrationTest extends AbstractIntegrationTest {

	private static File zipFileWithFolder = null;
	private static File zipFileWithoutFolder = null;
	private static File configExpectedParams = null;
	private static Session session = null;
	
	private final String TEXT_CONTENT = "The Marketing Cloud is an integrated family of digital marketing";
	
	@BeforeClass
	public static void init()  {
		try {
			ClassLoader classLoader = DOCBOOKIntegrationTest.class.getClassLoader();
			
			String zipNameWithOutFolder = System.currentTimeMillis() + ".zip";
			
			ZipHelper.zipDir(classLoader.getResource("exampleDITA").getFile(), zipNameWithOutFolder);
			
			zipFileWithoutFolder = new File(zipNameWithOutFolder);
			zipFileWithFolder = createZipFileWithFolder(classLoader.getResource("exampleDOCBOOK").getFile(), System.currentTimeMillis() + ".zip", "exampleDOCBOOK");
			
			configExpectedParams = new File(classLoader.getResource(
					"config_params_DOCBOOK.xml").getFile());

			Repository repo = JcrUtils.getRepository(URL_REPO);
			
			SimpleCredentials creds = new SimpleCredentials(USERNAME,
					PASSWORD.toCharArray());
			session = repo.login(creds, "crx.default");
			
			
			if (!session.itemExists(PATH_NODE)) {
				Node page = JcrUtils.getOrCreateByPath(PATH_NODE, "cq:Page", session);
				Node jcrContent = page.addNode("jcr:content", "cq:PageContent");
				jcrContent.setProperty("sling:resourceType", SLING_RESOURCETYPE);
				session.save();
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}
	
	/**
	 * Test creation config file to make a transformation by filling out a POST to upload component
	 */
	@Test
	public void testConfigFileByFillingOutForm() {

		Config config = new Config();
		JSONObject jsonResult = null;

		try {

			config.setSrc("/var/aem-importer/importTest1");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/pando");
			config.setTransformer("com.adobe.aem.importer.impl.XMLTransformerDocbookImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/docbook-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");
			
			Properties expectedProperties = createExpectedProperties(config);

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME,
					PASSWORD, config, null);

			Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
			assertTrue(checkProperties(expectedProperties,properties));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}
	
	
	/**
	 * Test creation config file to make a transformation by uploading a zip file with all necessary files at root to upload component
	 */
	@Test
	public void testConfigFileByZipFile() {
		JSONObject jsonResult = null;
		Properties expectedProperties = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFileWithFolder);

			
			if("false".equalsIgnoreCase((String)jsonResult.get("error"))) {
				Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
				assertTrue(checkProperties(expectedProperties,properties));
			} else {
				assertTrue(false);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}

	
	
	/**
	 * Test creation config file to make a transformation by uploading a zip file with all necessary files at root to upload component and filling out some parameters two.
	 * Configuration inside zipfile will have priority from that parameters.
	 */
	@Test
	public void testConfigFileByFillingOutFormAndZipFile() {
		JSONObject jsonResult = null;
		Properties expectedProperties = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);

			Config config = new Config();
			
			config.setSrc("/var/aem-importer/importTest2");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/pando");
			config.setTransformer("com.adobe.aem.importer.impl.XMLTransformerDocbookImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/docbook-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					config, zipFileWithFolder);

			Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
			assertTrue(checkProperties(expectedProperties,properties));
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	/**
	 * Test creation config file to make a transformation by uploading a zip file with all necessary files inside a folder to upload component.
	 */
	@Test
	public void testConfigFileByZipFileWithFolder() {
		JSONObject jsonResult = null;
		Properties expectedProperties = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFileWithFolder);

			
			if("false".equalsIgnoreCase((String)jsonResult.get("error"))) {
				Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
				assertTrue(checkProperties(expectedProperties,properties));
			} else {
				assertTrue(false);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}
	
	/**
	 * Test creation content page made by transformation process, filling out form parameters.
	 */
	@Test
	public void testContentPageByFillingOutForm() {
		Config config = new Config();
		JSONObject jsonResult = null;

		try {

			config.setSrc("/var/aem-importer/zip");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/docbook-import-test");
			config.setTransformer("com.adobe.aem.importer.impl.XMLTransformerDocbookImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/docbook-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");
			
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME,
					PASSWORD, config, null);
			
			if("false".equalsIgnoreCase((String)jsonResult.get("error"))) {
				//2 seconds sleeping execution to finish workflow process with the content page to be generated
				Thread.sleep(MILLISECONDS);
				
				String target = config.getTarget();
				
				JSONObject nodeInfo = retrieveNodeInfoFromJCR(target + "/home/_jcr_content/par/text.json");
				
				String text = nodeInfo.getString("text");
				
				if (text != null) {
					assertTrue(text.contains(TEXT_CONTENT));
					
				} else {
					assertTrue(false);
				}
			} else {
				assertTrue(false);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}
	
	/**
	 * Test creation content page made by transformation process, uploading a zip file.
	 */
	@Test
	public void testContentPageByZipFile() {
		JSONObject jsonResult = null;
		
		try {
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFileWithoutFolder);
			
			if("false".equalsIgnoreCase((String)jsonResult.get("error"))) {
				
				Properties configProperties = retrieveConfigPropertiesFromJCR(jsonResult);
				
				//2 seconds sleeping execution to finish workflow process with the content page to be generated
				Thread.sleep(MILLISECONDS);
				
				String target = configProperties.getProperty(TARGET_PROP);
				
				JSONObject nodeInfo = retrieveNodeInfoFromJCR(target + "/home/_jcr_content/par/text.json");
				
				String text = nodeInfo.getString("text");
				
				if (text != null) {
					assertTrue(text.contains(TEXT_CONTENT));
					
				} else {
					assertTrue(false);
				}
				
				
				
				
			} else {
				assertTrue(false);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}
	
	@AfterClass
	public static void finish() {
		if (zipFileWithFolder != null) {
			zipFileWithFolder.delete();
		}
		
		if (zipFileWithoutFolder != null) {
			zipFileWithoutFolder.delete();
		}
	}
	
}
