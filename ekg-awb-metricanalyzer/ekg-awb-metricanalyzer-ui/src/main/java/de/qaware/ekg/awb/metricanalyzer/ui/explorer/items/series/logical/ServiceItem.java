package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.logical;

import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;

public class ServiceItem extends ContextAwareItem {

    /**
     * Creates a new Service item based on the given context and the project
     *
     * @param label      the name of the tree item that will used as displayed label in the view
     * @param context    bean that define filters that relating to series data or the view flavor and control the item rendering
     * @param repository the containing types.
     */
    public ServiceItem(String label, FilterContext context, EkgRepository repository) {
        super(label, context, repository);
        super.setGraphic(getIconProvider().getServiceItemIcon(this));
    }
}
