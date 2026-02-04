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

import de.qaware.ekg.awb.application.base.adapter.ChartPanelAdapter;
import de.qaware.ekg.awb.common.ui.events.FinishEvent;
import de.qaware.ekg.awb.metricanalyzer.ui.MetricAnalyzerUiModule;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.platform.api.Module;

import javax.inject.Singleton;

@Singleton
@SuppressWarnings("unused")
public class MetricAnalyzerModule extends MetricAnalyzerUiModule implements Module {

    @Override
    public void preload() {
        ModuleBinding.bind();
        EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);
        try {
            // Force to execute all other preloaders
            Thread.sleep(500);
            eventBus.publish(new ProgressEvent("Start local Solr Server...", 0, this));

            EkgLookup.lookup(ChartPanelAdapter.ChartPanelManager.class).getClass();
            eventBus.publish(new ProgressEvent("Finished starting local Solr Server...", 1, this));

            super.preload();

        } catch (InterruptedException e) {
            eventBus.publish(new FinishEvent(-1, this));
        }
    }
}
