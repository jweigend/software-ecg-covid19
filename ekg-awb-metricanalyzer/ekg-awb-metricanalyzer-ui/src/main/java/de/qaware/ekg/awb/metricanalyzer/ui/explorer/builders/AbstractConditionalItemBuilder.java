package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for ContextAware ItemBuilder that only create child items for
 * parents than a (from the derived builders) implemented condition results in TRUE.
 */
public abstract class AbstractConditionalItemBuilder<T extends RepositoryBaseItem> extends AbstractItemBuilder<T> {

    @Override
    public List<? extends ContextAwareItem> apply(T parentItem, ProgressNotifier progressNotifier) {

        FilterContext filterContext = retrieveFilterContext(parentItem);

        if (isValidParentForChilds(parentItem, filterContext)) {
            List<? extends ContextAwareItem> result = getChildItemsFor(parentItem, filterContext, progressNotifier);

            // don't show metrics, which are labeled with N/D (not defined or no data)
            result = result.stream().filter(item -> !item.getValue().equals("N/D")).collect(Collectors.toList());

            progressNotifier.updateProgress(result.size(), result.size());
            return result;
        }

        return Collections.emptyList();
    }

    /**
     * Validates if the TreeItems create by this ItemBuilder are assignable to
     * the given parent item with the specified filters.
     *
     * @param parentItem the TreeItem of the explorer tree that will be the target/parent of the child if valid
     * @param queryParams the context with query filter the belongs to the  possible parent node
     *
     * @return true if the TreeItems build by this Builder can assigned to the tested parent TreeItem
     */
    protected abstract boolean isValidParentForChilds(T parentItem, FilterContext queryParams);
}
