//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.events;

import java.util.EventObject;

public class FinishEvent extends EventObject {

    private final int finishCode;

    /**
     * FinishEvent for calculating a progress.
     *
     * @param finishCode the finishCode the Event will carry
     * @param source     the object creating this event
     */
    public FinishEvent(int finishCode, Object source) {
        super(source);
        this.finishCode = finishCode;
    }

    /**
     * Returns the Event's finishCode.
     *
     * @return the Event's finishCode
     */
    public int getFinishCode() {
        return finishCode;
    }
}
