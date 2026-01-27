package de.qaware.ekg.awb.project.api;

import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.RepositoryClientAware;

import java.util.List;

/**
 * Interface that represents services that provide CRUD actions for {@link Project} instances
 * and a query API to fetch specific one.
 * The service will always work with context of a specific repository. Projects that are stored
 * in another repository have to fetched with another service instance.
 */
public interface ProjectDataAccessService extends RepositoryClientAware {

    /**
     * Return a list of all {@link Project} instances that exists in the EKG repository
     * this service is bind to. The returned project instance will be sorted by the name
     * in acceding order.
     *
     * @return a sorted list of Project instances
     */
    List<Project> listProjects();

    /**
     * Return the result of the queried project with project name as primary filter.
     * The returned list will always have 0 or 1 {@link Project} instance because project
     * names must be unique per repository.
     *
     * @param projectName the name of the project the caller is looking for
     * @param searchRemoteProjects true if only project of type 'SPLIT_SOURCE_PROJECT' should returned,
     *                             false for project with time series data in EKG repositories
     *
     * @see de.qaware.ekg.awb.project.api.model.ProjectType for further information
     * @return a list with zero or one project instance
     */
    List<Project> queryProjects(String projectName, boolean searchRemoteProjects);

    /**
     * Stores the given {@link Project} instance with all it's domain properties to the
     * persistence layer and return the persisted bean that also contains the id as of the entity
     * primary key provided by the repository.
     *
     * @param project the project that should persisted
     * @return the persisted project fulfilled with the technical id
     */
    Project persistProject(Project project);

    /**
     * Persists the given project attributes to the repository
     * and checks if the also the name has change and other resources must be updated
     * that reference to it.
     * This is the big difference to the persistProject() method that just store the
     * project itself without awareness for existing data.
     *
     * -- Not implemented yet --
     *
     * @param project a project that already exists in the database and should be updated
     * @return the persisted project fulfilled with the new technical id
     */
    Project updateExistingProject(Project project);

    /**
     * Deletes the given TimeSeries from the persisted types.
     *
     * @param seriesName the name of the series series.
     */
    void deleteProjectByName(String seriesName);

}
