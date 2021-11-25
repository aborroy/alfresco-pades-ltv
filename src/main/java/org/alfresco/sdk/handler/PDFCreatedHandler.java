package org.alfresco.sdk.handler;

import org.alfresco.core.handler.NodesApi;
import org.alfresco.core.model.NodeEntry;
import org.alfresco.event.sdk.handling.filter.EventFilter;
import org.alfresco.event.sdk.handling.filter.IsFileFilter;
import org.alfresco.event.sdk.handling.filter.MimeTypeFilter;
import org.alfresco.event.sdk.handling.handler.OnNodeCreatedEventHandler;
import org.alfresco.event.sdk.model.v1.model.DataAttributes;
import org.alfresco.event.sdk.model.v1.model.NodeResource;
import org.alfresco.event.sdk.model.v1.model.RepoEvent;
import org.alfresco.event.sdk.model.v1.model.Resource;
import org.alfresco.sdk.filter.ParentNodeFilter;
import org.alfresco.sdk.pades.PadesLTVService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

/**
 * Event Handler listening to a folder in the repository for PDF files creation.
 * For every PDF, timestamp (TS) is calculated using Pades-LTV format.
 * Original PDF file is updated in the repository with a new major version of the document including that TS.
 */
@Component
public class PDFCreatedHandler implements OnNodeCreatedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDFCreatedHandler.class);

    @Autowired
    NodesApi nodesApi;

    @Autowired
    PadesLTVService padesLtvService;

    @Value("${app.watch.folder}")
    private String watchFolderPath;
    private String watchFolderNodeId;

    /**
     * Get Node ID for the specified watch folder once
     */
    @PostConstruct
    public void init() {
        ResponseEntity<NodeEntry> responseEntity = nodesApi.getNode("-root-", null, watchFolderPath, Collections.singletonList("id"));
        watchFolderNodeId =  responseEntity.getBody().getEntry().getId();
    }

    /**
     *  Only filtered events (PDF files created in watch folder) will run this method
     */
    @Override
    public void handleEvent(RepoEvent<DataAttributes<Resource>> repoEvent) {

        final NodeResource nodeResource = (NodeResource) repoEvent.getData().getResource();

        LOGGER.info("A PDF content named {} has been created!", nodeResource.getName());

        try {

            LOGGER.debug("Retrieving content from Alfresco node {}", nodeResource.getName());

            InputStream input = nodesApi.getNodeContent(nodeResource.getId(), true, null, null)
                .getBody()
                .getInputStream();

            nodesApi.updateNodeContent(nodeResource.getId(), padesLtvService.getPadesLTV(input), true, "PadesLTV", null, null, null);

            LOGGER.debug("PadesLTV file has been uploaded to Alfresco Repository");

        } catch (Exception ex) {
            LOGGER.error("An error occurred trying to create PadesLTV from the PDF", ex);
        }

    }

    /**
     * Get only events for files created on watch folder with mimetype PDF
     */
    @Override
    public EventFilter getEventFilter() {
        return IsFileFilter.get()
            .and(ParentNodeFilter.of(watchFolderNodeId))
            .and(MimeTypeFilter.of("application/pdf"));
    }
}
