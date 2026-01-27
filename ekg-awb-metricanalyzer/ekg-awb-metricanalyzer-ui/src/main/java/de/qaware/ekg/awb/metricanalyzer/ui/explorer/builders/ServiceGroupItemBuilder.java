package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Service;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.logical.ServiceGroupItem;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type ServiceGroupItem
 */
public class ServiceGroupItemBuilder<T extends RepositoryBaseItem> extends AbstractConditionalItemBuilder<T> {

    @Override
    protected boolean isValidParentForChilds(T parentItem, FilterContext parentFilterContext) {
        return parentFilterContext.getQueryParameter().getService() == Service.DEFAULT
                && parentFilterContext.getActiveViewFlavor() == ProjectViewFlavor.LOGICAL_VIEW;
    }

    @Override
    protected List<ServiceGroupItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {
        return List.of(new ServiceGroupItem("Services", context, item.getRepository()));
    }
}