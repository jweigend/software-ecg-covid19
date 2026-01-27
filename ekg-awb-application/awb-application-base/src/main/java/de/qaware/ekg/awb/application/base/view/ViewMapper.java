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
package de.qaware.ekg.awb.application.base.view;

import de.qaware.ekg.awb.common.ui.view.EkgView;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;

/**
 * Mapping of the EkgView to FXMLView
 *
 * @param <CONTROLLER> controller
 */
public class ViewMapper<CONTROLLER> {

    /**
     * Converts a EkgView to a FXMLView
     * @param ekgView ekg view
     * @param position position for fxml view
     * @param areaSize area size for fxml view
     * @return fxml view
     */
    public FXMLView<CONTROLLER> convert(EkgView<CONTROLLER> ekgView, Position position, double areaSize) {
        return new FXMLView.Builder<CONTROLLER>()
                .withId(ekgView.getViewId())
                .withTitle(ekgView.getTitle())
                .withPos(position)
                .withToolTipInfo(ekgView.getToolTipInfo())
                .withViewAreaSize(areaSize)
                .withViewImage(ekgView.getViewImagePath())
                .withRootPane(ekgView.getRootNode())
                .withController(ekgView.getController())
                .clone();
    }
}
