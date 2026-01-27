package de.qaware.ekg.awb.project.ui.mgrdialog;

import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.ui.selector.RepositorySelectorPane;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import javax.inject.Inject;

/**
 * The controller of the CreateProjectDialog UI component that includes
 * the whole UI logic of the dialog like managing it's state and interactions.
 */
public class CreateProjectDialogController extends ProjectDialogController {


    //-----------------------------------------------------------------------------------------------------------------
    // FXML view components

    @FXML
    private RepositorySelectorPane repositorySelector;

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

    public CreateProjectDialogController() {
        super(new CreateProjectDialogModel());
    }

    //================================================================================================================
    //  Controller API that could be used by the FileImportDialog component or deriving implementations
    //================================================================================================================

    /**
     * Sets the Dialog this controller belongs to an manages the
     * child components.
     *
     * @param createProjectDialog the Dialog that own the components controlled by this controller
     */
    public void setParent(Dialog createProjectDialog) {
        createProjectDialog.getDialogPane().lookupButton(ButtonType.APPLY)
                .disableProperty().bind(getViewModel().isValidProjectConfigProperty().not());
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

        Project newProject = projectDataAccess.persistProject(getViewModel().getConfiguredProject());

        eventBus.publish(new ExplorerUpdateEvent(this, newProject));
        resetViewStates();
        return newProject;
    }

    //================================================================================================================
    //  internal implementation of controller functionality
    //================================================================================================================

    @Override
    protected void initializeModelToView() {
        super.initializeModelToView();
        getViewModel().selectedRepositoryProperty().bind(repositorySelector.selectedRepositoryProperty());
    }

    @Override
    protected void initializeViewState() {
        super.initializeViewState();
        updateProjectNames(repositorySelector.getSelectedRepository(), null);
    }

    /**
     * Initialize all handler that belongs to this component and will
     * proceed click actions or other event handling logic.
     */
    @Override
    protected void initializeHandler() {
        repositorySelector.selectedRepositoryProperty().addListener((observable, prevRepro, selectedRepro)
                -> updateProjectNames(selectedRepro, null));

        repositorySelector.setFilter(repository -> !repository.isEmbedded());
    }

    //================================================================================================================
    //  helper methods
    //================================================================================================================

    private CreateProjectDialogModel getViewModel() {
        return (CreateProjectDialogModel) this.viewModel;
    }
}
