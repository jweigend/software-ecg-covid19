package de.qaware.ekg.awb.project.ui.projectselector;

import de.qaware.ekg.awb.project.api.ProjectConfiguration;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectSelector;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectTimeSeriesType;
import de.qaware.ekg.awb.sdk.awbapi.project.TargetProjectConfig;
import de.qaware.ekg.awb.sdk.awbapi.repository.AliasMappingProvider;
import de.qaware.ekg.awb.sdk.awbapi.repository.Repository;
import de.qaware.ekg.awb.sdk.core.lookup.EkgDefault;
import de.qaware.ekg.awb.sdk.core.resourceloader.CdiFxmlLoader;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import javax.enterprise.inject.Default;
import java.util.HashSet;
import java.util.Set;

/**
 * An UI component to create or select a workbench projects.
 * This component is designed to use in other complex dialogs like import dialogs
 * to specify the target project of the imported time-series.
 */
@Default
@EkgDefault
public class ProjectSelectorPane extends GridPane implements ProjectSelector {

    /**
     * The controller that manages the view of ProjectSelector
     */
    private ProjectSelectorController projectSelectorController;

    private ProjectConfigListenerAdapter projectConfigListenerAdapter;

    /**
     * Constructs a new instance of ProjectSelector
     */
    public ProjectSelectorPane() {
        projectSelectorController = CdiFxmlLoader.loadView("ProjectSelector.fxml", this).getController();
        projectConfigListenerAdapter = new ProjectConfigListenerAdapter(projectSelectorController.getViewModel().projectConfigurationProperty());
    }

    /**
     * Returns a BooleanBinding that can be used to control other control elements like enable/disable
     * submit button in a dialog.
     *
     * The BooleanBinding will provide a TRUE boolean if the ProjectSelector is able to serve a
     * valid ProjectConfiguration. For this the user has to select an existing project or fully specify a new one.
     *
     * @return a BooleanBinding that indicate if a valid ProjectConfiguration was specified or not
     */
    public BooleanBinding isValidProjectDefinitionProperty() {
        return projectSelectorController.getViewModel().isValidProjectConfigProperty();
    }

    /**
     * Returns the ProjectConfiguration entity the represents the selected or created projects
     * done by user input in the ProjectSelector
     *
     * @return an ProjectConfiguration instance or null if no valid project specified
     */
    public TargetProjectConfig getTargetProjectConfiguration() {
        return projectSelectorController.getSpecifiedProjectConfiguration();
    }

    /**
     * Submit changes done in the ProjectSelector.
     * This means if the user has specified a new project it will persisted
     */
    public void submitChanges() {
        projectSelectorController.submitChanges();
    }

    /**
     * Returns an ObjectProperty used to hold a EkgRepository instance
     * used to read and store projects.
     *
     * @return an ObjectProperty used to hold a types
     */
    public ObjectProperty<Repository> getRepositoryProperty() {
        return projectSelectorController.getViewModel().selectedRepositoryProperty();
    }

    /**
     * Returns a property that provides a the latest ProjectConfiguration that will updated
     * every time the user changes a setting or a the name (in case of new projects).
     * The the user interaction results in an invalid project config, the property stores null
     * as configuration.
     *
     * @return a property that provides the latest ProjectConfiguration or null in case of invalid configuration
     */
    public ObservableValue<TargetProjectConfig> getProjectConfigurationProperty() {
        return projectConfigListenerAdapter;
    }

    @Override
    public Node asJfxComponent() {
        return this;
    }

    /**
     * Set a boolean that indicate that the ProjectSelector provides the option to configure new
     * cloud projects/select existing cloud project or not
     *
     * @param cloudSupportEnabled TRUE if cloud typed projects available in the ProjectSelector or FALSE if not
     */
    @Override
    public void setCloudFlavorSupportEnabled(boolean cloudSupportEnabled) {
        this.projectSelectorController.getViewModel().setCloudSupportEnabled(cloudSupportEnabled);
    }

    /**
     * Sets a boolean flag if the imported data will support classic view flavor or not.
     * This will stored in the project settings if new created.
     *
     * @param classicSupportEnabled TRUE if the the imported data provides required data for class view flavor
     */
    @Override
    public void setClassicFlavorSupportEnabled(boolean classicSupportEnabled) {
        this.projectSelectorController.getViewModel().setClassicSupportEnabled(classicSupportEnabled);
    }

    public void setSupportedTimeSeriesType(ProjectTimeSeriesType supportedProjectType) {
        // todo skr
    }

    public void enableSplitSourceProjectSupport(boolean remoteProjectsEnabled) {
        this.projectSelectorController.getViewModel().remoteProjectInactiveProperty().set(!remoteProjectsEnabled);
    }

    public void enableLocalProjectSupport(boolean localProjectsEnabled) {
        this.projectSelectorController.getViewModel().localProjectInactiveProperty().set(!localProjectsEnabled);
    }


    public void setImporterId(String importerId) {
        this.projectSelectorController.getViewModel().setImporterId(importerId);
    }

    public void setImporterAliasMappingProvider(AliasMappingProvider dimensionMappingAliasProvider) {
        this.projectSelectorController.getViewModel().importerAliasMappingProviderProperty().set(dimensionMappingAliasProvider);
    }

    public void updateState() {
        projectSelectorController.updateState();
    }

    private static class ProjectConfigListenerAdapter implements ObservableValue<TargetProjectConfig>, ChangeListener<ProjectConfiguration>, InvalidationListener {

        private Set<ChangeListener<? super TargetProjectConfig>> changeListeners = new HashSet<>();
        private Set<InvalidationListener> invalidationListeners = new HashSet<>();

        private ObjectProperty<ProjectConfiguration> projectConfigurationProperty;

        private ProjectConfigListenerAdapter(ObjectProperty<ProjectConfiguration> projectConfigProperty) {
            this.projectConfigurationProperty = projectConfigProperty;
            projectConfigurationProperty.addListener((ChangeListener<ProjectConfiguration>) this);
            projectConfigurationProperty.addListener((InvalidationListener) this);
        }

        @Override
        public void addListener(ChangeListener<? super TargetProjectConfig> changeListener) {
            changeListeners.add(changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super TargetProjectConfig> changeListener) {
            changeListeners.remove(changeListener);
        }

        @Override
        public TargetProjectConfig getValue() {
            return projectConfigurationProperty.get();
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            invalidationListeners.add(invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            invalidationListeners.remove(invalidationListener);
        }

        @Override
        public void changed(ObservableValue<? extends ProjectConfiguration> observableValue,
                            ProjectConfiguration oldConfig, ProjectConfiguration newConfig) {

            for (ChangeListener<? super TargetProjectConfig> listener : changeListeners) {
                listener.changed(observableValue, oldConfig, newConfig);
            }
        }

        @Override
        public void invalidated(Observable observable) {
            for (InvalidationListener listener : invalidationListeners) {
                listener.invalidated(observable);
            }
        }
    }
}
