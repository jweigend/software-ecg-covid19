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

import de.qaware.ekg.awb.application.base.impl.EkgApplicationController;
import de.qaware.ekg.awb.common.ui.explorer.ExplorerController;
import de.qaware.ekg.awb.common.ui.explorer.RootItem;
import de.qaware.ekg.awb.commons.module.EkgModule;
import de.qaware.ekg.awb.repository.ui.admin.EkgRepositoryAdmin;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import javafx.stage.Stage;
import org.slf4j.Logger;

import javax.inject.Singleton;

@Singleton
@SuppressWarnings("unused")
public class EkgRepositoryAdminModule implements EkgModule, Module {

    private static final Logger LOGGER = EkgLogger.get();

    @Override
    public void start() {

        Stage appStage = EkgLookup.lookup(ApplicationWindow.class).getStage();
        EkgApplicationController appController = EkgLookup.lookup(EkgApplicationController.class);

        RootItem rootItem = (RootItem) EkgLookup.lookup(ExplorerController.class).getRootItem();
        rootItem.addContextMenuEntry("Add EKG repository", e -> {
            EkgRepositoryAdmin ekgRepositoryAdmin = new EkgRepositoryAdmin();
            ekgRepositoryAdmin.setTitle("Add EKG repository");
            ekgRepositoryAdmin.initOwner(appStage);
            ekgRepositoryAdmin.show();
        });
    }

    @Override
    public void preload() {
    }

    @Override
    public void stop() {
    }
}
