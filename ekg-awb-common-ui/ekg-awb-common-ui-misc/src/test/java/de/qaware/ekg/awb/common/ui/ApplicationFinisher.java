//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui;

import de.qaware.ekg.awb.common.ui.events.FinishEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import org.slf4j.Logger;

/**
 * This class is used to exit applications.
 */
public class ApplicationFinisher {

    /**
     * Our logger.
     */
    private static final Logger LOGGER = EkgLogger.get();

    /**
     * Encapsulates the application exit process.
     * <p>
     * Warning is suppresses as System.exit must be used here, to provide the functionality for finishing the
     * test Applications.
     *
     * @param code the exit return code
     */
    @SuppressWarnings("all")
    public static void exit(int code) {
        System.exit(code);
    }

    /**
     * Encapsulates the application exit process.
     * <p>
     * Warning is suppresses as System.exit must be used here, to provide the functionality for finishing the
     * test Applications.
     *
     * @param event the finish event to get the finishcode from
     */
    @SuppressWarnings("all")
    @EkgEventSubscriber(eventClass = FinishEvent.class)
    public void closeApplication(final FinishEvent event) {
        LOGGER.info("FINISHING APPLICATION - Received FinishEvent from '" + event.getSource().getClass().getName() + "'");
        exit(event.getFinishCode());
    }

}
