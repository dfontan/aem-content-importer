package com.adobe.aem.importer.test;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adobe.aem.importer.test.utils.HttpClientUtils;
import com.adobe.aem.importer.xml.Config;

public class TestUploadContent {
	
	private final String URL = "http://localhost:4502/content/resources/help/en/upload-content/_jcr_content";
	private final String USERNAME = "admin";
	private final String PASSWORD = "admin";
	private File zipFile = null;
	
	//@BeforeClass
	public void init() {
		ClassLoader classLoader = getClass().getClassLoader();
		zipFile = new File(classLoader.getResource("example.zip").getFile());
	}
	
	/**
	 * Test creation content by filling out a POST to upload component
	 */
	//@Test
	public void createContentByFillingOutForm() {
		
		Config config = new Config();
		
		config.setSrc("/var/aem-importer/import7");
		config.setMasterFile("mcloud.ditamap");
		config.setTarget("/content/pando");
		config.setTransformer("com.adobe.aem.importer.impl.DITATransformerXSLTImpl");
		
		try {
			assertFalse(HttpClientUtils.post(URL, USERNAME, PASSWORD, config, null));
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	//@Test
	public void createContentByZipFile() {
		try {
			assertFalse(HttpClientUtils.post(URL, USERNAME, PASSWORD, null, zipFile));
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	//@Test
	public void createContentByFillingOutFormAndZipFile() {
		try {
			
			Config config = new Config();
			
			config.setSrc("/var/aem-importer/import7");
			config.setMasterFile("mcloud.ditamap");
			config.setTarget("/content/pando");
			config.setTransformer("com.adobe.aem.importer.impl.DITATransformerXSLTImpl");
			
			assertFalse(HttpClientUtils.post(URL, USERNAME, PASSWORD, config, zipFile));
		} catch (Exception e) {
			assertTrue(false);
		}
	}

}
