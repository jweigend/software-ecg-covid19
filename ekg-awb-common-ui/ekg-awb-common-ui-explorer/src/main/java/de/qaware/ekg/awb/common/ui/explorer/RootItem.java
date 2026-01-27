//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.explorer;


import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;

import javax.inject.Singleton;

/**
 * Base root item.
 */
@Singleton
public class RootItem extends AbstractItem<String> {

    /**
     * Init the root item.
     */
    public RootItem() {
        super("Workbench");
    }
}
