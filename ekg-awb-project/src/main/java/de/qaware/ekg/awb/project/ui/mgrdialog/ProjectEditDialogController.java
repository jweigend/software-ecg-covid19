package de.qaware.ekg.awb.project.ui.mgrdialog;

import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import de.qaware.ekg.awb.sdk.importer.ui.controls.qdsselector.QuerySourceSelector;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

/**
 * The controller of the ProjectEditDialogController UI component that includes
 * the whole UI logic of the dialog like managing it's state and interactions.
 */
public class ProjectEditDialogController extends ProjectDialogController {

    //-----------------------------------------------------------------------------------------------------------------
    // FXML view components
    //-----------------------------------------------------------------------------------------------------------------

    @FXML
    private QuerySourceSelector querySourceSelector;

    @FXML
    private BorderPane bpSourceSelectorDisableOverlay;


    //-----------------------------------------------------------------------------------------------------------------
    // members the controller needs for state and event handling
    //-----------------------------------------------------------------------------------------------------------------

    /**
     * EKG event bus used to notify other about new
     * repositories or types modifications
     */
    @Inject
    private EkgEventBus eventBus;

    //================================================================================================================
    //  Constructor
    //================================================================================================================

    public ProjectEditDialogController() {
        super(new ProjectEditDialogModel());
    }

    //================================================================================================================
    //  Controller API
    //================================================================================================================

    /**
     * Sets the Dialog this controller belongs to an manages the
     * child components.
     *
     * @param projectMgrDialog the Dialog that own the components controlled by this controller
     */
    public void setParent(Dialog projectMgrDialog) {
        projectMgrDialog.getDialogPane().lookupButton(ButtonType.APPLY)
                .disableProperty().bind(getViewModel().isValidProjectNameProperty().not());
    }

    /**
     * Persist a new specified project to types if necessary.
     * If user select an existing project nothing will happened.
     *
     * @param buttonType the type of the pressed button
     * @return the created types
     */
    public Project persistProject(ButtonType buttonType) {

        // reset state if not clicked the apply button
        if (buttonType != ButtonType.APPLY) {
            resetViewStates();
            return null;
        }

        ProjectDataAccessService projectDataAccess =
                ServiceDiscovery.lookup(ProjectDataAccessService.class, getViewModel().getSelectedRepository());

        Project projectToUpdate = getViewModel().getProject();
        projectToUpdate.setProjectFlavor(getViewModel().getProjectFlavor());
        projectToUpdate.setCloudPlatformType(getViewModel().getCloudPlatformType().toString());

        Project updatedProject = projectDataAccess.updateExistingProject(projectToUpdate);

        if (getViewModel().hasImporterSourceRepositoryProperty().get()) {
            querySourceSelector.submitChanges();
        }

        eventBus.publish(new ExplorerUpdateEvent(this, updatedProject));
        resetViewStates();
        return updatedProject;
    }

    public void setRepository(EkgRepository repository) {
        getViewModel().setSelectedRepository(repository);
        querySourceSelector.getRepositoryProperty().set(getViewModel().getSelectedRepository());
    }

    public void setProject(Project project) {
        getViewModel().setEditedProject(project);

        if (project == null) {
            return;
        }

        txtProjectName.setText(project.getName());
        cbCloudPlatformType.setValue(project.getCloudPlatformType());
        cbProjectFlavor.setValue(project.getProjectFlavor());

        querySourceSelector.setProjectName(getViewModel().getProjectName(), false);
        updateProjectNames(getViewModel().getSelectedRepository(), getViewModel().getProjectName());
    }

    //================================================================================================================
    // internal initializing logic
    //================================================================================================================

    @Override
    protected void initializeViewState() {
        super.initializeViewState();
        querySourceSelector.disableViewReset(true);
    }

    @Override
    protected void initializeViewBehavior() {
        super.initializeViewBehavior();
        bpSourceSelectorDisableOverlay.visibleProperty().bind(getViewModel().hasImporterSourceRepositoryProperty().not());

        txtProjectName.setStyle("-fx-text-fill: #a4a4a4");
        txtProjectName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                querySourceSelector.requestFocus();
            }
        });
    }

    //================================================================================================================
    //  helper methods
    //================================================================================================================

    private ProjectEditDialogModel getViewModel() {
        return (ProjectEditDialogModel) this.viewModel;
    }
}
