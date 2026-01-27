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
package de.qaware.ekg.awb.repository.api.events;


import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.core.events.ChangedEvent;

/**
 * An event that indicates that a types has been changed.
 */
public class RepositoryChangeEvent extends ChangedEvent<EkgRepository> {

    /**
     * Create a new changed event
     *
     * @param obj    The changed object.
     * @param change the type of the change
     * @param source the sender object.
     */
    public RepositoryChangeEvent(EkgRepository obj, Change change, Object source) {
        super(obj, change, source);
    }
}
