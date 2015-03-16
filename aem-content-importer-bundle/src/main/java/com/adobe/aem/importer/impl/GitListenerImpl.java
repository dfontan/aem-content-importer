/*******************************************************************************
 * Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
 *
 * Licensed under the Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0
 ******************************************************************************/
package com.adobe.aem.importer.impl;

import com.adobe.aem.importer.DocImporter;
import com.adobe.aem.importer.GitListener;
import com.adobe.granite.codesharing.File;
import com.adobe.granite.codesharing.Project;
import com.adobe.granite.codesharing.github.GitHubPushEvent;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.webdav.util.EncodeUtil;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import com.adobe.granite.codesharing.github.GitHubPushConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component(
    label = "Document Importer Git Listener",
    description = "Listens for push events on linked Git repository and imports DITA or DocBook documentation changes",

    //Event Listener starts listening immediately
    immediate = true
)
@Properties({
    @Property(
        label = "Event Topics",
        value = {GitHubPushConstants.EVT_TOPIC},
        description = "This event handler responds to Git Push Events.",
        name = EventConstants.EVENT_TOPIC,
        propertyPrivate = true
    )
})
@Service
public class GitListenerImpl implements GitListener {

    private static Logger log = LoggerFactory.getLogger(GitListenerImpl.class);

    private static final String DOC_IMPORTER = "doc-importer";

    @Reference
    private SlingRepository repository;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private DocImporter docImporter;


    public void handleEvent(final Event osgiEvent) {
        Session session;

        Project project = (Project)osgiEvent.getProperty(GitHubPushConstants.EVT_PROJECT);
        GitHubPushEvent gitHubPushEvent = (GitHubPushEvent)osgiEvent.getProperty(GitHubPushConstants.EVT_GHEVENT);

        List<String> added = gitHubPushEvent.getAddedFileNames();
        log.info("added files: " + Arrays.toString(added.toArray()));

        List<String> modified = gitHubPushEvent.getModifiedFileNames();
        log.info("modified files: " + Arrays.toString(modified.toArray()));

        List<String> deleted = gitHubPushEvent.getDeletedFileNames();
        log.info("deleted files: " + Arrays.toString(deleted.toArray()));

        modified.addAll(added);
        log.info("added or modified files: " + Arrays.toString(modified.toArray()));

        try {
            // Use loginAdministrative until in-content principal config is figured out
            session = repository.loginAdministrative(null);
            //session = repository.loginService(DOC_IMPORTER, null);


            for (String path : modified){
                log.info("added or modified...");
                log.info("git file path: " + path);

                String escapedPath = EncodeUtil.escapePath(path);
                log.info("escaped git file path: " + escapedPath);

                File gitFile = project.getFile(escapedPath);
                String gitFileContent = gitFile.getContent();

                String[] split = path.split("/");
                log.info("split: " + Arrays.toString(split));

                String fileName = split[split.length - 1];
                log.info("git file name: " + fileName);

                int lastForwardSlashPos = path.lastIndexOf("/");
                log.info("last forward slash pos: " + lastForwardSlashPos);

                String parentPath = DocImporter.ROOT_TEMP_PATH;
                if (lastForwardSlashPos > 0) {
                    parentPath = parentPath + "/" + path.substring(0, lastForwardSlashPos);
                }
                log.info("parent path: " + parentPath);

                Node parentNode = JcrUtils.getOrCreateByPath(parentPath,"nt:folder", "nt:folder", session, true);
                log.info("parent node: " + parentNode.toString());

                InputStream in = IOUtils.toInputStream(gitFileContent, "UTF-8");
                Node node = JcrUtils.putFile(parentNode, fileName, "application/xml", in);
                session.save();
                log.info("file: " + path + " written to node:" + node.getPath());
            }

            for (String path : deleted){
                log.info("deleted...");
                log.info("git file path: " + path);

                String deleteNodePath = DocImporter.ROOT_TEMP_PATH + "/" + path;
                if (session.nodeExists(deleteNodePath)){
                    session.getNode(deleteNodePath).remove();
                    session.save();
                    log.info("removal of file: " + path + "caused removal of node: " + deleteNodePath);
                }
                log.info("removal of file: " + path + "caused no change. No node found at: " + deleteNodePath);
            }

            docImporter.doImport();

        } catch (Exception e){
            log.error("error", e);
        }
    }
}
