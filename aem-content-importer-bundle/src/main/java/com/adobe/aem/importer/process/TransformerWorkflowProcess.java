/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer.process;

import java.util.Properties;
import java.util.Map.Entry;

import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.exception.AemImporterException;
import com.adobe.aem.importer.xml.utils.Utils;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component
@Service
@org.apache.felix.scr.annotations.Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Adobe - XML Transformer Process"),
	@Property(name = Constants.SERVICE_VENDOR, value = "Adobe") })
public class TransformerWorkflowProcess implements WorkflowProcess  {
	
	private static final Logger log = LoggerFactory.getLogger(TransformerWorkflowProcess.class);
	
	/* (non-Javadoc)
	 * @see com.adobe.granite.workflow.exec.WorkflowProcess#execute(com.adobe.granite.workflow.exec.WorkItem, com.adobe.granite.workflow.WorkflowSession, com.adobe.granite.workflow.metadata.MetaDataMap)
	 */
	@Override
	public void execute(WorkItem item, WorkflowSession wSession, MetaDataMap meta) throws WorkflowException {
		WorkflowData workflowData = item.getWorkflowData();
    if (workflowData.getPayloadType().equals("JCR_PATH")) {
        String path = workflowData.getPayload().toString() + "/jcr:content";
        log.debug("PAYLOAD == "+path);
        try {
        	Session session = wSession.adaptTo(Session.class);
        	Node node = (Node) session.getItem(path);
          if (node != null) {
          	// Read configuration properties file
          	Properties configFile = new Properties();
      			configFile.loadFromXML(JcrUtils.readFile(node));
      			overrideConfiParam(configFile, meta);
          	if (log.isDebugEnabled())
          		debugTraceConfig(configFile);
          	
          	// Check DITA Transformer
          	String xmlTransformerClass = configFile.getProperty(XMLTransformerHelper.CONFIG_PARAM_TRANSFORMER, "");
          	if (xmlTransformerClass.equals(""))
          		throw new WorkflowException("Configuration error: Transformer property cannot be null");
          	
          	// Check Source Node
          	String src = configFile.getProperty(XMLTransformerHelper.CONFIG_PARAM_SRC,"");
          	if (src.trim().equals("") || !session.itemExists(src))
          		throw new WorkflowException("Configuration error: Source folder doesn't exist --> "+src);
          	
          	// Start Transform process
          	XMLTransformerHelper.getXMLTransformer(xmlTransformerClass).transform(session.getNode(src), configFile);;
          }
        } catch (AemImporterException e) {
        	log.error(e.getMessage(),e.getException());
        	throw new WorkflowException(e.getMessage(), e.getException());
        } catch (Exception e) {
        	log.error(e.getMessage(),e);
        	throw new WorkflowException(e.getMessage(), e);
		}
    }
	}
	
	/**
	 * debugTraceConfig
	 * @param configFile
	 */
	private void debugTraceConfig(Properties configFile) {
		log.debug("****** CONFIG FILE *******");
		for(Entry<Object, Object> entry : configFile.entrySet())
			log.debug(entry.getKey().toString()+": "+entry.getValue().toString());
		log.debug("*************************");
	}
	
	/**
	 * overrideConfiParam
	 * @param configProp
	 * @param meta
	 * @return
	 */
	private Properties overrideConfiParam(Properties configProp, MetaDataMap meta) {
		String transformer = meta.get("transformer", "");
		if (!transformer.trim().equals(""))
			configProp.put(XMLTransformerHelper.CONFIG_PARAM_TRANSFORMER, transformer);
		String src = meta.get("src", "");
		if (!src.trim().equals(""))
			configProp.put(XMLTransformerHelper.CONFIG_PARAM_SRC, src);
		String target = meta.get("target", "");
		if (!target.trim().equals(""))
			configProp.put(XMLTransformerHelper.CONFIG_PARAM_TARGET, target);
		String master = meta.get("masterFile", "");
		if (!master.trim().equals(""))
			configProp.put(XMLTransformerHelper.CONFIG_PARAM_MASTER_FILE, master);
		String customProp = meta.get("customProperties","");
		if (!customProp.trim().equals(""))
			Utils.appendCustomProperties(configProp, customProp);
		
		return configProp;
	}

	
}
