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
        log.info("Added files: " + Arrays.toString(added.toArray()));

        List<String> modified = gitHubPushEvent.getModifiedFileNames();
        log.info("Modified files: " + Arrays.toString(modified.toArray()));

        List<String> deleted = gitHubPushEvent.getDeletedFileNames();
        log.info("Deleted files: " + Arrays.toString(deleted.toArray()));

        modified.addAll(added);
        log.info("Added or modified files: " + Arrays.toString(modified.toArray()));

        try {
            // Use loginAdministrative until in-content principal config is figured out
            session = repository.loginAdministrative(null);
            //session = repository.loginService(DOC_IMPORTER, null);


            for (String path : modified){
                log.info("Changed...");
                log.info("git file path: " + path);

                File gitFile = project.getFile(path);
                String gitFileContent = gitFile.getContent();
                log.info("git file content: \n" + gitFileContent);

                String[] split = path.split("/");
                log.info("split: " + Arrays.toString(split));

                String fileName = split[split.length - 1];
                log.info("git file name: " + fileName);

                int lastForwardSlashPos = path.lastIndexOf("/");
                log.info("lastForwardSlashPos: " + lastForwardSlashPos);

                String parentPath = DocImporter.ROOT_TEMP_PATH;
                if (lastForwardSlashPos > 0) {
                    parentPath = parentPath + "/" + path.substring(0, lastForwardSlashPos);
                }
                log.info("parentPath: " + parentPath);

                Node parentNode = JcrUtils.getOrCreateByPath(parentPath,"nt:folder", "nt:folder", session, true);
                log.info("parentNode: " + parentNode.toString());

                InputStream in = IOUtils.toInputStream(gitFileContent, "UTF-8");
                JcrUtils.putFile(parentNode, fileName, "application/xml", in);
                session.save();
            }

            for (String path : deleted){
                log.info("Deleted...");
                log.info("git file path: " + path);

                session.getNode(DocImporter.ROOT_TEMP_PATH + "/" + path).remove();
                session.save();
            }

            /*
            Node sourcePathNode = parentNode.getNode(DocImporter.SOURCE_DOC_FOLDER);
            Node configNode = sourcePathNode.getNode(DocImporter.CONFIG_FILE_NAME + "/jcr:content");
            java.util.Properties properties = new java.util.Properties();
            properties.loadFromXML(JcrUtils.readFile(configNode));
            docImporter.doImport(session.getNode(DocImporter.SOURCE_DOC_PATH, properties);
            */

        } catch (Exception e){
            log.error("error", e);
        }
    }
}
