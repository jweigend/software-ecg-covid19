package de.qaware.ekg.awb.project.ui.mgrdialog;

import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.repository.ImporterSourceRepository;
import de.qaware.ekg.awb.sdk.core.resourceloader.CachingSvgLoader;
import de.qaware.ekg.awb.sdk.core.resourceloader.CdiFxmlLoader;
import de.qaware.ekg.awb.sdk.core.resourceloader.ViewLoadResult;
import javafx.scene.Group;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * The ProjectEditDialog UI component is a JavaFX Dialog that will provide the all
 * editable project properties including the optional ImportSourceRepository data to
 * the AWB user.
 * The dialog can used to change project settings after it's initial creation.
 * Especially change the access data for the {@link ImporterSourceRepository}
 * is the most imported use case of this dialog.
 */
public class ProjectEditDialog extends Dialog<Project> {

    /**
     * Creates a new CreateProjectDialog instance
     *
     * @param repository the EKG repository that stores the project that should be edit in the current dialog scope
     * @param project the project that should be edit in the current dialog scope
     */
    public ProjectEditDialog(EkgRepository repository, Project project) {

        setTitle("Manage EKG project");

        ViewLoadResult viewResult = CdiFxmlLoader.loadView("ProjectEditDialogView.fxml");
        setDialogPane(viewResult.getComponent());

        ProjectEditDialogController controller = viewResult.getController();
        controller.setParent(this);
        controller.setRepository(repository);
        controller.setProject(project);

        // bind controller to Dialog closed with apply
        setResultConverter(controller::persistProject);

        setHeaderText("With this dialog you can change project properties as well as connection details " +
                "of the target system if using remote time series (split source projects).");

        Group iconImage = new CachingSvgLoader(false)
                .setDefaultScale(0.14)
                .getSvgImage("icons/ekg-repository-icon2.svg");

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/ekg-app-icon2.png").toExternalForm()));


        setGraphic(iconImage);
    }
}
