package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.ProcessGroupItem;

import java.util.List;

/**
 * TreeItem builder that produces HostGroup TreeItem instances.
 */
public class ProcessGroupItemBuilder<T extends RepositoryBaseItem> extends AbstractConditionalItemBuilder<T> {

    @Override
    protected boolean isValidParentForChilds(T parentItem, FilterContext parentFilterContext) {
        return parentFilterContext.getQueryParameter().getProcess() == Process.DEFAULT;
    }

    @Override
    protected List<ProcessGroupItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {
        return List.of(new ProcessGroupItem(context, item.getRepository()));
    }
}
