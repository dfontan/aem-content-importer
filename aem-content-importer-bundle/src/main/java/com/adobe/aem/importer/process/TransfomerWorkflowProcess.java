/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer.process / TransfomerWorkflowProcess.java 
 * Dec 11, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer.process;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.commons.IOUtils;
import org.apache.jackrabbit.oak.commons.StringUtils;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.DITATranformer;
import com.adobe.aem.importer.DITATransformerHelper;
import com.adobe.aem.importer.constant.Constant;
import com.adobe.aem.importer.xml.Config;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;


@Component
@Service
@org.apache.felix.scr.annotations.Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Adobe - DITA Transformer"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Adobe") })
public class TransfomerWorkflowProcess implements WorkflowProcess{

	private static final Logger log = LoggerFactory.getLogger(TransfomerWorkflowProcess.class);
	
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
        	Node node = (Node) wSession.getSession().getItem(path);
          if (node != null) {
          	// Read configuration properties file
          	Properties configFile = new Properties();
      			configFile.loadFromXML(JcrUtils.readFile(node));
          	if (log.isDebugEnabled())
          		debugTraceConfig(configFile);
          	
          	// Check DITA Transformer
          	String ditaTransformerClass = configFile.getProperty(Constant.TRANSFORMER, "");
          	if (ditaTransformerClass.equals(""))
          		throw new WorkflowException("Configuration error: Transformer property cannot be null");
          	
          	// Start Transform process
          	DITATranformer ditaTransformer = DITATransformerHelper.getDITATransformer(ditaTransformerClass);
          	ditaTransformer.initialize(node.getParent().getParent(), configFile);
          	ditaTransformer.execute(configFile.getProperty(Constant.MASTER_FILE), configFile.getProperty(Constant.TARGET, "/"));
          }
        } catch (Exception e) {
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
}
