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

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;

/**
 * The explorerView tree item for a single series.
 */
public class MeasurementItem extends ContextAwareItem {

    /**
     * The icon of the node in the explorerView tree
     */
    private static final String NODE_ICON = "/de/qaware/ekg/awb/metricanalyzer/ui/explorer/icons/series/counters.png";


    /**
     * Constructs a series from the given arguments
     *
     * @param measurement - the series
     * @param context     - the query context
     *                    @param repository the containing types.
     */
    public MeasurementItem(Measurement measurement, FilterContext context, EkgRepository repository) {
        super(measurement.getName(), context, repository);
        super.setGraphic(getIconProvider().getMeasurementItemIcon(this));
    }
}