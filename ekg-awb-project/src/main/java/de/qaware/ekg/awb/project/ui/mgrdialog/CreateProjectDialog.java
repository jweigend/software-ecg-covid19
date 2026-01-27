package de.qaware.ekg.awb.project.ui.mgrdialog;

import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.sdk.core.resourceloader.CachingSvgLoader;
import de.qaware.ekg.awb.sdk.core.resourceloader.CdiFxmlLoader;
import de.qaware.ekg.awb.sdk.core.resourceloader.ViewLoadResult;
import javafx.scene.Group;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Dialog component that is used to create new empty projects in a remote repository that
 * can used by EKG-Collector to fill it with time series data directly from the collectors.
 */
public class CreateProjectDialog extends Dialog<Project> {

    private static final String DEFAULT_TITLE = "Adding EKG Collector-Project";

    /**
     * Creates a new CreateProjectDialog instance
     */
    public CreateProjectDialog() {

        setTitle(DEFAULT_TITLE);

        ViewLoadResult viewResult = CdiFxmlLoader.loadView("CreateProjectDialogView.fxml");
        setDialogPane(viewResult.getComponent());

        CreateProjectDialogController controller = viewResult.getController();
        controller.setParent(this);

        // bind controller to Dialog closed with apply
        setResultConverter(controller::persistProject);

        setHeaderText("To create a new project for the EKG collector, specify" +
                " all project properties. This makes sense \n for remote EKG repositories only.");

        Group iconImage = new CachingSvgLoader(false)
                .setDefaultScale(0.14)
                .getSvgImage("icons/ekg-repository-icon2.svg");

        setGraphic(iconImage);

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/ekg-app-icon2.png").toExternalForm()));
    }
}
