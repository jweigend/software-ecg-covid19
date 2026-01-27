package de.qaware.ekg.awb.metricanalyzer.ui.api;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

import java.util.Objects;

/**
 * A bean that define the context with different kind of filter
 * that relating to the series data or the view flavor the user
 * has selected.
 */
public class FilterContext {

    /**
     * A query param instance that stores all filters that are used
     * to define a branch in the tree.
     * Example: a filter set with project X, host Y and process Z but * on measurement and metric groups
     * to define nodes displayed as child of a process Z that is a child of host Y that belongs to project X.
     */
    private QueryFilterParams queryParams;

    /**
     * The project view flavor that is currently in use (logical view vs. physical view)
     * Depending on the flavor items will rendered different or removed/added.
     */
    private ProjectViewFlavor activeViewFlavor;

    /**
     * Constructs a new instance of FilterContext
     *
     * @param queryParams the filter parameters that are active the for tree item
     * @param activeViewFlavor the active view flavor of the node
     */
    public FilterContext(QueryFilterParams queryParams, ProjectViewFlavor activeViewFlavor) {
        this.queryParams = queryParams;
        this.activeViewFlavor = activeViewFlavor;
    }

    /**
     * Returns the filter context that all child nodes
     * has to consider.
     *
     * @return the context as QueryFilterParams instance that can use for filter queries and more
     */
    public QueryFilterParams getQueryParameter() {
        return queryParams;
    }

    /**
     * Returns the active view flavor of the node
     *
     * @return the view flavor that is currently in use
     */
    public ProjectViewFlavor getActiveViewFlavor() {
        return activeViewFlavor;
    }

    /**
     * Clones and returns the FilterContext instance and set the given
     * query parameters to the cloned instance.
     *
     * @param newQueryParams the QueryFilterParams that should used for the cloned context.
     * @return the cloned context
     */
    public FilterContext cloneWith(QueryFilterParams newQueryParams) {
        return new FilterContext(newQueryParams, activeViewFlavor);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterContext context = (FilterContext) o;
        return Objects.equals(queryParams, context.queryParams) &&
                activeViewFlavor == context.activeViewFlavor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryParams, activeViewFlavor);
    }
}
