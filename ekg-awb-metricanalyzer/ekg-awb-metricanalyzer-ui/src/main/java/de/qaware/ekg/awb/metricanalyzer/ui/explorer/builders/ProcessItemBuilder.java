package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.ProcessItem;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type ProcessItem
 */
public class ProcessItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    @Override
    protected List<ProcessItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);

        List<Process> metricProcesses = metricDataAccess.getProcesses(new MetricQuery(context.getQueryParameter()));

        return ItemBuilder.getItems(metricProcesses.stream(),
                process -> {
                    QueryFilterParams parentParams = context.getQueryParameter();
                    QueryFilterParams childQueryParams = new QueryFilterParams.Builder(parentParams).withProcess(process).build();
                    return new ProcessItem(process, context.cloneWith(childQueryParams), item.getRepository());
                },
                notifier,
                metricProcesses.size(),
                "Process"
        );
    }
}