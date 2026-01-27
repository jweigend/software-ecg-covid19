//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui;

import de.qaware.ekg.awb.commons.module.EkgModule;
import de.qaware.ekg.awb.explorer.ui.TreeIconsProviderRegistry;
import de.qaware.ekg.awb.metricanalyzer.ui.api.MetricTreeIconsProvider;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.ItemBuilderManager;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;

import javax.inject.Singleton;

/**
 * The EKG Module. Prepare the ui while preloading and handles
 * the start and stop of the UI.
 */
@Singleton
public class MetricAnalyzerUiModule implements EkgModule {

    /**
     * Get the human readable module name.
     *
     * @return The module name.
     */
    public String getName() {
        return "Software-EKG - MetricAnalyzer UI";
    }

    /**
     * Get the version of this module.
     *
     * @return The version of the module.
     */
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void preload() {
        EkgLookup.lookup(TreeIconsProviderRegistry.class).registerDefaultTreeIconProvider(MetricTreeIconsProvider.DEFAULT);
        ItemBuilderManager.registerTreeItemBuilders();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
