/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer;

import java.util.Properties;

import javax.jcr.Node;

import com.adobe.aem.importer.exception.DocImporterException;

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
    final static String PACKAGE_TEMPLATE_PATH = "/apps/aem-importer/resources/package-tpl";

    // Temporary repository locations
    final static String ROOT_TEMP_PATH = "/var/doc-importer";
    final static String OUTPUT_PACKAGE_FOLDER = "tmp";
    final static String OUTPUT_PACKAGE_PATH = ROOT_TEMP_PATH + "/" + OUTPUT_PACKAGE_FOLDER;
    final static String SOURCE_DOC_FOLDER = "src";
    final static String SOURCE_DOC_PATH = ROOT_TEMP_PATH + "/" + SOURCE_DOC_FOLDER;

    /**
	 * Initialize and execute import of content
	 * @throws Exception
	 */
	public void doImport() throws DocImporterException;
}
