package de.qaware.ekg.awb.project.ui.projectbar;

import de.qaware.ekg.awb.project.api.ProjectViewFlavorChangedEvent;
import de.qaware.ekg.awb.project.api.events.ProjectSelectedEvent;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.project.ui.mgrdialog.ProjectEditDialog;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * This controller manages the {@link ProjectBar} UI component
 * and it's actions.
 */
public class ProjectBarController implements Initializable {

    @FXML
    private ComboBox<ProjectViewFlavor> cbViewFlavor;

    @FXML
    private Button btnEditProject;

    @FXML
    private Button btnImportDataToProject;

    @FXML
    private Label lbProjectId;

    @FXML
    private Label lbProjectType;

    @FXML
    private Label lbPlatformFlavor;

    private GridPane gpProjectBar;

    private EkgEventBus eventBus;

    private Project activeProject = null;

    /**
     * the EKG repository that stores the active (chosen) project
     * viewed in the ProjectBar
     */
    private EkgRepository activeEkgRepository = null;

    private Map<ProjectViewFlavor, BooleanProperty> disablePropertyMap = new HashMap<>();

    //=================================================================================================================
    //  implementation of Initializable interface
    //=================================================================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        disablePropertyMap.put(ProjectViewFlavor.LOGICAL_VIEW, new SimpleBooleanProperty(true));
        disablePropertyMap.put(ProjectViewFlavor.PHYSICAL_VIEW, new SimpleBooleanProperty(true));

        eventBus = EkgLookup.lookup(EkgEventBus.class);

        cbViewFlavor.setSkin(new ViewFlavorComboBoxSkin<>(cbViewFlavor, disablePropertyMap));

        cbViewFlavor.getItems().addAll(
                ProjectViewFlavor.PHYSICAL_VIEW,
                ProjectViewFlavor.LOGICAL_VIEW
        );

        cbViewFlavor.setValue(null);
        cbViewFlavor.valueProperty().addListener((observable, oldValue, newValue) -> {
            eventBus.publish(new ProjectViewFlavorChangedEvent(this, activeProject, oldValue, newValue));
        });

        btnEditProject.setOnMouseClicked(event -> new ProjectEditDialog(activeEkgRepository, activeProject).showAndWait());
    }

    //=================================================================================================================
    //  controller own API
    //=================================================================================================================

    @EkgEventSubscriber(eventClass = ProjectSelectedEvent.class)
    public void changeProject(ProjectSelectedEvent event) {

        gpProjectBar.setDisable(false);

        activeProject = event.getSelectedProject();
        activeEkgRepository = event.getRepository();

        lbProjectId.setText(activeProject.getName());
        lbProjectType.setText(activeProject.useSplitSource() ? "Fremdsystem" : "EKG Repository");

        CloudPlatformType cloudPlatformType = activeProject.getCloudPlatformType();
        if (cloudPlatformType == CloudPlatformType.NONE) {
            lbPlatformFlavor.setText("-");
            disablePropertyMap.get(ProjectViewFlavor.PHYSICAL_VIEW).setValue(false);
            disablePropertyMap.get(ProjectViewFlavor.LOGICAL_VIEW).setValue(true);
            cbViewFlavor.setValue(ProjectViewFlavor.PHYSICAL_VIEW);

        } else  {
            if (cloudPlatformType == CloudPlatformType.OTHER) {
                lbPlatformFlavor.setText("Others");
            } else {
                lbPlatformFlavor.setText(cloudPlatformType.getName());
            }
            ProjectFlavor activeFlavor = activeProject.getProjectFlavor();
            disablePropertyMap.get(ProjectViewFlavor.PHYSICAL_VIEW).setValue(activeFlavor == ProjectFlavor.CLASSIC);
            disablePropertyMap.get(ProjectViewFlavor.LOGICAL_VIEW).setValue(false);
            cbViewFlavor.setValue(ProjectViewFlavor.LOGICAL_VIEW);
        }
    }

    public void setGpProjectBar(GridPane gpProjectBarController) {
        this.gpProjectBar = gpProjectBarController;
    }
}
