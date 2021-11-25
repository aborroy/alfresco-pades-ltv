package org.alfresco.sdk.filter;


import java.util.List;
import java.util.Objects;
import org.alfresco.event.sdk.handling.filter.AbstractEventFilter;
import org.alfresco.event.sdk.model.v1.model.DataAttributes;
import org.alfresco.event.sdk.model.v1.model.NodeResource;
import org.alfresco.event.sdk.model.v1.model.RepoEvent;
import org.alfresco.event.sdk.model.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EventFilter that checks if an event makes reference to a descendant of a specific node
 */
public class ParentNodeFilter extends AbstractEventFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParentNodeFilter.class);

    private final String parentNodeId;

    private ParentNodeFilter(final String parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    /**
     * Obtain a ParentNodeFilter for a specific parent node id
     */
    public static ParentNodeFilter of(final String parentNodeId) {
        Objects.requireNonNull(parentNodeId);
        return new ParentNodeFilter(parentNodeId);
    }

    @Override
    public boolean test(final RepoEvent<DataAttributes<Resource>> event) {
        final List<String> primaryHierarchy = ((NodeResource)event.getData().getResource()).getPrimaryHierarchy();
        return primaryHierarchy != null && primaryHierarchy.contains(parentNodeId);
    }
}
