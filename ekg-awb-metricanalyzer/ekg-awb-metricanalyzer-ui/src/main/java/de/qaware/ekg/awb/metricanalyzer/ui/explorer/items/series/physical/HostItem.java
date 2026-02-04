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
package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.physical;

import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;

/**
 * The explorerView item that shows a single host.
 */
public class HostItem extends ContextAwareItem {

    /**
     * Creates a HostItem with the given name and the Context.
     *
     * @param name    the item's name
     * @param context the context.
     * @param repository   the containing types.
     */
    public HostItem(String name, FilterContext context, EkgRepository repository) {
        super(name, context, repository);
        super.setGraphic(getIconProvider().getHostItemIcon(this));
    }
}