//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.MetricGroup;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;

/**
 * Explorer menu item to group metric counters.
 */
public class MetricGroupItem extends ContextAwareItem {

    /**
     * Creates a MetricGroupItem with the given parameter.
     *
     * @param metricGroup   the metricGroup to use as name
     * @param context the context
     *                @param repository the containing types.
     */
    public MetricGroupItem(MetricGroup metricGroup, FilterContext context, EkgRepository repository) {
        super(metricGroup.getName(), context, repository);
        super.setGraphic(getIconProvider().getMetricGroupItemIcon(this));
    }
}
