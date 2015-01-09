/*******************************************************************************
 * Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
 *
 * Licensed under the Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0
 ******************************************************************************/

package com.adobe.aem.importer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.xml.Config;

public class Command {

	private static final String SRC_OPTION = "-src";
	private static final String TARGET_OPTION = "-target";
	private static final String TRANSFORMER_OPTION = "-transformer";
	private static final String MASTER_FILE_OPTION = "-masterFile";
	private static final String CUSTOM_PROPS_OPTION = "-customProps";
	private static final String SOURCES_OPTION = "-sources";

	private static final String PATH_NODE = "/content/resources/importer-tool/jcr:content/upload-content";
	private static final String POST_URL = "http://localhost:4502" + PATH_NODE;
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "admin";
	
	private static Logger log = LoggerFactory.getLogger(Command.class);

	public static void main(String args[]) {

		Config config = new Config();
		Map<String, String> params = processCommandParams(args, config);

		try {
			if (params.isEmpty()) {
				log.info("Command usage:");
				log.info("-src <value> => Source folder in AEM where are located the all necessary files to generate content");
				log.info("-target <value> => Target folder in AEM where it's going to be allocated generated content");
				log.info("-transformer <value> => Sort of transformer to apply using files inside src folder");
				log.info("-masterFile <value> => The root file to start transformation if it's needed");
				log.info("-customProps <value> => Custom properties to add to the configuration process. Ex: xslt-transformer=net.sf.saxon.TransformerFactoryImpl#xslt-file=/apps/aem-importer/resources/dita-to-content.xsl");
				log.info("-sources <value> => A zip file or local folder with all source files for making the transformation");

				return;
			}
			
			String error = "";

			if (params.containsKey(SOURCES_OPTION)) {

				File sources = new File(params.get(SOURCES_OPTION));
				String zipName = "";
				if (!sources.exists()) {
					log.info("Source path indicated doesn't exist");
					return;
				}

				log.info("Sending sources to AEM...");
				if (sources.isDirectory()) {

					ZipHelper zipHelper = new ZipHelper();
					zipName = System.currentTimeMillis() + ".zip";
					zipHelper.zipDir(params.get(SOURCES_OPTION), zipName);
					
					sources = new File(zipName);

				}

				JSONObject result = HttpClientUtils.post(POST_URL, USERNAME,
						PASSWORD, null, sources);

				error = result.getString("error");
				
				if (zipName.length() > 0) {
					sources.delete();
				}

				

			} else {
				log.info("Sending configuration parameters AEM...");
				JSONObject result = HttpClientUtils.post(POST_URL, USERNAME,
							PASSWORD, config, null);

				error = result.getString("error");
			}
			
			if ("false".equalsIgnoreCase(error)) {
				log.info("Sent it the information correctly. Workflow is about to lauch.");
			} else {
				log.info("Process failed! Check source files");
				
			}
			
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}

	}

	private static Map<String, String> processCommandParams(String args[],
			Config config) {
		HashMap<String, String> params = new HashMap<String, String>();

		int i = 0;
		String arg = "";
		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			if (SRC_OPTION.equalsIgnoreCase(arg)) {
				params.put(SRC_OPTION, args[i]);
				config.setSrc(args[i]);
			}

			if (TARGET_OPTION.equalsIgnoreCase(arg)) {
				params.put(TARGET_OPTION, args[i]);
				config.setTarget(args[i]);
			}

			if (TRANSFORMER_OPTION.equalsIgnoreCase(arg)) {
				params.put(TRANSFORMER_OPTION, args[i]);
				config.setTransformer(args[i]);
			}

			if (MASTER_FILE_OPTION.equalsIgnoreCase(arg)) {
				params.put(MASTER_FILE_OPTION, args[i]);
				config.setMasterFile(args[i]);
			}

			if (CUSTOM_PROPS_OPTION.equalsIgnoreCase(arg)) {
				params.put(CUSTOM_PROPS_OPTION, args[i]);
				config.setCustomProps(args[i]);
			}

			if (SOURCES_OPTION.equalsIgnoreCase(arg)) {
				params.put(SOURCES_OPTION, args[i]);
			}
			
			i++;
		}

		return params;
	}

}
