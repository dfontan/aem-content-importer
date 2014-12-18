package com.adobe.aem.importer.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.codec.binary.Base64;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONObject;

import com.adobe.aem.importer.test.utils.HttpClientUtils;
import com.adobe.aem.importer.xml.Config;

public class TestUploadContent {

	private final String POST_URL = "http://localhost:4502/content/resources/help/en/upload-content/_jcr_content";
	private final String CONFIG_PARAM_SERVER = "http://localhost:4502";
	private final static String URL_REPO = "http://localhost:4502/crx/server";
	private final static String USERNAME = "admin";
	private final static String PASSWORD = "admin";
	private static File zipFile = null;
	private static File configExpectedParams = null;
	private static Session session = null;

//	@BeforeClass
	public static void init() {
		ClassLoader classLoader = TestUploadContent.class.getClassLoader();
		zipFile = new File(classLoader.getResource("exampleTest.zip").getFile());
		configExpectedParams = new File(classLoader.getResource(
				"config_params.xml").getFile());

		try {
			Repository repo = JcrUtils.getRepository(URL_REPO);

			SimpleCredentials creds = new SimpleCredentials(USERNAME,
					PASSWORD.toCharArray());
			session = repo.login(creds, "crx.default");

		} catch (RepositoryException e) {
			System.out.println(e.getMessage());
		}

	}

	/**
	 * Test creation content by filling out a POST to upload component
	 */
//	@Test
	public void createContentByFillingOutForm() {

		Config config = new Config();
		JSONObject jsonResult = null;

		try {

			config.setSrc("/var/aem-importer/importTest1");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/pando");
			config.setTransformer("com.adobe.aem.importer.impl.DITATransformerXSLTImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/dita-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME,
					PASSWORD, config, null);

			URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
					+ jsonResult.get("configPath")).openConnection();
			String userpass = USERNAME + ":" + PASSWORD;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			urlConnection.setRequestProperty("Authorization", basicAuth);

			Properties properties = new Properties();
			properties.loadFromXML(urlConnection.getInputStream());

			assertTrue(checkConfigFile(config, properties));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}

//	 @Test
	public void createContentByZipFile() {
		JSONObject jsonResult = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			Properties expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFile);

			URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
					+ jsonResult.get("configPath")).openConnection();
			String userpass = USERNAME + ":" + PASSWORD;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			urlConnection.setRequestProperty("Authorization", basicAuth);

			Properties properties = new Properties();
			properties.loadFromXML(urlConnection.getInputStream());

			assertEquals(expectedProperties, properties);
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

//	 @Test
	public void createContentByFillingOutFormAndZipFile() {
		JSONObject jsonResult = null;
		try {

			FileInputStream fis = new FileInputStream(configExpectedParams);
			Properties expectedProperties = new Properties();
			expectedProperties.loadFromXML(fis);

			Config config = new Config();

			config.setSrc("/var/aem-importer/importTest2");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/pando");
			config.setTransformer("com.adobe.aem.importer.impl.DITATransformerXSLTImpl");
			config.setCustomProps("xslt-transformer=net.sf.saxon.TransformerFactoryImpl\r\nxslt-file=/apps/aem-importer/resources/dita-to-content.xsl\r\ntempFolder=/var/aem-importer/tmp\r\npackageTpl=/apps/aem-importer/resources/package-tpl\r\ngraphicFolders=images,graphics,Graphics");

			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					config, zipFile);

			URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
					+ jsonResult.get("configPath")).openConnection();
			String userpass = USERNAME + ":" + PASSWORD;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			urlConnection.setRequestProperty("Authorization", basicAuth);

			Properties properties = new Properties();
			properties.loadFromXML(urlConnection.getInputStream());

			assertEquals(expectedProperties, properties);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	private Boolean checkConfigFile(Config config, Properties properties) {
		if (config.getSrc().equals(properties.get("src"))
				&& config.getMasterFile().equals(properties.get("masterFile"))
				&& config.getTarget().equals(properties.get("target"))
				&& config.getTransformer().equals(
						properties.get("dita-transformer"))) {
			return true;
		}
		return false;
	}
}
