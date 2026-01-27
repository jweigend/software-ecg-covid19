package de.qaware.ekg.awb.project.api;

import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.project.TargetProjectConfig;

import java.util.Objects;

/**
 * Entity that represents a Software-EKG workbench project configuration
 * primary used to specify target for imports including additional settings to control the process
 */
public class ProjectConfiguration extends TargetProjectConfig {

    private Project project;

    private ProjectFlavor projectFlavor;

    private CloudPlatformType cloudPlatformType;

    private boolean overrideExistingData = true;

    //=================================================================================================================
    // various constructors
    //=================================================================================================================

    /**
     * Constructs a new instance of ProjectConfiguration based on the
     * given project attributes given as dedicated parameters.
     *
     * @param projectName the name of the project
     * @param projectFlavor the supported view flavor of the project like Classic or Hybrid
     * @param cloudPlatformType the type of the cloud platform if any
     * @param isRemoteProject a boolean flag if the project used an remote repository for time series or not
     */
    public ProjectConfiguration(String projectName, ProjectFlavor projectFlavor,
                                CloudPlatformType cloudPlatformType, boolean isRemoteProject) {

        this.project = new Project(projectName, "", projectFlavor.toString(),
                cloudPlatformType.toString(), isRemoteProject);
    }

    /**
     * Constructs a new instance of ProjectConfiguration based on the
     * project and it's attributes plus.
     *
     * @param project the project that will used as template for the configuration
     * @param overrideExistingData a boolean flag that controls if already existing time series data will overwritten
     */
    public ProjectConfiguration(Project project, boolean overrideExistingData) {
        this.project = project;
        this.projectFlavor = project.getProjectFlavor();
        this.cloudPlatformType = project.getCloudPlatformType();
        this.overrideExistingData = overrideExistingData;
    }


    //=================================================================================================================
    // API of this configuration class
    //=================================================================================================================


    public Project getProjectEntity() {
        return project;
    }

    public String getProjectName() {
        return project.getName();
    }

    public String getProjectDescription() {
        return project.getDescription();
    }

    public ProjectFlavor getProjectFlavor() {
        return project.getProjectFlavor();
    }

    public CloudPlatformType getCloudPlatformType() {
       return project.getCloudPlatformType();
    }

    public boolean doesOverrideExistingData() {
        return overrideExistingData;
    }

    @Override
    public boolean isNewProject() {
        return project.getId() == null;
    }

    public ProjectConfiguration setOverrideExistingData(boolean overrideExistingData) {
        this.overrideExistingData = overrideExistingData;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectConfiguration)) {
            return false;
        }
        ProjectConfiguration that = (ProjectConfiguration) o;
        return overrideExistingData == that.overrideExistingData &&
                Objects.equals(project, that.project) &&
                projectFlavor == that.projectFlavor &&
                cloudPlatformType == that.cloudPlatformType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, projectFlavor, cloudPlatformType, overrideExistingData);
    }
}
