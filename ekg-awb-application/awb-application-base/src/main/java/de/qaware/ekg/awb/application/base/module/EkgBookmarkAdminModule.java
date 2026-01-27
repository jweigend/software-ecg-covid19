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
package de.qaware.ekg.awb.application.base.module;

import de.qaware.ekg.awb.commons.module.ModuleException;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.BookmarkAdminModule;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.sdfx.platform.api.Module;
import org.slf4j.Logger;

import javax.inject.Singleton;

@Singleton
@SuppressWarnings("unused")
public class EkgBookmarkAdminModule extends BookmarkAdminModule implements Module {
    private static final Logger LOGGER = EkgLogger.get();

    @Override
    public void preload() {
        ModuleBinding.bind();

        try {
            super.preload();
        } catch (ModuleException e) {
            LOGGER.info(e.getMessage());
        }
    }
}
