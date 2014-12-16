/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer.process / TransfomerWorkflowProcess.java 
 * Dec 11, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer.process;

import java.util.Calendar;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component
@Service
public class TransformerWorkflowProcess implements WorkflowProcess  {
	
	private static final Logger log = LoggerFactory.getLogger(TransformerWorkflowProcess.class);
	
	@Property(value = "Transformer content workflow.")
    static final String DESCRIPTION = Constants.SERVICE_DESCRIPTION;
    @Property(value = "Adobe")
    static final String VENDOR = Constants.SERVICE_VENDOR;
    @Property(value = "Transformer Workflow Process")
    static final String LABEL="process.label";
	
	@Override
	public void execute(WorkItem item, WorkflowSession wSession, MetaDataMap meta) throws WorkflowException {
		WorkflowData workflowData = item.getWorkflowData();
    if (workflowData.getPayloadType().equals("JCR_PATH")) {
        String path = workflowData.getPayload().toString() + "/jcr:content";
        
        try {
        	Session session = wSession.adaptTo(Session.class);
            Node node = (Node) session.getItem(path);
            if (node != null) {
                node.setProperty("executed", Calendar.getInstance());
                session.save();
            }
        } catch (RepositoryException e) {
            throw new WorkflowException(e.getMessage(), e);
        }
    }
	}

	
}
