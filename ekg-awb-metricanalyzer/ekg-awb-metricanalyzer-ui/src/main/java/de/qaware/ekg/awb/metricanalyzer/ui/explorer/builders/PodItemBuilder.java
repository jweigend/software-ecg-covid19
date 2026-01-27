package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Pod;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.logical.PodItem;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type PodItem
 */
public class PodItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    @Override
    protected List<PodItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);

        List<Pod> pods = metricDataAccess.getPods(new MetricQuery(context.getQueryParameter()));

        return ItemBuilder.getItems(pods.stream(),
                pod -> {
                    QueryFilterParams parentParams = context.getQueryParameter();
                    QueryFilterParams childQueryParams = new QueryFilterParams.Builder(parentParams).withPod(pod).build();
                    return new PodItem(pod.getValueName(), context.cloneWith(childQueryParams), item.getRepository());
                },
                notifier,
                pods.size(),
                "Pods"
        );
    }
}