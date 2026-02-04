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
package de.qaware.ekg.awb.repository.ui.admin;

import afester.javafx.svg.SvgLoader;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.events.RepositoryModifyEvent;
import de.qaware.ekg.awb.sdk.core.resourceloader.CdiFxmlLoader;
import de.qaware.ekg.awb.sdk.core.resourceloader.ViewLoadResult;
import javafx.scene.Group;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * The connection controller controls the Dialog where the user can
 * addAndSum Repositories from the network/from URLs.
 */
public class EkgRepositoryAdmin extends Dialog<EkgRepository> {

    private EkgRepositoryAdminController controller;

    /**
     * Creates a new EkgRepositoryAdmin
     */
    public EkgRepositoryAdmin() {

        setTitle("Add EKG repository");

        ViewLoadResult viewResult = CdiFxmlLoader.loadView("EkgRepositoryAdminView.fxml");
        setDialogPane(viewResult.getComponent());

        controller = viewResult.getController();
        controller.setParent(this);

        // bind controller to Dialog closed with apply
        setResultConverter(controller::openConnection);

        setHeaderText("To create another EKG repository, please specify the type and " +
                "connection details.\n Inputs and connection will be checked immediately.");

        InputStream iconStream = EkgRepositoryAdmin.class.getResourceAsStream("icons/ekg-repository-icon2.svg");
        Group iconImage = new SvgLoader().loadSvg(iconStream);
        iconImage.setScaleX(0.14);
        iconImage.setScaleY(0.14);
        setGraphic(new Group(iconImage));

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/ekg-app-icon2.png").toExternalForm()));
    }

    public boolean handleModifyRepositoryEvent(RepositoryModifyEvent event) {
        return controller.handleModifyRepositoryEvent(event);
    }

    public void setOldRepository(EkgRepository repository) {
        controller.setOldRepository(repository);
    }
}
