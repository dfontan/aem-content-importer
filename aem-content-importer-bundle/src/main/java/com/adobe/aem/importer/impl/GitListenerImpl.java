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
import java.net.URI;
import java.util.List;

@Component(
    label = "Document Importer Git Listener",
    description = "Listens for push events on linked Git repository and imports DITA or DocBook documentation changes",
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

    @Reference
    private SlingRepository repository;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private DocImporter docImporter;

    public void handleEvent(final Event osgiEvent) {
        Project gitProject = (Project)osgiEvent.getProperty(GitHubPushConstants.EVT_PROJECT);
        GitHubPushEvent gitHubPushEvent = (GitHubPushEvent)osgiEvent.getProperty(GitHubPushConstants.EVT_GHEVENT);

        URI gitRepoUrl = gitHubPushEvent.getRepoUrl();
        String sourcePath = DocImporter.GIT_REPOS_FOLDER_PATH + "/" + gitRepoUrl.getHost() + gitRepoUrl.getPath();

        List<String> added = gitHubPushEvent.getAddedFileNames();
        List<String> modified = gitHubPushEvent.getModifiedFileNames();
        List<String> deleted = gitHubPushEvent.getDeletedFileNames();
        modified.addAll(added);

        Session session;
        try {
            session = repository.loginAdministrative(null);
            for (String path : modified){
                String fileName = getFileName(path);
                String parentPath = getParentPath(sourcePath, path);
                String escapedPath = EncodeUtil.escapePath(path);

                long size = gitProject.getFileSize(path);
                log.error("Modified: " + path + " Escaped: " + escapedPath + " Size: " + size);
                if(size < 1000000) {
                    File gitFile = gitProject.getFile(escapedPath);
                    Node parentNode = JcrUtils.getOrCreateByPath(parentPath, "nt:folder", "nt:folder", session, true);
                    InputStream in = IOUtils.toInputStream(gitFile.getContent(), "UTF-8");
                    JcrUtils.putFile(parentNode, fileName, "application/xml", in);
                    session.save();
                } else {
                    log.error("Could not retrieve file " + path + ". Size exceeds GitHUb API limit of 1 MB", path);
                }
            }

            for (String path : deleted){
                String deleteNodePath = sourcePath + "/" + path;
                log.error("Deleted: " + path);

                if (session.nodeExists(deleteNodePath)){
                    session.getNode(deleteNodePath).remove();
                    session.save();
                }
            }
            docImporter.doImport(sourcePath);
        } catch (Exception e){
            log.error("Exception", e);
        }
    }

    private String getParentPath(String sourcePath, String path) {
        int lastForwardSlashPos = path.lastIndexOf("/");

        String parentPath = sourcePath;
        if (lastForwardSlashPos > 0) {
            parentPath = parentPath + "/" + path.substring(0, lastForwardSlashPos);
        }
        return parentPath;
    }

    private String getFileName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
