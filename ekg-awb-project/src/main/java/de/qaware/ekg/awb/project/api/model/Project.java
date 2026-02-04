//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.project.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;
import de.qaware.ekg.awb.sdk.awbapi.project.CloudPlatformType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import static de.qaware.ekg.awb.repository.api.schema.EkgSchemaField.*;


/**
 * Bean the represents the project construct of the EKG Workbench
 */
public class Project implements Comparable<Project>, Serializable, de.qaware.ekg.awb.sdk.awbapi.project.Project {

    public static final Project DEFAULT = new Project("*", "DEFAULT", "CLASSIC", "NONE");

    /**
     * The fixed document type for multi-datatype tables that specifies that
     * this bean(document) is a project.
     */
    @PersistedField(DOC_TYPE)
    private String type = DocumentType.PROJECT.name();

    /**
     * The unique id of the project specified only than
     * the project is persisted
     */
    @PersistedField(ID)
    private String id;

    /**
     * The name of the project (should be unique)
     */
    @PersistedField(PROJECT_NAME)
    private String name;

    /**
     * A description text that provides more details to the project
     */
    @PersistedField(PROJECT_DESCRIPTION)
    private String description;

    /**
     * A string representation of the ProjectType that can be null or
     * one of the following strings:
     * <ul>
     *     <li>CLASSIC</li>
     *     <li>HYBRID</li>
     *     <li>CLOUD_NATIVE</li>
     * </ul>
     */
    @PersistedField(PROJECT_FLAVOR)
    private String projectFlavor;

    /**
     * A boolean flag that indicates if the project is a local or
     * a remote project
     */
    @JsonProperty(value="isSplitSourceProject")
    @PersistedField(PROJECT_USE_SPLIT_SOURCE)
    private Boolean isSplitSourceProject;

    /**
     * A string representation of the CloudPlatformType that can be null or
     * one of the following strings:
     * <ul>
     *     <li>NONE</li>
     *     <li>OPEN_SHIFT</li>
     *     <li>KUBERNETES</li>
     *     <li>OTHER</li>
     * </ul>
     */
    @PersistedField(PROJECT_CN_TYPE)
    private String cloudPlatformType;

    /**
     * The id of the importer that has initial created this
     * project (creation in import dialog)
     *
     * The id can used to filter projects in import dialogs if a specific importer
     * enforces to import new only in projects with it's own alias mapping.
     */
    @PersistedField(PROJECT_IMPORTER_ID)
    private String importerId;

    /**
     * the optional alias names if project specific mapping is in use
     */
    @PersistedField(PROJECT_DIM_ALIAS_HOSTGROUP)
    private String dimensionAliasHostGroup;

    @PersistedField(PROJECT_DIM_ALIAS_HOST)
    private String dimensionAliasHost;

    @PersistedField(PROJECT_DIM_ALIAS_NAMESPACE)
    private String dimensionAliasNamespace;

    @PersistedField(PROJECT_DIM_ALIAS_SERVICE)
    private String dimensionAliasService;

    @PersistedField(PROJECT_DIM_ALIAS_POD)
    private String dimensionAliasPod;

    @PersistedField(PROJECT_DIM_ALIAS_CONTAINER)
    private String dimensionAliasContainer;

    @PersistedField(PROJECT_DIM_ALIAS_MEASUREMENT)
    private String dimensionAliasMeasurement;

    @PersistedField(PROJECT_DIM_ALIAS_PROCESS)
    private String dimensionAliasProcess;

    @PersistedField(PROJECT_DIM_ALIAS_METRICGROUP)
    private String dimensionAliasMetricGroup;

    @PersistedField(PROJECT_DIM_ALIAS_METRICNAME)
    private String dimensionAliasMetricName;

    /**
     * Empty constructor to enable persistence layer
     * to instance anf fill this entity via reflection
     */
    public Project() {
        // no op
    }

    /**
     * Constructs a new Project instance
     *
     * @param projectName the alias name of the project
     */
    public Project(String projectName) {
        this.name = projectName;
        this.description = "";
        this.projectFlavor = "";
        this.cloudPlatformType = "";
        this.isSplitSourceProject = false;
    }

    /**
     * Constructs a new Project instance based
     * on the given data
     *
     * @param name the alias name of the project
     * @param description the description of the project
     * @param projectFlavor the project type
     * @param cloudPlatformType the cloud platform flavor in case of hybrid or cloud native projects, or none at classic
     */
    public Project(String name, String description, String projectFlavor, String cloudPlatformType) {
        this.name = name;
        this.description = description;
        this.projectFlavor = projectFlavor;
        this.cloudPlatformType = cloudPlatformType;
        this.isSplitSourceProject = false;
    }


    public Project(String name, String description, String projectFlavor, String cloudPlatformType, boolean isSplitSourceProject) {
        this(name, description, projectFlavor, cloudPlatformType);

        this.isSplitSourceProject = isSplitSourceProject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Project setId(String id) {
        this.id = id;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Project setDescription(String description) {
        this.description = description;
        return this;
    }

    public ProjectFlavor getProjectFlavor() {
        return projectFlavor != null ? ProjectFlavor.valueOf(projectFlavor) : null;
    }

    public Project setProjectFlavor(ProjectFlavor projectFlavor) {
        this.projectFlavor = projectFlavor.toString();
        return this;
    }

    public CloudPlatformType getCloudPlatformType() {
        return cloudPlatformType != null ? CloudPlatformType.valueOf(cloudPlatformType) : null;
    }

    public Project setCloudPlatformType(CloudPlatformType cloudPlatformType) {
        this.cloudPlatformType = cloudPlatformType.toString();
        return this;
    }

    public Project setCloudPlatformType(String cloudPlatformType) {
        this.cloudPlatformType = cloudPlatformType;
        return this;
    }

    public String getImporterId() {
        return importerId;
    }

    public Project setImporterId(String importerId) {
        this.importerId = importerId;
        return this;
    }

    public boolean useSplitSource() {
        return isSplitSourceProject == null ? false : isSplitSourceProject;
    }

    public Project setIsSplitSourceProject(boolean remoteProjectType) {
        isSplitSourceProject = remoteProjectType;
        return this;
    }

    public String getDimensionAliasHostGroup() {
        return dimensionAliasHostGroup;
    }

    public void setDimensionAliasHostGroup(String dimensionAliasHostGroup) {
        this.dimensionAliasHostGroup = dimensionAliasHostGroup;
    }

    public String getDimensionAliasHost() {
        return dimensionAliasHost;
    }

    public void setDimensionAliasHost(String dimensionAliasHost) {
        this.dimensionAliasHost = dimensionAliasHost;
    }

    public String getDimensionAliasNamespace() {
        return dimensionAliasNamespace;
    }

    public void setDimensionAliasNamespace(String dimensionAliasNamespace) {
        this.dimensionAliasNamespace = dimensionAliasNamespace;
    }

    public String getDimensionAliasService() {
        return dimensionAliasService;
    }

    public void setDimensionAliasService(String dimensionAliasService) {
        this.dimensionAliasService = dimensionAliasService;
    }

    public String getDimensionAliasPod() {
        return dimensionAliasPod;
    }

    public void setDimensionAliasPod(String dimensionAliasPod) {
        this.dimensionAliasPod = dimensionAliasPod;
    }

    public String getDimensionAliasContainer() {
        return dimensionAliasContainer;
    }

    public void setDimensionAliasContainer(String dimensionAliasContainer) {
        this.dimensionAliasContainer = dimensionAliasContainer;
    }

    public String getDimensionAliasMeasurement() {
        return dimensionAliasMeasurement;
    }

    public void setDimensionAliasMeasurement(String dimensionAliasMeasurement) {
        this.dimensionAliasMeasurement = dimensionAliasMeasurement;
    }

    public String getDimensionAliasProcess() {
        return dimensionAliasProcess;
    }

    public void setDimensionAliasProcess(String dimensionAliasProcess) {
        this.dimensionAliasProcess = dimensionAliasProcess;
    }

    public String getDimensionAliasMetricGroup() {
        return dimensionAliasMetricGroup;
    }

    public void setDimensionAliasMetricGroup(String dimensionAliasMetricGroup) {
        this.dimensionAliasMetricGroup = dimensionAliasMetricGroup;
    }

    public String getDimensionAliasMetricName() {
        return dimensionAliasMetricName;
    }

    public void setDimensionAliasMetricName(String dimensionAliasMetricName) {
        this.dimensionAliasMetricName = dimensionAliasMetricName;
    }

    @Override
    public int compareTo(Project o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return StringUtils.isBlank(name) ? "Project([BLANK])" : "Project(name='" + name + "')";
    }

}
