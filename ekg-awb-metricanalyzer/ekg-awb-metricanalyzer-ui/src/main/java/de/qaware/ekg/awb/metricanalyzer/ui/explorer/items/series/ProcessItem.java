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

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;

/**
 * The explorerView item to show a single process.
 */
public class ProcessItem extends ContextAwareItem {

    /**
     * Constructs a process item
     *
     * @param process    process
     * @param context    the query context
     * @param repository the containing types.
     */
    public ProcessItem(Process process, FilterContext context, EkgRepository repository) {
        super(process.getName(), context, repository);
        super.setGraphic(getIconProvider().getProcessItemIcon(this));
    }
}