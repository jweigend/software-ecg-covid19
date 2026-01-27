package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.HostGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.physical.HostsRootItem;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

import java.util.List;

/**
 * TreeItem builder that produces a HostGroupsItem that acts as
 * parent for all HostGroupItem's
 */
public class HostsRootItemBuilder<T extends RepositoryBaseItem> extends AbstractConditionalItemBuilder<T> {

    @Override
    protected boolean isValidParentForChilds(T parentItem, FilterContext parentFilterContext) {
        return parentFilterContext.getActiveViewFlavor() == ProjectViewFlavor.PHYSICAL_VIEW;
    }

    @Override
    protected List<HostsRootItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);
        List<HostGroup> hostGroups = metricDataAccess.getHostGroups(new MetricQuery(context.getQueryParameter()));

        // one host group with '*' always exists
        if (hostGroups.isEmpty() || (hostGroups.size() == 1 && "*".equals(hostGroups.get(0).getValueName()))) {
            String aliasLabel = context.getQueryParameter().getProject().getDimensionAliasHost();
            return List.of(new HostsRootItem(aliasLabel, "Hosts", context, item.getRepository()));
        } else {
            String aliasLabel = context.getQueryParameter().getProject().getDimensionAliasHostGroup();
            return List.of(new HostsRootItem(aliasLabel,"Cluster/devices", context, item.getRepository()));
        }
    }
}
