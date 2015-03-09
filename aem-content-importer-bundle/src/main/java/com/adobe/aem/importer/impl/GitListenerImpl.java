/*******************************************************************************
 * Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
 *
 * Licensed under the Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0
 ******************************************************************************/
package com.adobe.aem.importer.impl;

import com.adobe.aem.importer.GitListener;
import com.adobe.granite.codesharing.Project;
import com.adobe.granite.codesharing.github.GitHubPushEvent;
import org.apache.felix.scr.annotations.*;
//import org.apache.sling.api.resource.LoginException;
//import org.apache.sling.api.resource.Resource;
//import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import com.adobe.granite.codesharing.github.GitHubPushConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.jcr.Node;
import java.io.IOException;
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

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    public void handleEvent(final Event osgiEvent) {
        Project project = (Project)osgiEvent.getProperty(GitHubPushConstants.EVT_PROJECT);
        GitHubPushEvent event = (GitHubPushEvent)osgiEvent.getProperty(GitHubPushConstants.EVT_GHEVENT);
        //ResourceResolver resourceResolver;

        List<String> modifiedFileNames = event.getModifiedFileNames();
        List<String> addedFileNames = event.getAddedFileNames();
        List<String> deletedFileNames = event.getDeletedFileNames();

        try {
            //resourceResolver = resourceResolverFactory.getResourceResolver(null);

            log.info("Modified File Names");
            for (String fileName : modifiedFileNames){
                log.info(fileName);

                //project.getFile(fileName);
                //Resource resource = resourceResolver.getResource(fileName);
                //Node node = resource.adaptTo(Node.class);
            }

            log.info("Added File Names");
            for (String fileName : addedFileNames){
                log.info(fileName);

                //project.getFile(fileName);
            }

            log.info("Deleted File Names");
            for (String fileName : deletedFileNames){
                log.info(fileName);
                project.getFile(fileName);
            }

    //    } catch (LoginException e){
    //        log.error("Login error", e);
        } catch (IOException e){
            log.error("IO error", e);
        }
    }
}
