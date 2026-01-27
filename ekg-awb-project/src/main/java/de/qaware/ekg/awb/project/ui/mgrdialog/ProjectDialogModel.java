package de.qaware.ekg.awb.project.ui.mgrdialog;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.repository.Repository;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * The base model that stores all projects attributes and validation logic
 * that are used by each dialog that derives from the ProjectDialog.
 */
public class ProjectDialogModel {

    /**
     * the currently selected types (will do outside in RepositorySelector UI component)
     */
    protected final ObjectProperty<Repository> selectedRepository = new SimpleObjectProperty<>();

    /**
     * The selected view flavor. One of CLASSIC, HYBRID, CLOUD NATIVE
     */
    protected final ObjectProperty<ProjectFlavor> projectFlavor = new SimpleObjectProperty<>();

    /**
     * Property that holds the currently selected type of cloud platforms (NONE, OPEN_SHIFT, KUBERNETES, OTHER)
     */
    protected final ObjectProperty<CloudPlatformType> cloudPlatformType = new SimpleObjectProperty<>();

    /**
     * The currently specified project name in the text-box for new projects. This property doesn't store
     * the project name that can selected from the list of existing projects!
     */
    protected final StringProperty projectName = new SimpleStringProperty("");

    /**
     * A set of project names that exists in the current selected repository.
     */
    protected final SetProperty<String> projectNames = new SimpleSetProperty<>(FXCollections.observableSet());

    /**
     * A boolean binding that will be true if the defined project name doesn't already exists
     * in the selected repository.
     */
    protected final BooleanBinding projectNameUnique = getIsUniqueProjectNameProperty();


    //=================================================================================================================
    // validation logic defined as BooleanBinding's
    //=================================================================================================================

    protected BooleanBinding getIsValidProjectDefinitionProperty() {
        return new BooleanBinding() {

            {
                bind(projectName, projectNameUnique, cloudPlatformType, projectFlavor);
            }

            @Override
            protected boolean computeValue() {
                return StringUtils.isNotBlank(getProjectName())
                        && !projectNameUnique.get()
                        && getCloudPlatformType() != null
                        && getProjectFlavor() != null;
            }
        };
    }

    protected BooleanBinding getIsUniqueProjectNameProperty() {
        return new BooleanBinding() {

            {
                bind(projectName, ProjectDialogModel.this.projectNames);
            }

            @Override
            protected boolean computeValue() {
                return projectNames.contains(projectName.get());
            }
        };
    }

    //=================================================================================================================
    // Accessor API of the CreateProjectDialogModel
    //=================================================================================================================

    public Repository getSelectedRepository() {
        return selectedRepository.get();
    }

    public void setSelectedRepository(EkgRepository repository) {
        selectedRepository.set(repository);
    }

    public ProjectFlavor getProjectFlavor() {
        return projectFlavor.get();
    }

    public ObjectProperty<ProjectFlavor> projectFlavorProperty() {
        return projectFlavor;
    }

    public CloudPlatformType getCloudPlatformType() {
        return cloudPlatformType.get();
    }

    public ObjectProperty<CloudPlatformType> cloudPlatformTypeProperty() {
        return cloudPlatformType;
    }

    public String getProjectName() {
        return projectName.get();
    }

    public StringProperty projectNameProperty() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName.set(projectName);
    }

    public BooleanBinding projectNameUniqueProperty() {
        return projectNameUnique;
    }

    public void setProjectNames(Collection<String> newProjectNames) {
        projectNames.clear();

        //noinspection UseBulkOperation (addAll is unsupported)
        newProjectNames.forEach(projectNames::add);
    }
}
