//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
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
 * Explorer tree item to group the counter groups.
 */
public class CounterItem extends ContextAwareItem {

    /**
     * The Constructor to create a CounterItem.
     *
     * @param context    the context to use for creating
     * @param repository the containing types.
     */
    public CounterItem(FilterContext context, EkgRepository repository) {
        super(isNotBlank(getProject(context).getDimensionAliasMetricGroup()) ?
                getProject(context).getDimensionAliasMetricGroup() : "Metriken", context, repository);
        super.setGraphic(getIconProvider().getCounterItemIcon(this));
    }

    @Override
    protected int getOrderPriority() {
        return 80;
    }
}
