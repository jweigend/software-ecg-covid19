package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.ProjectItem;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Base class for all ContextAware ItemBuilder.
 * This abstract base class define convenience logic to serve a suitable filter context
 * also if the target TreeItem doesn't define one.
 */
public abstract class AbstractItemBuilder<T extends RepositoryBaseItem> implements
        BiFunction<T, ProgressNotifier, List<? extends AbstractItem>> {

    /**
     * The logger used by the builder to log errors
     */
    private static final Logger LOGGER = EkgLogger.get();

    /* (non-Javadoc)
     * @see java.util.function.BiFunction#apply(T, U)
     * @see de.qaware.ekg.awb.common.ui.explorer.api.ItemBuilderRegistry
     */
    @Override
    public List<? extends ContextAwareItem> apply(T parentItem, ProgressNotifier progressNotifier) {

        List<? extends ContextAwareItem> result =
                getChildItemsFor(parentItem, retrieveFilterContext(parentItem), progressNotifier);
        progressNotifier.updateProgress(result.size(), result.size());

        return result;
    }

    /**
     * Build a list of TreeItem instance of a specific type that is derived from ContextAwareItem.
     * The items will assigned to the given parent item  by the caller of the builder. The implementation
     * should consider the filters given by the QueryFilterParams context at the decision which items to build.
     *
     * @param parentItem the TreeItem that will be parent of the items to build
     * @param parentFilterContext the context that defines filters that restrict which child should be added
     * @param progressNotifier a progress notifier that can use to expose the loading/build state.
     *                         It will set to 100% by the caller.
     *
     * @return a list of TreeItem instances that can add to the specified parent node
     */
    protected abstract List<? extends ContextAwareItem> getChildItemsFor(T parentItem, FilterContext parentFilterContext,
                                                                         ProgressNotifier progressNotifier);

    /**
     * Tries to retrieve the filter context from the target TreeItem
     * and return it. If the given parent does't define a filter context
     * a new one with suitable filter will created.
     *
     * @param parentItem the TreeItem instance that will used to retrieve the context.
     * @return the retrieved or created context.
     */
    protected FilterContext retrieveFilterContext(RepositoryBaseItem parentItem) {

        FilterContext filterContext;

        if (parentItem instanceof ProjectItem) {
            Project project = ((ProjectItem) parentItem).getProject();

            filterContext = new FilterContext(
                new QueryFilterParams.Builder().withProject(project).build(),
                ((ProjectItem) parentItem).getActiveViewFlavor()
            );

        } else if (parentItem instanceof ContextAwareItem) {
            filterContext = ((ContextAwareItem) parentItem).getFilterContext();

        } else if (parentItem instanceof RepositoryItem) {

            filterContext= new FilterContext(
                    new QueryFilterParams.Builder().build(),
                    ProjectViewFlavor.PHYSICAL_VIEW
            );

        } else {
            String errorMsg = "The given parent item of type " + parentItem.getClass().getName()
                    + " isn't supported by this builder!";

            LOGGER.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        return filterContext;
    }
}
