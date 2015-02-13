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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.test.utils.HttpClientUtils;
import com.adobe.aem.importer.xml.Config;
import com.adobe.aem.importer.xml.utils.ZipHelper;

@FixMethodOrder(MethodSorters.JVM)
public class DITAIntegrationTest extends AbstractIntegrationTest {

	private static File zipFileWithFolder = null;
	private static File zipFileWithoutFolder = null;
	private static File configExpectedParams = null;
	private static Session session = null;
	private static Logger log = LoggerFactory
			.getLogger(DITAIntegrationTest.class);

	private final String TEXT_CONTENT = "The Marketing Cloud is an integrated family of digital marketing";
	
	@BeforeClass
	public static void init() throws Exception {
		try {
			ClassLoader classLoader = DITAIntegrationTest.class
					.getClassLoader();

			String zipNameWithOutFolder = System.currentTimeMillis() + ".zip";

			ZipHelper.zipDir(classLoader.getResource("ditaExamples/mcloud")
					.getFile(), zipNameWithOutFolder);

			zipFileWithoutFolder = new File(zipNameWithOutFolder);
			zipFileWithFolder = createZipFileWithFolder(classLoader
					.getResource("ditaExamples/mcloud").getFile(),
					System.currentTimeMillis() + ".zip", "mcloud");

			configExpectedParams = new File(classLoader.getResource(
					"ditaExamples/mcloud/0_config_params.xml").getFile());
			
			Repository repo = JcrUtils.getRepository(URL_REPO);

			SimpleCredentials creds = new SimpleCredentials(USERNAME,
					PASSWORD.toCharArray());
			session = repo.login(creds, "crx.default");

			if (!session.itemExists(PATH_NODE)) {
				Node page = JcrUtils.getOrCreateByPath(PATH_NODE, "cq:Page",
						session);
				Node jcrContent = page.addNode("jcr:content", "cq:PageContent");
				jcrContent
						.setProperty("sling:resourceType", SLING_RESOURCETYPE);
				session.save();
			}

		} catch (RepositoryException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

	/**
	 * Test creation config file to make a transformation by filling out a POST
	 * to upload component
	 */
	@Test
	public void testConfigFileByFillingOutForm() {
		log.info("Executing test: testConfigFileByFillingOutForm");
		Config config = new Config();
		JSONObject jsonResult = null;

		try {

			config.setSrc("/var/aem-importer/importTest1");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/pando");
			config.setTransformer("com.adobe.aem.importer.impl.XMLTransformerDITAImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/dita-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");

			Properties expectedProperties = createExpectedProperties(config);

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					config, null);

			Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
			assertTrue(checkProperties(expectedProperties, properties));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}

	/**
	 * Test creation config file to make a transformation by uploading a zip
	 * file with all necessary files at root to upload component
	 */
	@Test
	public void testConfigFileByZipFile() {
		log.info("Executing test: testConfigFileByZipFile");
		JSONObject jsonResult = null;
		Properties expectedProperties = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFileWithFolder);

			if ("false".equalsIgnoreCase((String) jsonResult.get("error"))) {
				Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
				assertTrue(checkProperties(expectedProperties, properties));
			} else {
				assertTrue(false);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}

	/**
	 * Test creation config file to make a transformation by uploading a zip
	 * file with all necessary files at root to upload component and filling out
	 * some parameters two. Configuration inside zipfile will have priority from
	 * that parameters.
	 */
	@Test
	public void testConfigFileByFillingOutFormAndZipFile() {
		log.info("Executing test: testConfigFileByFillingOutFormAndZipFile");
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
			config.setTransformer("com.adobe.aem.importer.impl.XMLTransformerDITAImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/dita-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					config, zipFileWithFolder);

			Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
			assertTrue(checkProperties(expectedProperties, properties));
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	/**
	 * Test creation config file to make a transformation by uploading a zip
	 * file with all necessary files inside a folder to upload component.
	 */
	@Test
	public void testConfigFileByZipFileWithFolder() {
		log.info("Executing test: testConfigFileByZipFileWithFolder");
		JSONObject jsonResult = null;
		Properties expectedProperties = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFileWithFolder);

			if ("false".equalsIgnoreCase((String) jsonResult.get("error"))) {
				Properties properties = retrieveConfigPropertiesFromJCR(jsonResult);
				assertTrue(checkProperties(expectedProperties, properties));
			} else {
				assertTrue(false);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}

	/**
	 * Test creation content page made by transformation process, filling out
	 * form parameters.
	 */
	@Test
	public void testContentPageByFillingOutForm() {
		log.info("Executing test: testContentPageByFillingOutForm");
		Config config = new Config();
		JSONObject jsonResult = null;

		try {

			config.setSrc("/var/aem-importer/zip");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/dita-mcloud-import");
			config.setTransformer("com.adobe.aem.importer.impl.XMLTransformerDITAImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/dita-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					config, null);

			if ("false".equalsIgnoreCase((String) jsonResult.get("error"))) {
				// 2 seconds sleeping execution to finish workflow process with
				// the content page to be generated
				Thread.sleep(MILLISECONDS);

				String target = config.getTarget();

				JSONObject nodeInfo = retrieveNodeInfoFromJCR(target
						+ "/home/_jcr_content/par/text.json");

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
	 * Test creation content page made by transformation process, uploading a
	 * zip file.
	 */
	@Test
	public void testContentPageByZipFile() {
		log.info("Executing test: testContentPageByZipFile");
		JSONObject jsonResult = null;

		try {
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFileWithoutFolder);

			if ("false".equalsIgnoreCase((String) jsonResult.get("error"))) {

				Properties configProperties = retrieveConfigPropertiesFromJCR(jsonResult);

				// 2 seconds sleeping execution to finish workflow process with
				// the content page to be generated
				Thread.sleep(MILLISECONDS);

				String target = configProperties.getProperty(TARGET_PROP);

				JSONObject nodeInfo = retrieveNodeInfoFromJCR(target
						+ "/home/_jcr_content/par/text.json");

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

	/*
	 * TESTING DIFFERENT KIND OF EXAMPLES
	 */
	 @Test
	public void testContentPageInternal() {
		log.info("Executing test: testContentPageInternal");
		assertTrue(postContent("ditaExamples/internal", "/localization/jcr:content/par/text.json", "text", "Contact Melissa Baerwald for localization schedules, budgets", MILLISECONDS));
	}

	@Test
	public void testContentPageReference() {
		log.info("Executing test: testContentPageReference");
		assertTrue(postContent("ditaExamples/reference", "/contact_and_legal/jcr:content/par/text.json", "text", "Help & Technical Support", 30000L));
	}

	@Test
	public void testContentPageScAppMeasurementPhp() {
		log.info("Executing test: testContentPageScAppMeasurementPhp");
		assertTrue(postContent("ditaExamples/sc/appmeasurement/php", "/c_prf/jcr:content/par/text.json", "text", "It lets you capture certain types of activity on your website", MILLISECONDS));
	}
	
	@Test
	public void testContentPageScAppMeasurementRelease() {
		log.info("Executing test: testContentPageScAppMeasurementRelease");
		assertTrue(postContent("ditaExamples/sc/appmeasurement/release", "/c_release_notes/jcr:content/par/text.json", "text", "The latest version of each library can be downloaded in", MILLISECONDS));
	}
	
	@Test
	public void testContentPageScAppMeasurementSymbian() {
		log.info("Executing test: testContentPageScAppMeasurementSymbian");
		assertTrue(postContent("ditaExamples/sc/appmeasurement/symbian", "/jcr:content.json", "jcr:title", "AppMeasurement Libraries for Symbian", MILLISECONDS));
	}
	
	@Test
	public void testContentPageScAppMeasurementVideo() {
		log.info("Executing test: testContentPageScAppMeasurementVideo");
		assertTrue(postContent("ditaExamples/sc/appmeasurement/video", "/video_tracking/jcr:content/par/text.json", "text", "Adobe has released a new way to measure ", MILLISECONDS));
	}
	
	@Test
	public void testContentPageScDataSource() {
		log.info("Executing test: testContentPageScDataSource");
		assertTrue(postContent("ditaExamples/sc/datasources", "/datasrc_home/jcr:content/par/text.json", "text", "By integrating offline data, you can leverage data sources to integrate", MILLISECONDS));
	}
	
	@Test
	public void testContentPageScImplement() {
		log.info("Executing test: testContentPageScImplement");
		assertTrue(postContent("ditaExamples/sc/implement", "/integrate_overview/jcr:content/par/text.json", "text", "The Integrate Module lets an Adobe online marketing partner integrate", MILLISECONDS));
	}
	
	@Test
	public void testContentPageScUpgrade() {
		log.info("Executing test: testContentPageScUpgrade");
		assertTrue(postContent("ditaExamples/sc/upgrade", "/upgrade_home/preface/jcr:content/par/text.json", "text", "Document Conventions", MILLISECONDS));
	}
	
	@Test
	public void testContentPageScUser() {
		log.info("Executing test: testContentPageScUser");
		assertTrue(postContent("ditaExamples/sc/user", "/home/jcr:content/par/text.json", "text", "Marketing reports and analytics provide a hosted, subscription-based", MILLISECONDS));
	}
	
	@Test
	public void testContentPageTnt() {
		log.info("Executing test: testContentPageTnt");
		assertTrue(postContent("ditaExamples/tnt", "/t_Setting_Up_Your_Site/jcr:content/par/text.json", "text", "Consider both the design of your page", 4000L));
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

	private Boolean postContent(String sourceFolder, String urlToCheck, String propertyToCheck, String expectedValue, Long milliSecondsToWait) {
		JSONObject jsonResult = null;
		File zipFile = null;
		Boolean success = false;
		try {
			ClassLoader classLoader = DITAIntegrationTest.class
					.getClassLoader();
			zipFile = createZipFileWithFolder(
					classLoader.getResource(sourceFolder
							).getFile(),
					System.currentTimeMillis() + ".zip", System.currentTimeMillis() + "");
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFile);

			if ("false".equalsIgnoreCase((String) jsonResult.get("error"))) {

				Properties configProperties = retrieveConfigPropertiesFromJCR(jsonResult);

				// Sleeping execution to finish workflow process with
				// the content page to be generated
				Thread.sleep(milliSecondsToWait);

				String target = configProperties.getProperty(TARGET_PROP);

				
				JSONObject nodeInfo = retrieveNodeInfoFromJCR(target
						+ urlToCheck);

				String text = nodeInfo.getString(propertyToCheck);

				if (text != null) {
					success = text.contains(expectedValue);
				}
				
			}
			
			//Waiting 2 seconds between each test, giving up permeating AEM
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			assertTrue(false);
		} finally {
			if (zipFile != null) {
				zipFile.delete();
			}
		}
		
		
		return success;
	}

}
