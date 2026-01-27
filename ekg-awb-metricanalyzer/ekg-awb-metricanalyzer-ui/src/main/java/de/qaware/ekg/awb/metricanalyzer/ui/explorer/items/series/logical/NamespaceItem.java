package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.logical;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;

public class NamespaceItem extends ContextAwareItem {


    /**
     * Creates a new namespace item based on the given context and the project
     *
     * @param namespace  the Namespace entity that will represented by the tree item and contains the label that will shown.
     * @param context    bean that define filters that relating to series data or the view flavor and control the item rendering
     * @param repository the containing types.
     */
    public NamespaceItem(Namespace namespace, FilterContext context, EkgRepository repository) {
        super(namespace.getValueName(), context, repository);
        super.setGraphic(getIconProvider().getNamespaceItemIcon(this));
    }
}
