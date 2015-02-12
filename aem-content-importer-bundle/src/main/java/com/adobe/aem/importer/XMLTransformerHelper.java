/*******************************************************************************
 * Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
 *
 * Licensed under the Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0
 ******************************************************************************/

package com.adobe.aem.importer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class XMLTransformerHelper {

    public static final String CONFIG_PARAM_SRC = "src";
    public static final String CONFIG_PARAM_TARGET = "target";
    public static final String CONFIG_PARAM_TRANSFORMER = "xml-transformer";
    public static final String CONFIG_PARAM_MASTER_FILE = "masterFile";
    public static final String DEFAULT_CONFIG_PARAM_SRC = "/var/aem-importer/import";


    private static final Logger log = LoggerFactory.getLogger(XMLTransformerHelper.class);


    @Reference(referenceInterface = XMLTransformer.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private static Map<Class<?>, XMLTransformer> availableTransformers = new HashMap<Class<?>, XMLTransformer>();


    /**
     * Get a set collection of available transformers
     *
     * @param slingScriptHelper
     * @return list of available transformers (class name)
     */
    public static Set<Class<?>> getAvailableTransformers() {
        return XMLTransformerHelper.availableTransformers.keySet();
    }

    /**
     * Get XMLTransformer from the className
     *
     * @param className (qualified name)
     * @return the XML transformer service
     * @throws Exception
     */
    public static XMLTransformer getXMLTransformer(String className) throws Exception {
        XMLTransformer xmlTransformer = XMLTransformerHelper.availableTransformers.get(Class.forName(className));
        if (xmlTransformer == null)
            throw new Exception("Transformer Class " + className + " not found");
        return xmlTransformer;
    }


    /**
     * Trace new registered transformer
     *
     * @param xmlTransformer
     */
    protected void bindAvailableTransformers(final XMLTransformer xmlTransformer) {
        log.debug("Bind transformer " + xmlTransformer.getClass().toString());
        synchronized (XMLTransformerHelper.availableTransformers) {
            XMLTransformerHelper.availableTransformers.put(xmlTransformer.getClass(), xmlTransformer);
        }
    }

    /**
     * Trace removed transformer
     *
     * @param xmlTransformer
     */
    protected void unbindAvailableTransformers(final XMLTransformer xmlTransformer) {
        log.debug("Unbind transformer " + xmlTransformer.getClass().toString());
        synchronized (XMLTransformerHelper.availableTransformers) {
            XMLTransformerHelper.availableTransformers.remove(xmlTransformer.getClass());
        }
    }
}
