package de.qaware.ekg.awb.metricanalyzer.ui.api;

import de.qaware.ekg.awb.explorer.ui.TreeIconsProviderRegistry;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import org.apache.commons.lang3.StringUtils;

/**
 * The base tree item class of all time series related tree items that
 * defines the context like the concrete project and additional filters
 * a series items belongs to.
 */
public abstract class ContextAwareItem extends RepositoryBaseItem {

    private FilterContext filterContext;

    protected TreeIconsProviderRegistry iconsProviderRegistry = EkgLookup.lookup(TreeIconsProviderRegistry.class);

    /**
     * Creates a new ContextAwareItem item based on the given context and the project
     *
     * @param label the name of the tree item that will used as displayed label in the view
     * @param context bean that define filters that relating to series data or the view flavor and control the item rendering
     * @param repository the containing types.
     */
    public ContextAwareItem(String label, FilterContext context, EkgRepository repository) {
        super(label, repository);
        this.filterContext = context;
    }

    /**
     * Returns the context with different kind of filter that relating to the
     * series data or the view flavor the user has selected.
     *
     * @return the context bean with the filter and post compute settings
     */
    public FilterContext getFilterContext() {
        return filterContext;
    }

    /**
     * Returns the project this explorer tree item belongs to.
     * This project is the context for all sub nodes in the tree.
     *
     * @return the project that is the data base of all sub nodes
     */
    public Project getProject() {
        return getProject(filterContext);
    }

    /**
     * Helper method that extract the project from the given filter
     * context to write less clue code in deriving classes.
     *
     * @param context the context bean with the filter (incl. the project) and post compute settings
     * @return the project extract from the context or null if no project is inside (should never be the case)
     */
    public static Project getProject(FilterContext context) {
        return context.getQueryParameter().getProject();
    }

    /**
     * Returns the MetricTreeIconsProvider that will server icons series specific tree nodes
     * and the default tree nodes like the project.
     *
     * @return the MetricTreeIconsProvider that serves the correct icons for this branch of the explorer tree
     */
    protected MetricTreeIconsProvider getIconProvider() {

        if (filterContext == null || filterContext.getQueryParameter() == null) {
            return MetricTreeIconsProvider.DEFAULT;
        }

        Project project = filterContext.getQueryParameter().getProject();

        if (project == null || StringUtils.isBlank(project.getImporterId())) {
            return MetricTreeIconsProvider.DEFAULT;
        }

        MetricTreeIconsProvider provider = (MetricTreeIconsProvider)
                iconsProviderRegistry.getTreeIconsProvider(project.getImporterId());

        return provider == null ? MetricTreeIconsProvider.DEFAULT : provider;
    }
}