/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer.process / TransfomerWorkflowProcess.java 
 * Dec 11, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer.process;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;

public class TransfomerWorkflowProcess implements WorkflowProcess {

	/* (non-Javadoc)
	 * @see com.day.cq.workflow.exec.WorkflowProcess#execute(com.day.cq.workflow.exec.WorkItem, com.day.cq.workflow.WorkflowSession, com.day.cq.workflow.metadata.MetaDataMap)
	 */
	@Override
	public void execute(WorkItem item, WorkflowSession wSession, MetaDataMap meta) throws WorkflowException {
		WorkflowData workflowData = item.getWorkflowData();
    if (workflowData.getPayloadType().equals("JCR_PATH")) {
        String path = workflowData.getPayload().toString() + "/jcr:content";
        try {
            Node node = (Node) wSession.getSession().getItem(path);
            if (node != null) {
                node.setProperty("executed", Calendar.getInstance());
                wSession.getSession().save();
            }
        } catch (RepositoryException e) {
            throw new WorkflowException(e.getMessage(), e);
        }
    }
	}

	
}
