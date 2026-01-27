package de.qaware.ekg.awb.project.ui.projectselector;

import afester.javafx.svg.SvgLoader;
import de.qaware.ekg.awb.common.ui.bindings.Bindings;
import de.qaware.ekg.awb.project.api.ProjectConfiguration;
import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.model.AliasMappingType;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.project.api.model.ProjectType;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.repository.Repository;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import de.qaware.ekg.awb.sdk.importer.ui.skins.EkgComboBoxListViewSkin;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static de.qaware.ekg.awb.project.api.model.AliasMappingType.EKG_STANDARD;
import static de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType.NONE;
import static de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor.*;
import static de.qaware.ekg.awb.sdk.importer.ui.skins.EkgComboBoxListViewSkin.CellBehaviorResource;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * The controller of the ProjectSelector UI component
 */
public class ProjectSelectorController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectSelectorController.class);

    private static final String COMMON_ICONS_ABSOLUTE_PATH = "/de/qaware/ekg/awb/project/ui/common-icons";

    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    private final Map<CloudPlatformType, CellBehaviorResource> cloudPlatformResourceMap = new HashMap<>();

    private final Map<AliasMappingType, CellBehaviorResource> aliasMappingResourceMap = new HashMap<>();

    private final Map<ProjectFlavor, CellBehaviorResource> projectFlavorResourceMap = new HashMap<>();

    private final Map<ProjectType, CellBehaviorResource> projectTypeResourceMap = new HashMap<>();

    @FXML
    private GridPane projectSelector;

    @FXML
    private CheckBox cbCreateProject;

    @FXML
    private GridPane gpExistingProject;

    @FXML
    private ComboBox<Project> cbProject;

    @FXML
    private GridPane gpNewProject;

    @FXML
    private TextField txtProjectName;

    @FXML
    private ComboBox<ProjectFlavor> cbProjectFlavor;

    @FXML
    private ComboBox<CloudPlatformType> cbCloudPlatformType;

    @FXML
    private ComboBox<AliasMappingType> cbDimensionAliasSetting;

    @FXML
    private ComboBox<ProjectType> cbProjectType;

    @FXML
    private RadioButton rbtgOverwriteSeriesRW;

    @FXML
    private RadioButton rbtgOverwriteSeriesSKIP;

    @FXML
    private Group warningIcon;

    @FXML
    private Group errorBubble;

    /**
     * The underling view model of the ProjectSelector
     */
    private ProjectSelectorModel viewModel = new ProjectSelectorModel();

    private Map<Repository, ProjectDataAccessService> serviceCache = new HashMap<>();

    //------------------------------------------------------------------------------------------------------------------
    //  Initializable API
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initializeViewStyles();
        initializeViewModelBinding();
        initializeViewState();
        initializeDefaultBehavior();

        viewModel.initializeModelData();
    }



    //------------------------------------------------------------------------------------------------------------------
    //  controller API
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the underling view model of the ProjectSelector
     * used by this instance of ProjectSelectorController
     *
     * @return the ProjectSelector view model
     */
    public ProjectSelectorModel getViewModel() {
        return viewModel;
    }


    /**
     * Returns the {@link ProjectConfiguration} if a valid project/project setup is defined.
     * Otherwise null will returned. The configuration will include an existing (from
     * database loaded) project or a new created one that based on the user input in the dialog.
     *
     * @return a ProjectConfiguration instance or null in not available
     */
    public ProjectConfiguration getSpecifiedProjectConfiguration() {
        return viewModel.getProjectConfiguration();
    }

    /**
     * Persist a new specified project to types if necessary.
     * If user select an existing project nothing will happened.
     */
    public void submitChanges() {

        try {
            if (!viewModel.isNewProject()) {
                return;
            }

            Repository repository = viewModel.getSelectedRepository();
            if (repository == null || !viewModel.isValidProjectConfigProperty().get()) {
                return;
            }

            Project project = getSpecifiedProjectConfiguration().getProjectEntity();
            Project storedProject = getBoundedService(repository).persistProject(project);
            viewModel.projectConfigurationProperty().set(new ProjectConfiguration(storedProject,
                    rbtgOverwriteSeriesRW.isSelected()));

        } catch (Exception e) {
            LOGGER.error("", e);
            throw new IllegalStateException(e);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //  internal implementation of controller functionality
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Initialize the view styles that have to be configured programmatically.
     * (default styling should be define by style sheets)
     */
    private void initializeViewStyles() {
        cloudPlatformResourceMap.put(CloudPlatformType.NONE, new CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/none-icon.svg",
                new ClassicProjectAware(viewModel.projectFlavorProperty())));
        cloudPlatformResourceMap.put(CloudPlatformType.KUBERNETES, new CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/kubernetes-icon.svg",
                new CloudProjectAware(viewModel.projectFlavorProperty())));
        cloudPlatformResourceMap.put(CloudPlatformType.OPEN_SHIFT, new CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/openshift-icon.svg",
                new CloudProjectAware(viewModel.projectFlavorProperty())));
        cloudPlatformResourceMap.put(CloudPlatformType.OTHER, new CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/cloud-other-icon.svg",
                new CloudProjectAware(viewModel.projectFlavorProperty())));

        projectFlavorResourceMap.put(ProjectFlavor.CLASSIC, new CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/baremetal-icon.svg",
                new ReadOnlyBooleanWrapper(false)));
        projectFlavorResourceMap.put(ProjectFlavor.HYBRID, new CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/hybrid-icon.svg",
                new ReadOnlyBooleanWrapper(false)));
        projectFlavorResourceMap.put(ProjectFlavor.CLOUD_NATIVE, new CellBehaviorResource(COMMON_ICONS_ABSOLUTE_PATH + "/cloud-native-icon.svg",
                new ReadOnlyBooleanWrapper(false)));

        projectTypeResourceMap.put(ProjectType.LOCAL_PROJECT, new CellBehaviorResource("icons/project-type-local.svg",
                viewModel.localProjectInactiveProperty()));
        projectTypeResourceMap.put(ProjectType.SPLIT_SOURCE_PROJECT, new CellBehaviorResource("icons/project-type-remote.svg",
                viewModel.remoteProjectInactiveProperty()));

        aliasMappingResourceMap.put(AliasMappingType.EKG_STANDARD, new CellBehaviorResource("icons/dimension-mapping-ekgstandard.svg",
                viewModel.importerAliasMappingProviderProperty().isNotNull()));
        aliasMappingResourceMap.put(AliasMappingType.IMPORTER_SPECIFIC, new CellBehaviorResource("icons/dimension-mapping-importer.svg",
                viewModel.importerAliasMappingProviderProperty().isNull()));
        aliasMappingResourceMap.put(AliasMappingType.CUSTOM, new CellBehaviorResource("icons/dimension-mapping-user.svg",
                new ReadOnlyBooleanWrapper(true)));

        // create SVG icon and it it to an empty placeholder-group element on the left of the text field
        Group iconImage = new SvgLoader().loadSvg(getClass().getResourceAsStream(COMMON_ICONS_ABSOLUTE_PATH + "/warning-icon.svg"));
        iconImage.setScaleX(0.07);
        iconImage.setScaleY(0.07);
        warningIcon.getChildren().add(iconImage);

        // update the view style of textfield that contains the project name according the the current
        // validation state
        viewModel.projectNameInvalidProperty().addListener((observable, oldState, newState)
                -> txtProjectName.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, newState));
    }


    private void initializeDefaultBehavior() {

        // delete state every time the model dialog will open/reopen
        projectSelector.layoutBoundsProperty().addListener(observable -> updateState());

        // load the list of projects every time the user uncheck the check box "create new project"
        // --> effectively "use existing project"
        cbCreateProject.selectedProperty().addListener((observable, oldValue, isCreateNewProjectSelected) -> {
            if (isCreateNewProjectSelected || viewModel.getSelectedRepository() == null) {
                return;
            }

            updateProjectComboBox(viewModel.getSelectedRepository());
        });

        // set validation state of the text field that contains the project name by checking the list of
        // existing projects for duplicate names
        viewModel.projectNameInvalidProperty().bind(viewModel.selectedRepositoryProperty().isNotNull().and(
                new BooleanBinding() {

                    {bind(txtProjectName.textProperty(), cbProject.itemsProperty());}

                    @Override
                    protected boolean computeValue() {
                        final String specifiedProjectName = txtProjectName != null ? txtProjectName.getText() : "";

                        return cbProject.getItems().stream().map(Project::getName)
                                .anyMatch(name -> name.equals(specifiedProjectName));
                    }
                }
        ));

        warningIcon.visibleProperty().bind(viewModel.projectNameInvalidProperty());
        errorBubble.visibleProperty().bind(warningIcon.hoverProperty());

        viewModel.localProjectInactiveProperty().addListener((observable, oldState, localProjectIsInactive)
                -> cbProjectType.setValue(localProjectIsInactive ? ProjectType.SPLIT_SOURCE_PROJECT : ProjectType.LOCAL_PROJECT));

        viewModel.importerAliasMappingProviderProperty().addListener((observable, oldState, provider)
                -> cbDimensionAliasSetting.setValue(provider != null ? AliasMappingType.IMPORTER_SPECIFIC : AliasMappingType.EKG_STANDARD));

        viewModel.projectFlavorProperty().addListener((observable, oldProjectFlavor, newProjectFlavor) -> {
            if (newProjectFlavor == CLASSIC) {
                viewModel.ensureClassicPlatformType();
            } else if(newProjectFlavor == CLOUD_NATIVE || newProjectFlavor == HYBRID) {
                viewModel.ensureCloudPlatformType();
            }
        });

        viewModel.cloudPlatformTypeProperty().addListener((observable, oldCloudPlatformType, newCloudPlatformType) -> {
            if (newCloudPlatformType != null && newCloudPlatformType != NONE) {
                viewModel.ensureCloudProjectType();
            } else if(newCloudPlatformType == NONE) {
                viewModel.ensureClassicProjectType();
            }
        });

        // replace items of project selection combobox depending on types selection state and the count of projects
        viewModel.selectedRepositoryProperty().addListener((observable, oldValue, selectedRepository) -> {
            if (selectedRepository == null) {
                return;
            }

            updateProjectComboBox(selectedRepository);
        });

        // (de)active project selection depending on the types selection state and the count of available projects
        BooleanExpression expression = viewModel.selectedRepositoryProperty().isNull().or(new BooleanBinding() {
            { bind(cbProject.itemsProperty()); }

            @Override
            protected boolean computeValue() {
                return cbProject.itemsProperty().get().size() == 0;
            }
        });

        cbProject.disableProperty().bind(expression);
        rbtgOverwriteSeriesRW.disableProperty().bind(expression);
        rbtgOverwriteSeriesSKIP.disableProperty().bind(expression);
    }

    private void initializeViewModelBinding() {

        viewModel.projectTypeProperty().bindBidirectional(cbProjectType.valueProperty());
        viewModel.aliasMappingTypeProperty().bindBidirectional(cbDimensionAliasSetting.valueProperty());
        viewModel.createNewProjectProperty().bind(cbCreateProject.selectedProperty());
        viewModel.projectNameProperty().bind(txtProjectName.textProperty());
        viewModel.projectFlavorProperty().bindBidirectional(cbProjectFlavor.valueProperty());
        viewModel.cloudPlatformTypeProperty().bindBidirectional(cbCloudPlatformType.valueProperty());
        cbProject.getSelectionModel().selectedItemProperty().addListener((instance, oldProject, newProject) -> {
            if (cbCreateProject.isSelected() || newProject == null) {
                return;
            }

            ProjectConfiguration projectConf = new ProjectConfiguration(newProject, rbtgOverwriteSeriesRW.isSelected());
            viewModel.projectConfigurationProperty().set(projectConf);
        });
        viewModel.overwriteExistingDataProperty().bind(rbtgOverwriteSeriesRW.selectedProperty());
    }

    private void initializeViewState() {

        // addAndSum invalid pseudo class to text-field - to control the validation state
        txtProjectName.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);

        // dual project view (create or select)
        gpNewProject.visibleProperty().bind(cbCreateProject.selectedProperty());
        gpExistingProject.visibleProperty().bind(cbCreateProject.selectedProperty().not());
        cbProject.setPromptText("- Please choose a repository -");

        // radio button functionality
        ToggleGroup rbToggle = new ToggleGroup();
        rbtgOverwriteSeriesRW.setToggleGroup(rbToggle);
        rbtgOverwriteSeriesSKIP.setToggleGroup(rbToggle);

        // initialize both combo boxes
        initProjectFlavorCombobox();
        initFilterMappingCombobox();
        initProjectTypeCombobox();
        initCloudPlatformTypeCombobox();
    }

    private void initProjectFlavorCombobox() {
        cbProjectFlavor.setSkin(new EkgComboBoxListViewSkin<>(cbProjectFlavor, projectFlavorResourceMap, CLASSIC));
        Bindings.bindComboBox(cbProjectFlavor, viewModel.getProjectFlavors(),
                viewModel.projectFlavorProperty());
    }

    private void initFilterMappingCombobox() {
        cbDimensionAliasSetting.setSkin(
                new EkgComboBoxListViewSkin<>(cbDimensionAliasSetting, aliasMappingResourceMap, EKG_STANDARD));
        Bindings.bindComboBox(cbDimensionAliasSetting, FXCollections.observableArrayList(AliasMappingType.values()),
                viewModel.aliasMappingTypeProperty());
    }

    /**
     * Initialize the model-binding and view skins of the ProjectFlavor combobox
     */
    private void initProjectTypeCombobox() {
        ProjectType defaultProjectType = viewModel.localProjectInactiveProperty().get() ?
                ProjectType.SPLIT_SOURCE_PROJECT : ProjectType.LOCAL_PROJECT;

        cbProjectType.setSkin(new EkgComboBoxListViewSkin<>(cbProjectType, projectTypeResourceMap, defaultProjectType));
        Bindings.bindComboBox(cbProjectType, FXCollections.observableArrayList(ProjectType.values()),
                viewModel.projectTypeProperty());
    }

    /**
     * Initialize the model-binding and view skins of the CloudPlatformType combobox
     */
    private void initCloudPlatformTypeCombobox() {
       cbCloudPlatformType.setSkin(new EkgComboBoxListViewSkin<>(cbCloudPlatformType, cloudPlatformResourceMap, NONE));
       Bindings.bindComboBox(cbCloudPlatformType, FXCollections.observableArrayList(CloudPlatformType.values()),
                viewModel.cloudPlatformTypeProperty());
    }

    private void updateProjectComboBox(Repository selectedRepository) {
        cbProject.setItems(observableArrayList(getBoundedService(selectedRepository).listProjects()));

        if (cbProject.getItems().size() > 0) {
            cbProject.setEditable(false);
            cbProject.getSelectionModel().select(0);
        } else {
            cbProject.setEditable(true);
            cbProject.setValue(null);
            cbProject.getItems().clear();
            cbProject.setPromptText("- no projects available -");
        }
    }

    public void updateState() {
        txtProjectName.setText(null);
        cbCreateProject.setSelected(true);
        cbProjectType.setValue(viewModel.localProjectInactiveProperty().get() ? ProjectType.SPLIT_SOURCE_PROJECT : ProjectType.LOCAL_PROJECT);

        // set focus to project name text-field every time the modal window opens
        Platform.runLater(() -> txtProjectName.requestFocus());
    }

    //-------------------------------------------------------------------------------------------------------------

    private static class ClassicProjectAware extends ProjectTypeObserver {
        public ClassicProjectAware(ObjectProperty<ProjectFlavor> projectType) {
            super(projectType);
        }

        @Override
        public boolean computeValue() {
            return getCurrentProjectType() != null && getCurrentProjectType() != ProjectFlavor.CLASSIC;
        }
    }

    private static class CloudProjectAware extends ProjectTypeObserver {

        public CloudProjectAware(ObjectProperty<ProjectFlavor> projectType) {
            super(projectType);
        }

        @Override
        public boolean computeValue() {
            return getCurrentProjectType() == ProjectFlavor.CLASSIC;
        }
    }

    private abstract static class ProjectTypeObserver extends BooleanBinding {

        private ObjectProperty<ProjectFlavor> projectType;

        public ProjectTypeObserver(ObjectProperty<ProjectFlavor> projectType) {
            bind(projectType);
            this.projectType = projectType;
        }

        protected ProjectFlavor getCurrentProjectType() {
            return projectType.get();
        }
    }

    private ProjectDataAccessService getBoundedService(Repository repository) {

        ProjectDataAccessService service = serviceCache.get(repository);

        if (service == null) {
            serviceCache.put(repository, ServiceDiscovery.lookup(ProjectDataAccessService.class, repository));
        }

        return serviceCache.get(repository);
    }

}
