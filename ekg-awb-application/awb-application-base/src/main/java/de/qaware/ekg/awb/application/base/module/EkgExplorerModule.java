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

import de.qaware.ekg.awb.application.base.view.ViewMapper;
import de.qaware.ekg.awb.common.ui.explorer.ExplorerController;
import de.qaware.ekg.awb.commons.module.ModuleException;
import de.qaware.ekg.awb.explorer.ui.ExplorerModule;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.scene.control.SplitPane;
import org.slf4j.Logger;

import javax.inject.Singleton;

@Singleton
@SuppressWarnings("unused")
public class EkgExplorerModule extends ExplorerModule implements Module {

    private static final Logger LOGGER = EkgLogger.get();

    @Override
    public void preload() {
        ModuleBinding.bind();

        try {
            super.preload();
        }
        catch (ModuleException e) {
            LOGGER.info(e.getMessage());
        }
    }

    @Override
    public void start() {
        WindowManager windowManager = EkgLookup.lookup(WindowManager.class);

        FXMLView<ExplorerController> view = new ViewMapper<ExplorerController>().convert(explorerView, Position.LEFT, 0.10);
        windowManager.register(view);
        SplitPane rootSplitPane = (SplitPane) windowManager.getRootPane().getChildrenUnmodifiable().get(0);
        rootSplitPane.setDividerPositions(0.2);
    }
}
