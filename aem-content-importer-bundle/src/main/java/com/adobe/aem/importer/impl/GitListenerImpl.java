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
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import com.adobe.granite.codesharing.github.GitHubPushConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.InflaterInputStream;


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

    private static final String DOC_IMPORTER_USER = "doc-importer-user";

    @Reference
    private SlingRepository repository;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private DocImporter docImporter;


    public void handleEvent(final Event osgiEvent) {
        Session session;
        ResourceResolver resourceResolver;

        Project project = (Project)osgiEvent.getProperty(GitHubPushConstants.EVT_PROJECT);
        GitHubPushEvent event = (GitHubPushEvent)osgiEvent.getProperty(GitHubPushConstants.EVT_GHEVENT);

        List<String> changed = event.getModifiedFileNames();
        changed.addAll(event.getAddedFileNames());

        List<String> deleted = event.getDeletedFileNames();

        try {
            session = repository.loginService(DOC_IMPORTER_USER, null);

            for (String path : changed){
                File gitFile = project.getFile(path);

                String[] split = path.split("/");
                String fileName = split[split.length - 1];
                String parentPath = DocImporter.ROOT_TEMP_PATH + "/" + path.substring(0, path.lastIndexOf("/"));

                Node parentNode = JcrUtils.getOrCreateByPath(parentPath,"nt:folder", "nt:folder", session, true);
                InputStream in = IOUtils.toInputStream(gitFile.getContent(), "UTF-8");
                JcrUtils.putFile(parentNode, fileName, "application/xml", in);
            }

            for (String path : deleted){
                session.getNode(DocImporter.ROOT_TEMP_PATH + "/" + path).remove();
                session.save();
            }

            // docImporter.doImport(session.getNode(DocImporter.DEFAULT_SOURCE_PATH, properties);

        } catch (Exception e){
            log.error("error", e);
        }
    }
}
