/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer.test.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;






import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adobe.aem.importer.exception.ImporterException.AEM_IMPORTER_EXCEPTION_TYPE;
import com.adobe.aem.importer.test.utils.HttpClientUtils;

public class ValidationIntegrationTest extends AbstractIntegrationTest {
	private static File zipFileWrongStructure = null;
	private static Session session = null;

	@BeforeClass
	public static void init() {
		try {
			ClassLoader classLoader = DITAIntegrationTest.class.getClassLoader();
			zipFileWrongStructure = createZipFileWithFolder(classLoader.getResource("ditaExamples/mcloud").getFile(), System.currentTimeMillis() + ".zip", "exampleDITA/example");

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

	/*
	 * Test for checking an exception when zip file has an invalid structure
	 */
	@Test
	public void testZipWithWrongStructure() {
		JSONObject jsonResult = null;
		try {
			jsonResult = HttpClientUtils.post(POST_URL, USERNAME, PASSWORD,
					null, zipFileWrongStructure);
			Assert.assertEquals(AEM_IMPORTER_EXCEPTION_TYPE.INVALID_ZIP_FILE.name(), (String)jsonResult.get("errorType"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			assertTrue(false);
		}
	}

	@AfterClass
	public static void finish() {
		if (zipFileWrongStructure != null) {
			zipFileWrongStructure.delete();
		}

	}




}
