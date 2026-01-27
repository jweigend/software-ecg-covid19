package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.MetricGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.MetricGroupItem;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type MetricGroupItem
 */
public class MetricGroupItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    @Override
    protected List<MetricGroupItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);

        List<MetricGroup> metricGroups = metricDataAccess.getMetricGroups(new MetricQuery(context.getQueryParameter()));

        return ItemBuilder.getItems(metricGroups.stream(),
                metricGroup -> {
                    QueryFilterParams parentParams = context.getQueryParameter();
                    QueryFilterParams childQueryParams = new QueryFilterParams.Builder(parentParams).withMetricGroup(metricGroup).build();
                    return new MetricGroupItem(metricGroup, context.cloneWith(childQueryParams), item.getRepository());
                },
                notifier,
                metricGroups.size(),
                "MetricGroups"
        );
    }
}