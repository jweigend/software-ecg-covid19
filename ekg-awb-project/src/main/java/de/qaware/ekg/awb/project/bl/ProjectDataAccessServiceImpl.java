package de.qaware.ekg.awb.project.bl;

import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryNotAvailableException;
import de.qaware.ekg.awb.repository.api.dataobject.delete.DeleteParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.sdk.awbapi.repository.Repository;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.core.events.AwbErrorEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.inject.Default;
import java.util.ArrayList;
import java.util.List;

import static de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory.exactFilter;
import static de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory.wildcardFilter;

/**
 * The default ProjectDataAccessServiceImpl implementation that will read and write project
 * data to a specify repository.
 */
@SuppressWarnings("unused") // class and constructors are used by CDI
@Default
public class ProjectDataAccessServiceImpl implements ProjectDataAccessService {

    private RepositoryClient client;

    private EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);

    public ProjectDataAccessServiceImpl() {
        // no op
    }

    public ProjectDataAccessServiceImpl(Repository repository) {
        if (!(repository instanceof EkgRepository)) {
            throw new IllegalArgumentException("The given repository instance '" + repository.getRepositoryName()
                    + "' isn't supported by the EKG AWB ProjectDataAccessService");
        }

        this.client = ((EkgRepository) repository).getRepositoryClient();
    }

    public void initializeService(RepositoryClient client) {
        this.client = client;
    }

    //=================================================================================================================
    // implementation of ProjectDataAccessService interface
    //=================================================================================================================

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.project.api.ProjectDataAccessService#listProjects()
     */
    @Override
    public List<Project> listProjects() {
        try {
            SearchParams searchParams = new SearchParams()
                    .withFilterQueries(
                            wildcardFilter(EkgSchemaField.PROJECT_NAME, "*"),
                            exactFilter(EkgSchemaField.DOC_TYPE, DocumentType.PROJECT.name()))
                    .withSortField(EkgSchemaField.PROJECT_NAME, SortField.SortMode.ASC);

            SearchResult<Project> searchResult = client.search(Project.class, searchParams);
            return searchResult.getRows();

        } catch (RepositoryNotAvailableException e) {
            EkgEventBus bus = EkgLookup.lookup(EkgEventBus.class);

            String errorMsg;

            if (e.getNotAvailableCause() == RepositoryNotAvailableException.Cause.HOST_NOT_AVAILABLE) {
                errorMsg = "Host of the EKG repository is not available at the moment. " +
                        "\nPlease check connection details or try again later.";
            } else {
                errorMsg = "Host of the EKG repository is available but the configured index does not exist. " +
                        "\nPlease check connection details.";
            }


            bus.publish(new AwbErrorEvent(this, errorMsg, e));

            return new ArrayList<>();

        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.project.api.ProjectDataAccessService#queryProjects(java.lang.String, boolean)
     */
    @Override
    public List<Project> queryProjects(String projectName, boolean searchRemoteProjects) {
        try {
            SearchParams searchParams = new SearchParams()
                    .withFilterQueries(
                            wildcardFilter(EkgSchemaField.PROJECT_NAME, projectName),
                            wildcardFilter(EkgSchemaField.PROJECT_USE_SPLIT_SOURCE, String.valueOf(searchRemoteProjects)),
                            exactFilter(EkgSchemaField.DOC_TYPE, DocumentType.PROJECT.name()))
                    .withSortField(EkgSchemaField.PROJECT_NAME, SortField.SortMode.ASC);

            SearchResult<Project> searchResult = client.search(Project.class, searchParams);
            return searchResult.getRows();

        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.project.api.ProjectDataAccessService#persistProject(Project)
     */
    @Override
    public Project persistProject(Project project) {
        try {
            client.add(project);
            client.commit();
            return project;
        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.project.api.ProjectDataAccessService#updateExistingProject(Project)
     */
    @Override
    public Project updateExistingProject(Project project) {
        try {
            if (StringUtils.isBlank(project.getId())) {
                throw new IllegalArgumentException("The project '" + project.getName() + "' that should be update " +
                        "doesn't has an id. Check that the project was persisted before!");
            }

            DeleteParams deleteParams = new DeleteParams().addFilter(EkgSchemaField.ID, project.getId());
            client.delete(deleteParams);
            client.add(project);
            client.commit();

            List<Project> projects = queryProjects(project.getName(), project.useSplitSource());

            if (projects.isEmpty()) {
                throw new IllegalStateException("Unable to fined the project in the repository that was stored just before.");
            }

            return projects.get(0);

        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.project.api.ProjectDataAccessService#deleteProjectByName(java.lang.String)
     */
    @Override
    public void deleteProjectByName(String projectName) {

        try {
            DeleteParams deleteQuery = new DeleteParams();
            deleteQuery.addFilter(EkgSchemaField.PROJECT_NAME, projectName);
            client.delete(deleteQuery);

            deleteQuery = new DeleteParams();
            deleteQuery.addFilter(EkgSchemaField.REPOSITORY_ACCORDING_PROJECT, projectName);
            client.delete(deleteQuery);

            client.commit();

        } catch (RepositoryException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
