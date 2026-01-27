package de.qaware.ekg.awb.project.ui.projectselector;

import com.google.common.collect.Lists;
import de.qaware.ekg.awb.project.api.ProjectConfiguration;
import de.qaware.ekg.awb.project.api.model.AliasMappingType;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.project.api.model.ProjectType;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.repository.AliasMappingDefinition;
import de.qaware.ekg.awb.sdk.awbapi.repository.AliasMappingProvider;
import de.qaware.ekg.awb.sdk.awbapi.repository.FilterDimension;
import de.qaware.ekg.awb.sdk.awbapi.repository.Repository;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType.NONE;
import static de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType.OTHER;
import static de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor.*;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * The model of the ProjectSelector UI component
 */
@SuppressWarnings("unused")
public class ProjectSelectorModel {

    //---------------------------------------------------------------------------------------------------------------
    //  fixed default value lists
    //---------------------------------------------------------------------------------------------------------------

    private static final List<ProjectFlavor> FULL_PROJECT_TYPE_LIST = Lists.newArrayList(CLASSIC, HYBRID, CLOUD_NATIVE);

    //---------------------------------------------------------------------------------------------------------------
    //  instance model properties
    //---------------------------------------------------------------------------------------------------------------

    /**
     * flag that controls if the ProjectSelector provides the option to configure new
     * cloud projects/select existing cloud project or not
     */
    private final BooleanProperty cloudSupportEnabled = new SimpleBooleanProperty();

    /**
     * flag that controls if the ProjectSelector provides the option to configure new
     * classic projects/select existing classic project or not
     */
    private final BooleanProperty classicSupportEnabled = new SimpleBooleanProperty();

    /**
     * the currently selected types (will do outside in RepositorySelector UI component)
     */
    private final ObjectProperty<Repository> selectedRepository = new SimpleObjectProperty<>();

    /**
     * The boolean flag that indicates if the user choose to create a new project or use an
     * existing one. TRUE means "new project", FALSE "use existing"
     */
    private final BooleanProperty createNewProject = new SimpleBooleanProperty(true);

    /**
     * A list the three project flavors (CLASSIC, HYBRID, CLOUD NATIVE)
     */
    private final ListProperty<ProjectFlavor> projectFlavors = new SimpleListProperty<>(observableArrayList());

    /**
     * The selected view flavor. One of CLASSIC, HYBRID, CLOUD NATIVE
     */
    private final ObjectProperty<ProjectFlavor> projectFlavor = new SimpleObjectProperty<>();

    /**
     * A singleton instance of AliasMappingProvider that could be set from outside. If available
     * the project selector will use it to set state of according combo box and fill the project "alias" properties
     */
    private final ObjectProperty<AliasMappingProvider> importerAliasMappingProvider = new SimpleObjectProperty<>();

    /**
     * A boolean flag that indicates if "Remote Projects" are supported by the importer that uses this ProjectSelector
     * or not. TRUE mean Remote Projects are supported, FALSE not.
     */
    private final BooleanProperty remoteProjectInactive = new SimpleBooleanProperty(true);

    /**
     * A boolean flag that indicates if "Local Projects" are supported by the importer that uses this ProjectSelector
     * or not. TRUE mean Local Projects are supported, FALSE not.
     */
    private final BooleanProperty localProjectInactive = new SimpleBooleanProperty(true);

    /**
     * A boolean flag that indicates if existing time series data that belongs to the project
     * should overwritten at import process or not
     */
    private final BooleanProperty overwriteExistingData = new SimpleBooleanProperty(true);

    /**
     * Property that holds the currently selected AliasMappingType (EKG_STANDARD, IMPORTER_SPECIFIC, CUSTOM)
     */
    private final ObjectProperty<AliasMappingType> projectAliasMappingType = new SimpleObjectProperty<>();

    /**
     * Property that holds the currently selected project type (SPLIT_SOURCE_PROJECT vs LOCAL_PROJECT)
     */
    private final ObjectProperty<ProjectType> projectType = new SimpleObjectProperty<>(ProjectType.LOCAL_PROJECT);

    /**
     * Property that holds the currently selected type of cloud platforms (NONE, OPEN_SHIFT, KUBERNETES, OTHER)
     */
    private final ObjectProperty<CloudPlatformType> cloudPlatformType = new SimpleObjectProperty<>();

    /**
     * The currently specified project name in the text-box for new projects. This property doesn't store
     * the project name that can selected from the list of existing projects!
     */
    private final StringProperty projectName = new SimpleStringProperty("");


    //------------------------------------------------------------------------------------------------------------------
    // internal data that isn't displayed in the view but represents that state or configured data of this component
    //------------------------------------------------------------------------------------------------------------------

    private final SimpleObjectProperty<ProjectConfiguration> projectConfig = new SimpleObjectProperty<>( );

    private final BooleanProperty projectNameInvalid = new SimpleBooleanProperty(true);

    private final BooleanBinding isValidProjectConfig = getIsValidProjectDefinitionProperty();

    private String importerId;

    private AtomicBoolean updateInProgress = new AtomicBoolean(false);

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a new Instance of ProjectSelectorModel
     * and initialize the binding that will update ProjectConfiguration based
     * on different model property states.
     */
    public ProjectSelectorModel() {
        bindProjectConfigToModelProperties();
    }


    //---------------------------------------------------------------------------------------------------------------
    //  state switching API
    //---------------------------------------------------------------------------------------------------------------

    /**
     * Define the initial state of all necessary properties
     */
    public void initializeModelData() {
        projectFlavors.addAll(FULL_PROJECT_TYPE_LIST);
        projectAliasMappingType.set(AliasMappingType.EKG_STANDARD);
        cloudPlatformType.set(CloudPlatformType.NONE);
        projectFlavor.set(ProjectFlavor.CLASSIC);
    }

    public void ensureClassicPlatformType() {
        executeInSequence(() -> {
            if (cloudPlatformType.get() != null && cloudPlatformType.get() == NONE) {
                return;
            }

            cloudPlatformType.setValue(NONE);
        });
    }

    public void ensureCloudPlatformType() {
        executeInSequence(() -> {
            if (cloudPlatformType.get() != null && cloudPlatformType.get() != NONE) {
                return;
            }

            cloudPlatformType.setValue(OTHER);
        });
    }

    public void ensureClassicProjectType() {
        executeInSequence(() -> {
            if (projectFlavor.get() != null && projectFlavor.get() != CLOUD_NATIVE) {
                return;
            }

            projectFlavor.setValue(CLASSIC);
        });
    }

    public void ensureCloudProjectType() {
        executeInSequence(() -> {
            if (projectFlavor.get() != null && projectFlavor.get() != CLASSIC) {
                return;
            }

            projectFlavor.setValue(HYBRID);
        });
    }


    //---------------------------------------------------------------------------------------------------------------
    //  accessor API
    //---------------------------------------------------------------------------------------------------------------


    public Boolean getIsValidProjectConfig() {
        return isValidProjectConfig.get();
    }

    public BooleanBinding isValidProjectConfigProperty() {
        return isValidProjectConfig;
    }

    public boolean isOverwriteExistingData() {
        return overwriteExistingData.get();
    }

    public BooleanProperty overwriteExistingDataProperty() {
        return overwriteExistingData;
    }

    public void setOverwriteExistingData(boolean overwriteExistingData) {
        this.overwriteExistingData.set(overwriteExistingData);
    }

    public String getImporterId() {
        return importerId;
    }

    public void setImporterId(String importerId) {
        this.importerId = importerId;
    }

    public AliasMappingProvider getImporterAliasMappingProvider() {
        return importerAliasMappingProvider.get();
    }

    public ObjectProperty<AliasMappingProvider> importerAliasMappingProviderProperty() {
        return importerAliasMappingProvider;
    }

    public void setImporterAliasMappingProvider(AliasMappingProvider importerAliasMappingProvider) {
        this.importerAliasMappingProvider.set(importerAliasMappingProvider);
    }

    public Boolean getRemoteProjectInactive() {
        return remoteProjectInactive.get();
    }

    public BooleanProperty remoteProjectInactiveProperty() {
        return remoteProjectInactive;
    }

    public void setRemoteProjectInactive(Boolean remoteProjectInactive) {
        this.remoteProjectInactive.set(remoteProjectInactive);
    }

    public void setLocalProjectInactive(Boolean localProjectInactive) {
        this.localProjectInactive.set(localProjectInactive);
    }

    public Boolean getLocalProjectInactive() {
        return localProjectInactive.get();
    }

    public BooleanProperty localProjectInactiveProperty() {
        return localProjectInactive;
    }

    public ObjectProperty<AliasMappingType> aliasMappingTypeProperty() {
        return projectAliasMappingType;
    }

    public AliasMappingType getProjectAliasMappingType() {
        return projectAliasMappingType.get();
    }

    public void setProjectAliasMappingType(AliasMappingType projectAliasMappingType) {
        this.projectAliasMappingType.set(projectAliasMappingType);
    }

    public boolean getProjectNameInvalid() {
        return projectNameInvalid.get();
    }

    public BooleanProperty projectNameInvalidProperty() {
        return projectNameInvalid;
    }

    public void setProjectNameInvalid(boolean projectNameInvalid) {
        this.projectNameInvalid.set(projectNameInvalid);
    }

    /**
     * Serves a boolean that indicate that the ProjectSelector provides the option to configure new
     * cloud projects/select existing cloud project or not
     *
     * @return TRUE if cloud typed projects available in the ProjectSelector or FALSE if not
     */
    public boolean isCloudSupportEnabled() {
        return cloudSupportEnabled.get();
    }

    /**
     * Set a boolean that indicate that the ProjectSelector provides the option to configure new
     * cloud projects/select existing cloud project or not
     *
     * @param cloudSupportEnabled TRUE if cloud typed projects available in the ProjectSelector or FALSE if not
     */
    public void setCloudSupportEnabled(boolean cloudSupportEnabled) {
        this.cloudSupportEnabled.set(cloudSupportEnabled);
    }

    public boolean isClassicSupportEnabled() {
        return classicSupportEnabled.get();
    }

    public BooleanProperty classicSupportEnabledProperty() {
        return classicSupportEnabled;
    }

    public void setClassicSupportEnabled(boolean classicSupportEnabled) {
        this.classicSupportEnabled.set(classicSupportEnabled);
    }

    public BooleanProperty cloudSupportEnabledProperty() {
        return cloudSupportEnabled;
    }

    public Repository getSelectedRepository() {
        return selectedRepository.get();
    }

    public ObjectProperty<Repository> selectedRepositoryProperty() {
        return selectedRepository;
    }

    public void setSelectedRepository(EkgRepository selectedRepository) {
        this.selectedRepository.set(selectedRepository);
    }

    public ProjectType getProjectType() {
        return projectType.get();
    }

    public ObjectProperty<ProjectType> projectTypeProperty() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType.set(projectType);
    }

    public ProjectConfiguration getProjectConfiguration() {
        return projectConfig.get();
    }

    public SimpleObjectProperty<ProjectConfiguration> projectConfigurationProperty() {
        return projectConfig;
    }

    public String getProjectName() {
        return projectName.get();
    }

    public StringProperty projectNameProperty() {
        return projectName;
    }

    public Boolean isNewProject() {
        return createNewProject.get();
    }

    public BooleanProperty createNewProjectProperty() {
        return createNewProject;
    }

    public ProjectFlavor getProjectFlavor() {
        return projectFlavor.get();
    }


    public void setProjectFlavor(ProjectFlavor projectFlavor) {
        this.projectFlavor.setValue(projectFlavor);
    }

    public void setCloudPlatformType(CloudPlatformType cloudPlatformType) {
        this.cloudPlatformType.set(cloudPlatformType);
    }

    public ObjectProperty<ProjectFlavor> projectFlavorProperty() {
        return projectFlavor;
    }

    public ObservableList<ProjectFlavor> getProjectFlavors() {
        return projectFlavors.get();
    }

    public ListProperty<ProjectFlavor> projectFlavorsProperty() {
        return projectFlavors;
    }

    public CloudPlatformType getCloudPlatformType() {
        return cloudPlatformType.get();
    }

    public ObjectProperty<CloudPlatformType> cloudPlatformTypeProperty() {
        return cloudPlatformType;
    }

    //---------------------------------------------------------------------------------------------------------------
    //  internal helpers and structures
    //---------------------------------------------------------------------------------------------------------------

    private BooleanBinding getIsValidProjectDefinitionProperty() {
        return new BooleanBinding() {

            {
                bind(projectName, projectNameInvalid, projectConfig, createNewProject);
            }

            @Override
            protected boolean computeValue() {
                if (isNewProject()) {
                    return StringUtils.isNotBlank(getProjectName())
                            && !projectNameInvalid.get()
                            && getCloudPlatformType() != null
                            && getProjectFlavor() != null;
                }

                return projectConfig.get() != null;
            }
        };
    }

    private void bindProjectConfigToModelProperties() {
        cloudSupportEnabled.addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                projectFlavor.set(ProjectFlavor.CLASSIC);
                projectFlavors.clear();
                projectFlavors.add(CLASSIC);
                projectFlavor.set(ProjectFlavor.CLASSIC);
            }
        }));

        isValidProjectConfig.addListener((observable, oldValue, isValid) -> {
            if (!isValid) {
                projectConfig.setValue(null);
                return;
            }

            if (projectName.get() != null || projectConfig.get() == null
                    || !StringUtils.isNotBlank(projectConfig.get().getProjectName())) {
                if (projectName.get() != null) {
                    projectConfig.setValue(new ProjectConfiguration(createProjectEntity(), overwriteExistingData.get()));
                }
            }
        });

        // update project config everytime one of the UI components gets updated
        projectName.addListener(new ProjectSetupChangeListener());
        projectType.addListener(new ProjectSetupChangeListener());
        projectFlavor.addListener(new ProjectSetupChangeListener());
        cloudPlatformType.addListener(new ProjectSetupChangeListener());
        overwriteExistingData.addListener(new ProjectSetupChangeListener());
        createNewProject.addListener(new ProjectSetupChangeListener());
    }


    private Project createProjectEntity() {

        Project project =  new Project(
                getProjectName(),
                "",
                getProjectFlavor().toString(),
                getCloudPlatformType().toString(),
                getProjectType().equals(ProjectType.SPLIT_SOURCE_PROJECT));


        if (getImporterAliasMappingProvider() != null) {
            AliasMappingDefinition definition = getImporterAliasMappingProvider().getAliasMappingDefinition();

            project.setImporterId(getImporterId());
            project.setDimensionAliasHostGroup(definition.getAliasLabel(FilterDimension.HOST_GROUP));
            project.setDimensionAliasHost(definition.getAliasLabel(FilterDimension.HOST));
            project.setDimensionAliasNamespace(definition.getAliasLabel(FilterDimension.NAMESPACE));
            project.setDimensionAliasService(definition.getAliasLabel(FilterDimension.SERVICE));
            project.setDimensionAliasPod(definition.getAliasLabel(FilterDimension.POD));
            project.setDimensionAliasContainer(definition.getAliasLabel(FilterDimension.CONTAINER));
            project.setDimensionAliasMeasurement(definition.getAliasLabel(FilterDimension.MEASUREMENT));
            project.setDimensionAliasProcess(definition.getAliasLabel(FilterDimension.PROCESS));
            project.setDimensionAliasMetricGroup(definition.getAliasLabel(FilterDimension.METRIC_GROUP));
            project.setDimensionAliasMetricName(definition.getAliasLabel(FilterDimension.METRIC_NAME));
        }

        project.setImporterId(getImporterId());

        return project;
    }

    private void executeInSequence(Sequence sequence) {
        if (!updateInProgress.compareAndSet(false, true)) {
            return;
        }

        try {
            sequence.call();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }

        updateInProgress.set(false);
    }


    private interface Sequence {
        void call();
    }


    private class ProjectSetupChangeListener implements InvalidationListener {

        @Override
        public void invalidated(Observable observable) {
            if (!isValidProjectConfig.get()) {
                projectConfig.setValue(null);
                return;
            }

            projectConfig.setValue(new ProjectConfiguration(createProjectEntity(), overwriteExistingData.get()));
        }
    }
}
