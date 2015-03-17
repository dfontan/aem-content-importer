/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer;

/**
 * XML Transformer Interface
 */
public interface DocImporter {

    // Default config file name
    final static String CONFIG_FILE_NAME = "0_config_params.xml";

    // Supported source formats
    final static String SOURCE_FORMAT_DITA = "dita";
    final static String SOURCE_FORMAT_DOCBOOK = "docbook";

    // Config parameters and default values
    final static String CONFIG_PARAM_SOURCE_FORMAT = "sourceFormat";
    final static String DEFAULT_SOURCE_FORMAT = SOURCE_FORMAT_DITA;

    final static String CONFIG_PARAM_MASTER_FILE = "masterFile";
    final static String DEFAULT_MASTER_FILE = "default.ditamap";

    final static String CONFIG_PARAM_GRAPHICS_FOLDER = "graphicsFolder";
    final static String DEFAULT_GRAPHICS_FOLDER = "graphics";

    final static String CONFIG_PARAM_TARGET_PATH = "targetPath";
    final static String DEFAULT_TARGET_PATH = "/content/imported";

    // XSLT locations
    final static String DITA_XSLT_PATH = "/apps/aem-importer/resources/dita-to-content.xsl";
    final static String DOCBOOK_XSLT_PATH = "/apps/aem-importer/resources/docbook-to-content.xsl";
    final static String DEFAULT_XSLT_PATH = DITA_XSLT_PATH;

    // Package template location
    final static String CONTENT_PACKAGE_TEMPLATE_PATH = "/apps/aem-importer/resources/package-tpl";

    // Temporary repository locations
    final static String ROOT_TEMP_PATH = "/var/doc-importer";
    final static String CONTENT_PACKAGE_NAME = "package";
    final static String CONTENT_PACKAGE_PATH = ROOT_TEMP_PATH + "/" + CONTENT_PACKAGE_NAME;
    final static String SOURCE_FOLDER_NAME = "source";
    final static String SOURCE_FOLDER_PATH = ROOT_TEMP_PATH + "/" + SOURCE_FOLDER_NAME;

    /**
	 * Initialize and execute import of content
	 * @throws Exception
	 */
	public void doImport();
}
