//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.application.base.module;

import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookupStrategy;
import de.qaware.ekg.awb.sdk.core.lookup.EkgServiceLoaderLookupStrategy;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.EventBus;
import javafx.fxml.FXMLLoader;

public final class ModuleBinding {
    private ModuleBinding() {
    }

    /**
     * Binds the Lookup of the stagedriver to the lookup used in the modules
     */
    public static void bind() {
        if (EkgLookup.getLookupStrategy() == null) {
            if (Lookup.getLookupStrategy() != null) {
                EkgLookupStrategy lookupStrategy = Lookup.lookup(EkgLookupStrategy.class);
                EkgLookup.init(lookupStrategy);
            } else {
                EkgServiceLoaderLookupStrategy lookupStrategy = new EkgServiceLoaderLookupStrategy();
                lookupStrategy.init(FXMLLoader.class, (EkgServiceLoaderLookupStrategy.Producer<FXMLLoader>) FXMLLoader::new);
                EkgLookup.init(lookupStrategy);
            }

            // Connect the eventbus of the stagedriver to the eventbus of the modules
            EventBus eventBusSdfx = Lookup.lookup(EventBus.class);
            EkgLookup.lookup(EkgEventBus.class).subscribe(ProgressEvent.class, (event)
                    -> eventBusSdfx.publish(new de.qaware.sdfx.platform.api.events.ProgressEvent(((ProgressEvent) event).getMessage(), ((ProgressEvent) event).getProgress(), event.getSource())));

        }
    }

}
