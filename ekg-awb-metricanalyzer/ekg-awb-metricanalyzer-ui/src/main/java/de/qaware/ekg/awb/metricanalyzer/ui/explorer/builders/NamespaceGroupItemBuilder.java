package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.logical.NamespaceGroupItem;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type NamespaceGroupItem.
 */
public class NamespaceGroupItemBuilder<T extends RepositoryBaseItem> extends AbstractConditionalItemBuilder<T> {

    @Override
    protected boolean isValidParentForChilds(T parentItem, FilterContext parentFilterContext) {
        return parentFilterContext.getQueryParameter().getNamespace() == Namespace.DEFAULT
                && parentFilterContext.getActiveViewFlavor() == ProjectViewFlavor.LOGICAL_VIEW;
    }

    @Override
    protected List<NamespaceGroupItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        CloudPlatformType platformType = context.getQueryParameter().getProject().getCloudPlatformType();
        final String itemName = platformType == CloudPlatformType.OPEN_SHIFT ? "Projekte (Namespaces)" : "Namespaces";

        return List.of(new NamespaceGroupItem(itemName, context, item.getRepository()));
    }
}