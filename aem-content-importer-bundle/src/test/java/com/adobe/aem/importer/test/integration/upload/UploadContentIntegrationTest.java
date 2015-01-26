/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/


package com.adobe.aem.importer.test.integration.upload;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.codec.binary.Base64;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adobe.aem.importer.test.utils.HttpClientUtils;
import com.adobe.aem.importer.xml.Config;

public class UploadContentIntegrationTest {

	private final static String PATH_NODE = "/content/aem-importer-test/upload-content";
	private final static String SLING_RESOURCETYPE = "aem-importer/components/upload-content";
	private final String POST_URL = "http://localhost:4502" + PATH_NODE + "/_jcr_content";
	private final String CONFIG_PARAM_SERVER = "http://localhost:4502";
	private final static String URL_REPO = "http://localhost:4502/crx/server";
	private final static String USERNAME = "admin";
	private final static String PASSWORD = "admin";
	private final String CONFIG_PATH_RESULT = "configPathResult";
	private static File zipFile = null;
	private static File configExpectedParams = null;
	private static Session session = null;

	@BeforeClass
	public static void init() {
		ClassLoader classLoader = UploadContentIntegrationTest.class.getClassLoader();
		zipFile = new File(classLoader.getResource("exampleTest.zip").getFile());
		
		configExpectedParams = new File(classLoader.getResource(
				"config_params.xml").getFile());

		try {
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
			
			

		} catch (RepositoryException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}
	
	/**
	 * Test creation content by filling out a POST to upload component
	 */
	@Test
	public void createContentByFillingOutForm() {

		Config config = new Config();
		JSONObject jsonResult = null;

		try {

			config.setSrc("/var/aem-importer/importTest1");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/pando");
			config.setTransformer("com.adobe.aem.importer.impl.XMLTransformerDITAImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/dita-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");
			
			Properties expectedProperties = createExpectedProperties(config);

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME,
					PASSWORD, config, null);

			URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
					+ jsonResult.get(CONFIG_PATH_RESULT)).openConnection();
			String userpass = USERNAME + ":" + PASSWORD;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			urlConnection.setRequestProperty("Authorization", basicAuth);

			Properties properties = new Properties();
			properties.loadFromXML(urlConnection.getInputStream());

			assertTrue(checkProperties(expectedProperties,properties));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void createContentByZipFile() {
		JSONObject jsonResult = null;
		Properties expectedProperties = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFile);

			URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
					+ jsonResult.get(CONFIG_PATH_RESULT)).openConnection();
			String userpass = USERNAME + ":" + PASSWORD;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			urlConnection.setRequestProperty("Authorization", basicAuth);

			Properties properties = new Properties();
			properties.loadFromXML(urlConnection.getInputStream());

			assertTrue(checkProperties(expectedProperties,properties));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void createContentByFillingOutFormAndZipFile() {
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
					config, zipFile);

			URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
					+ jsonResult.get(CONFIG_PATH_RESULT)).openConnection();
			String userpass = USERNAME + ":" + PASSWORD;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			urlConnection.setRequestProperty("Authorization", basicAuth);

			Properties properties = new Properties();
			properties.loadFromXML(urlConnection.getInputStream());

			assertTrue(checkProperties(expectedProperties,properties));
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	private Properties createExpectedProperties(Config config) {
		Properties properties = new Properties();
		
		properties.setProperty("src", config.getSrc());
		properties.setProperty("target", config.getTarget());
		properties.setProperty("masterFile", config.getMasterFile());
		properties.setProperty("xml-transformer", config.getTransformer());
		
		if (config.getCustomProps() != null) {
			ByteArrayInputStream bai = new ByteArrayInputStream(config.getCustomProps().getBytes());
			try {
				properties.load(bai);
			} catch (IOException e) {
				System.out.println("Error loading expected custom properties");
			}
		}
		
		return properties;
		
	}
	
	private Boolean checkProperties(Properties expectedProperties, Properties properties) {
		
		Boolean equals = true;
		
		for (Object key : expectedProperties.keySet()) {
			String expectedValue = expectedProperties.getProperty((String)key);
			String value = properties.getProperty((String)key);
			
			if (!expectedValue.equals(value)) {
				equals = false;
			}
			
		}
		
		return equals;
		
	}
	
}
