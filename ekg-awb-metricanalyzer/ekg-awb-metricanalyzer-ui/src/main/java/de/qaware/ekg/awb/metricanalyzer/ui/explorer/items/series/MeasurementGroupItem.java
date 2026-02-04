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

import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The explorerView tree item to group the series items.
 */
public class MeasurementGroupItem extends ContextAwareItem {

    /**
     * Constructs a measurements item
     *
     * @param context    - the query context
     * @param repository the containing types.
     */
    public MeasurementGroupItem(FilterContext context, EkgRepository repository) {
        super(isNotBlank(getProject(context).getDimensionAliasMeasurement()) ?
                getProject(context).getDimensionAliasMeasurement() : "Measurements", context, repository);

        super.setGraphic(getIconProvider().getMeasurementGroupItemIcon(this));
    }

    @Override
    protected int getOrderPriority() {
        return 50;
    }
}
