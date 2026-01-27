package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.Host;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.HostGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.physical.HostItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.physical.HostsRootItem;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type HostItem
 */
public class HostItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    @Override
    protected List<HostItem> getChildItemsFor(T item, FilterContext parentContext, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);

        List<HostGroup> hostGroups = metricDataAccess.getHostGroups(new MetricQuery(parentContext.getQueryParameter()));
        if (item instanceof HostsRootItem &&
                !(hostGroups.isEmpty() || (hostGroups.size() == 1 && "*".equals(hostGroups.get(0).getValueName())))) {
            return List.of();
        }

        List<Host> metricHosts = metricDataAccess.getHosts(new MetricQuery(parentContext.getQueryParameter()));
        int amountFetchedHosts = metricHosts.size();

        return ItemBuilder.getItems(metricHosts.stream().map(Host::getName),
              host -> {
                  QueryFilterParams parentQueryParams = parentContext.getQueryParameter();
                  QueryFilterParams childQueryParams = new QueryFilterParams.Builder(parentQueryParams).withHost(host).build();

                  return new HostItem(host, parentContext.cloneWith(childQueryParams), item.getRepository());

                },
              notifier, amountFetchedHosts, "Hosts"
        );
    }
}