package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.CounterItem;

import java.util.List;

/**
 * TreeItem builder that produces CounterItem TreeItem instances.
 */
public class CounterItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    @Override
    protected List<CounterItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {
        return List.of(new CounterItem(context, item.getRepository()));
    }
}