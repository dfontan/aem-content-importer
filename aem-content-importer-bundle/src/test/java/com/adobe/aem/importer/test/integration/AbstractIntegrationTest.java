package com.adobe.aem.importer.test.integration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.sling.commons.json.JSONObject;

import com.adobe.aem.importer.xml.Config;
import com.adobe.aem.importer.xml.utils.ZipHelper;

public abstract class AbstractIntegrationTest {
	
	protected final static String PATH_NODE = "/content/aem-importer-test/upload-content";
	protected final static String SLING_RESOURCETYPE = "aem-importer/components/upload-content";
	protected final String POST_URL = "http://localhost:4502" + PATH_NODE + "/_jcr_content";
	protected final String CONFIG_PARAM_SERVER = "http://localhost:4502";
	protected final static String URL_REPO = "http://localhost:4502/crx/server";
	protected final static String USERNAME = "admin";
	protected final static String PASSWORD = "admin";
	protected final String CONFIG_PATH_RESULT = "configPathResult";
	protected final Long MILLISECONDS = 2000L;
	protected final String TARGET_PROP = "target";
	
	protected Properties createExpectedProperties(Config config) {
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
	
	protected Boolean checkProperties(Properties expectedProperties, Properties properties) {
		
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
	
	protected JSONObject retrieveNodeInfoFromJCR(String path) throws Exception {
		URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
				+ path).openConnection();
		String userpass = USERNAME + ":" + PASSWORD;
		String basicAuth = "Basic "
				+ new String(new Base64().encode(userpass.getBytes()));
		urlConnection.setRequestProperty("Authorization", basicAuth);
		
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8")); 
	    StringBuilder responseStrBuilder = new StringBuilder();

	    String inputStr;
	    while ((inputStr = streamReader.readLine()) != null)
	        responseStrBuilder.append(inputStr);
	    
	    return new JSONObject(responseStrBuilder.toString());
		
		
	}
	
	protected Properties retrieveConfigPropertiesFromJCR(JSONObject jsonResult) throws Exception {
		URLConnection urlConnection = new URL(CONFIG_PARAM_SERVER
				+ jsonResult.get(CONFIG_PATH_RESULT)).openConnection();
		String userpass = USERNAME + ":" + PASSWORD;
		String basicAuth = "Basic "
				+ new String(new Base64().encode(userpass.getBytes()));
		urlConnection.setRequestProperty("Authorization", basicAuth);
		
		Properties properties = new Properties();
		properties.loadFromXML(urlConnection.getInputStream());
		
		return properties;
	}
	
	protected static File createZipFileWithFolder(String srcPath, String nameFile, String nameZipFolder) throws Exception {
		ZipOutputStream zip = null;
		FileOutputStream fW = null;
		fW = new FileOutputStream(nameFile);
		zip = new ZipOutputStream(fW);
		
		File pathToRead = new File(srcPath);
		
		
		String subfolders[] = nameZipFolder.split("/");
		
		String folderName = "";
		for(String subfolder : subfolders) {
			folderName += subfolder + "/";
			ZipEntry zipEntry = new ZipEntry(folderName);
			
			zip.putNextEntry(zipEntry);
		}
		
		
		for (File f : pathToRead.listFiles()) {
			ZipHelper.addFileToZip(nameZipFolder, f.getAbsolutePath(), zip, false);
		}
		
		zip.close();
		
		File zipFile = new File(nameFile);
		
		return zipFile;
	}
}
