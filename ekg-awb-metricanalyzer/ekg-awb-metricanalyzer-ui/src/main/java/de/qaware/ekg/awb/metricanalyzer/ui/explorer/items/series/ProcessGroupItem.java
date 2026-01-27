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
 * The explorerView item to group several processes under one item.
 */
public class ProcessGroupItem extends ContextAwareItem {

    /**
     * Constructs a process item from the given query context
     *
     * @param context - the query context
     * @param repository the containing types.
     */
    public ProcessGroupItem(FilterContext context, EkgRepository repository) {
        super(isNotBlank(getProject(context).getDimensionAliasProcess()) ?
                getProject(context).getDimensionAliasProcess() : "Processes", context, repository);
        super.setGraphic(getIconProvider().getProcessGroupItemIcon(this));
    }

    @Override
    protected int getOrderPriority() {
        return 70;
    }
}
