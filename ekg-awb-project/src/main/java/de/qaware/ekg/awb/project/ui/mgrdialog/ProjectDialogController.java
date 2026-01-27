package de.qaware.ekg.awb.project.ui.mgrdialog;

import de.qaware.ekg.awb.common.ui.bindings.Bindings;
import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.repository.Repository;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import de.qaware.ekg.awb.sdk.core.resourceloader.CachingSvgLoader;
import de.qaware.ekg.awb.sdk.importer.ui.skins.EkgComboBoxListViewSkin;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType.NONE;
import static de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor.CLASSIC;

/**
 * This controller implements the base control logic for
 * all dialog ui components that derive from it to support the use cases
 * of create and update EKG projects.
 * All the common stuff like project name validation is sourced out to this class
 * and prevent a lot of clue code and redundancy in the deriving controller implementations.
 */
public class ProjectDialogController implements Initializable {

    protected static final String COMMON_ICONS_ABSOLUTE_PATH = "/de/qaware/ekg/awb/project/ui/common-icons";

    protected static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    //-----------------------------------------------------------------------------------------------------------------
    // Skins for the combo boxes
    //-----------------------------------------------------------------------------------------------------------------

    protected final Map<CloudPlatformType, EkgComboBoxListViewSkin.CellBehaviorResource> cloudPlatformResourceMap = new HashMap<>();

    protected final Map<ProjectFlavor, EkgComboBoxListViewSkin.CellBehaviorResource> projectFlavorResourceMap = new HashMap<>();

    //-----------------------------------------------------------------------------------------------------------------
    // FXML view components
    //-----------------------------------------------------------------------------------------------------------------

    @FXML
    protected TextField txtProjectName;

    @FXML
    protected Group warningIcon;

    @FXML
    protected Group errorBubble;

    @FXML
    protected ComboBox<ProjectFlavor> cbProjectFlavor;

    @FXML
    protected ComboBox<CloudPlatformType> cbCloudPlatformType;

    //-----------------------------------------------------------------------------------------------------------------
    // members the controller needs for state and event handling
    //-----------------------------------------------------------------------------------------------------------------

    protected ProjectDialogModel viewModel;

    //================================================================================================================
    //  constructor
    //================================================================================================================

    protected ProjectDialogController(ProjectDialogModel dialogModel) {
        this.viewModel = dialogModel;
    }

    //================================================================================================================
    //  Initializable interface implementation
    //================================================================================================================

    /* (non-Javadoc)
     * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // init pseudo classes and other view styles that
        // will setup programmatically
        initializeViewStyles();

        // set all members to initial values
        initializeViewState();

        // init default behaviour (validation, filter, ...0) and binding
        // before calling methods overwritten by deriving classes
        initializeViewBehavior();

        // update view from model properties and link the binding if exists
        initializeModelToView();

        // init event handlers
        initializeHandler();
    }

    //================================================================================================================
    //  internal implementation of controller functionality
    //================================================================================================================

    /**
     * Initialize all handler that belongs to this component and will
     * proceed click actions or other event handling logic.
     */
    protected void initializeHandler() {

    }

    /**
     * Initialize the view state the UI component should have than shown for the first time.
     * This means to programmatically set members to the initial values and trigger mechanisms
     * like validation that based on it.
     */
    protected void initializeViewState() {
        // addAndSum invalid pseudo class to text-field - to control the validation state
        txtProjectName.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
    }

    /**
     * Initialize the view component behaviour like enable/disable switching
     * or or any other visible state changes (like specific prompt texts) that
     * based on the underlying view data.
     */
    protected void initializeViewBehavior() {
        errorBubble.visibleProperty().bind(warningIcon.hoverProperty());
        warningIcon.visibleProperty().bind(viewModel.projectNameUniqueProperty());
    }

    /**
     * Initialize the view components with data by binding the model data to it
     * or set it directly.
     */
    protected void initializeModelToView() {

        // initialize both combo boxes
        initProjectFlavorCombobox();
        initCloudPlatformTypeCombobox();

        viewModel.projectNameProperty().bind(txtProjectName.textProperty());
        viewModel.projectFlavorProperty().bindBidirectional(cbProjectFlavor.valueProperty());
        viewModel.cloudPlatformTypeProperty().bindBidirectional(cbCloudPlatformType.valueProperty());
    }

    /**
     * Initialize the view styles that have to be configured programmatically.
     * (default styling should be define by style sheets)
     */
    protected void initializeViewStyles() {

        cloudPlatformResourceMap.put(CloudPlatformType.NONE, new EkgComboBoxListViewSkin.CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/none-icon.svg",
                new ClassicProjectAware(viewModel.projectFlavorProperty())));
        cloudPlatformResourceMap.put(CloudPlatformType.KUBERNETES, new EkgComboBoxListViewSkin.CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/kubernetes-icon.svg",
                new CloudProjectAware(viewModel.projectFlavorProperty())));
        cloudPlatformResourceMap.put(CloudPlatformType.OPEN_SHIFT, new EkgComboBoxListViewSkin.CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/openshift-icon.svg",
                new CloudProjectAware(viewModel.projectFlavorProperty())));
        cloudPlatformResourceMap.put(CloudPlatformType.OTHER, new EkgComboBoxListViewSkin.CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/cloud-other-icon.svg",
                new CloudProjectAware(viewModel.projectFlavorProperty())));

        projectFlavorResourceMap.put(ProjectFlavor.CLASSIC, new EkgComboBoxListViewSkin.CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/baremetal-icon.svg",
                new ReadOnlyBooleanWrapper(false)));
        projectFlavorResourceMap.put(ProjectFlavor.HYBRID, new EkgComboBoxListViewSkin.CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/hybrid-icon.svg",
                new ReadOnlyBooleanWrapper(false)));
        projectFlavorResourceMap.put(ProjectFlavor.CLOUD_NATIVE, new EkgComboBoxListViewSkin.CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/cloud-native-icon.svg",
                new ReadOnlyBooleanWrapper(false)));


        // create SVG icon and it it to an empty placeholder-group element on the left of the text field
        Group iconImage = new CachingSvgLoader(false).setDefaultScale(0.07).getSvgImage(COMMON_ICONS_ABSOLUTE_PATH + "/warning-icon.svg");
        warningIcon.getChildren().add(iconImage);

        // update the view style of textfield that contains the project name according the the current
        // validation state
        viewModel.projectNameUniqueProperty().addListener((observable, oldState, newState)
                -> txtProjectName.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, newState));
    }

    /**
     * Initialize the model-binding and view skins of the ProjectFlavor combobox
     */
    protected void initProjectFlavorCombobox() {
        cbProjectFlavor.setSkin(new EkgComboBoxListViewSkin<>(cbProjectFlavor, projectFlavorResourceMap, CLASSIC));
        Bindings.bindComboBox(cbProjectFlavor, FXCollections.observableArrayList(ProjectFlavor.values()),
                viewModel.projectFlavorProperty());
    }

    /**
     * Initialize the model-binding and view skins of the CloudPlatformType combobox
     */
    protected void initCloudPlatformTypeCombobox() {
        cbCloudPlatformType.setSkin(new EkgComboBoxListViewSkin<>(cbCloudPlatformType, cloudPlatformResourceMap, NONE));
        Bindings.bindComboBox(cbCloudPlatformType, FXCollections.observableArrayList(CloudPlatformType.values()),
                viewModel.cloudPlatformTypeProperty());
    }

    //================================================================================================================
    // internal control logic of the controller
    //================================================================================================================

    /**
     * Update the set of project names hold by the view model
     * with data fetched from the given repository.
     *
     * @param selectedRepository the repository that should used to resolve contained project names
     * @param projectToFilter a single project name that should filtered from the list of project names if specified
     */
    protected void updateProjectNames(Repository selectedRepository, String projectToFilter) {
        if (selectedRepository == null) {
            return;
        }

        ProjectDataAccessService projectDataAccess = ServiceDiscovery.lookup(ProjectDataAccessService.class, selectedRepository);

        Collection<Project> projects = projectDataAccess.listProjects();
        viewModel.setProjectNames(projects.stream()
                .map(Project::getName)
                .filter(s -> !s.equals(projectToFilter))
                .collect(Collectors.toList())
        );
    }

    /**
     * Resets the view to initial state
     */
    protected void resetViewStates() {
        txtProjectName.setText(null);
        cbProjectFlavor.setValue(null);
        cbCloudPlatformType.setValue(null);
    }

    //================================================================================================================
    // anonymous classes that will manage the enable/disable states of project flavor list items
    //================================================================================================================

    protected static class ClassicProjectAware extends ProjectTypeObserver {
        public ClassicProjectAware(ObjectProperty<ProjectFlavor> projectType) {
            super(projectType);
        }

        @Override
        public boolean computeValue() {
            return getCurrentProjectType() != null && getCurrentProjectType() != ProjectFlavor.CLASSIC;
        }
    }

    protected static class CloudProjectAware extends ProjectTypeObserver {

        public CloudProjectAware(ObjectProperty<ProjectFlavor> projectType) {
            super(projectType);
        }

        @Override
        public boolean computeValue() {
            return getCurrentProjectType() == ProjectFlavor.CLASSIC;
        }
    }

    protected abstract static class ProjectTypeObserver extends BooleanBinding {

        private ObjectProperty<ProjectFlavor> projectType;

        public ProjectTypeObserver(ObjectProperty<ProjectFlavor> projectType) {
            bind(projectType);
            this.projectType = projectType;
        }

        protected ProjectFlavor getCurrentProjectType() {
            return projectType.get();
        }
    }
}
