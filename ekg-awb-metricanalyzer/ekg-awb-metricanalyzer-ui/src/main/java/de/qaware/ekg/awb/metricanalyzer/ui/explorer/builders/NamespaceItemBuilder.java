package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.logical.NamespaceItem;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type NamespaceItem.
 */
public class NamespaceItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    @Override
    protected List<NamespaceItem> getChildItemsFor(T item, FilterContext parentContext, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);

        List<Namespace> namespaces = metricDataAccess.getNamespaces(new MetricQuery(parentContext.getQueryParameter()));

        CloudPlatformType platformType = parentContext.getQueryParameter().getProject().getCloudPlatformType();
        final String itemName = platformType == CloudPlatformType.OPEN_SHIFT ? "Projekt (Namespace)" : "Namespace";

        return ItemBuilder.getItems(namespaces.stream(),
                namespace -> {
                    QueryFilterParams parentQueryParams = parentContext.getQueryParameter();
                    QueryFilterParams childQueryParams = new QueryFilterParams.Builder(parentQueryParams).withNamespace(namespace).build();
                    return new NamespaceItem(namespace, parentContext.cloneWith(childQueryParams), item.getRepository());
                },
                notifier,
                namespaces.size(),
                itemName
        );
    }
}