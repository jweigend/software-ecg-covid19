//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.repository.bl.services;


import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryService;
import de.qaware.ekg.awb.repository.api.dataobject.delete.DeleteParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.events.RepositoryChangeEvent;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.types.Embedded;
import de.qaware.ekg.awb.repository.bl.mapper.RepositoryMapper;
import de.qaware.ekg.awb.repository.bl.repositories.RepositoryEt;
import de.qaware.ekg.awb.sdk.awbapi.repository.*;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory.exactFilter;
import static de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory.wildcardFilter;
import static de.qaware.ekg.awb.sdk.core.events.ChangedEvent.Change.ADD;
import static de.qaware.ekg.awb.sdk.core.events.ChangedEvent.Change.DELETE;

/**
 *  A data access service that provides functionality to read, write & query
 *  information's about types types from the persistence layer.
 */
@SuppressWarnings("unused") // used via CDI / Reflection
public class RepositoryDataAccessServiceImpl implements RepositoryService, SourceRepositoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDataAccessServiceImpl.class);

    private RepositoryClient client;

    @Inject
    @Embedded
    private EkgRepository embeddedRepository;

    public RepositoryDataAccessServiceImpl() {
        // no op
    }

    public RepositoryDataAccessServiceImpl(Repository repository) {
        if (repository instanceof EkgRepository) {
            client = ((EkgRepository) repository).getRepositoryClient();
        } else {
            LOGGER.warn("RepositoryDataAccessService called via parameterized constructor without EkgRepository instance.");
        }
    }

    //=================================================================================================================
    // implementation of RepositoryClientAware interface (base of the RepositoryService)
    //=================================================================================================================

    @Override
    public void initializeService(RepositoryClient client) {
        this.client = client;
    }

    //=================================================================================================================
    // implementation of the API defined by the RepositoryService interface itself
    //=================================================================================================================

    @Override
    public List<EkgRepository> listEkgRepositories() {

        List<EkgRepository> result = new ArrayList<>();

        try {

            SearchParams searchParams = new SearchParams()
                    .withFilterQueries(
                            wildcardFilter(EkgSchemaField.REPOSITORY_NAME, "*"),
                            exactFilter(EkgSchemaField.DOC_TYPE, DocumentType.REPOSITORY.toString()))
                    .withSortField(EkgSchemaField.REPOSITORY_NAME, SortField.SortMode.DESC);

            SearchResult<RepositoryEt> searchResult = client.search(RepositoryEt.class, searchParams);

            result = searchResult.getRows().stream()
                    .map(RepositoryMapper::buildRepository)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());


            if (!result.contains(embeddedRepository)) {
                result.add(embeddedRepository);
            }

        } catch (RepositoryException e) {
            LOGGER.error("Exception raised while getting the types from solr.", e);
        }

        return result;

    }

    @Override
    public List<ImporterSourceRepository> listImportSourceRepositories() {
        throw new NotImplementedException("method not implemented");
    }

    @Override
    public ImporterSourceRepository queryImportSourceRepository(String projectName) {

         try {
            SearchParams searchParams = new SearchParams()
                    .withFilterQueries(
                            exactFilter(EkgSchemaField.DOC_TYPE, DocumentType.REPOSITORY.toString()),
                            exactFilter(EkgSchemaField.REPOSITORY_IS_IMPORTSOURCE, Boolean.TRUE.toString()),
                            exactFilter(EkgSchemaField.REPOSITORY_ACCORDING_PROJECT, projectName)
                    )
                    .withSortField(EkgSchemaField.REPOSITORY_NAME, SortField.SortMode.DESC);


            List<RepositoryEt> sourceRepositories = client.search(RepositoryEt.class, searchParams).getRows();

            if (sourceRepositories.size() == 0) {
                return null;
            }

            if (sourceRepositories.size() > 1) {
                throw new IllegalStateException("Found more than one source types for project " + projectName);
            }

            return RepositoryMapper.mapToImporterSourceRepository(sourceRepositories.get(0));

        } catch (RepositoryException e) {
            throw new IllegalStateException("Could not read ImporterSourceRepository from Solr", e);
        }
    }

    @Override
    public void deleteImportSourceRepositoryByProjectName(String projectName) {

        try {
            DeleteParams deleteParams = new DeleteParams();
            deleteParams.addFilter(EkgSchemaField.DOC_TYPE, DocumentType.REPOSITORY.toString());
            deleteParams.addFilter(EkgSchemaField.REPOSITORY_IS_IMPORTSOURCE, "true");
            deleteParams.addFilter(EkgSchemaField.REPOSITORY_ACCORDING_PROJECT,  projectName);

            client.delete(deleteParams);
            client.commit();

        } catch (RepositoryException e) {
            throw new IllegalStateException("Could not read ImporterSourceRepository from Solr", e);
        }
    }

    @Override
    public void persistImporterSourceRepository(ImporterSourceRepository sourceRepository) {

        // check for null values and invalid repositories.
        if (sourceRepository == null || StringUtils.isBlank(sourceRepository.getRepositoryUrl())) {
            LOGGER.error("The given ImporterSourceRepository is null or has no connection URL.");
            return;
        }

        if (StringUtils.isBlank(sourceRepository.getAccordingProjectName())) {
            LOGGER.error("The given ImporterSourceRepository doesn't define the project it belongs to.");
            return;
        }

        RepositoryEt repositoryEt = RepositoryMapper.mapToRepositoryEt(sourceRepository);

        try {
            client.add(repositoryEt);
            client.commit();

        } catch (RepositoryException e) {
            throw new IllegalStateException("Could not write new ImporterSourceRepository to Solr", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public EkgRepository addEkgRepository(String name, String connectionUrl, String indexName,
                                          EkgRepositoryDbType repositoryDbType, ResourceAuthType resourceAuthType,
                                          String username, String password) {

        RepositoryEt repositoryDt = RepositoryMapper.createRepositoryEt(name, connectionUrl, indexName,
                repositoryDbType, resourceAuthType, username, password);


        try {
            client.add(repositoryDt);
            client.commit();
            EkgRepository repository = RepositoryMapper.buildRepository(repositoryDt);
            EkgLookup.lookup(EkgEventBus.class).publish(new RepositoryChangeEvent(repository, ADD, this));
            return repository;

        } catch (RepositoryException e) {
            throw new IllegalStateException("Could not addAndSum or commit SolrClassicRepository to solr", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deleteRepository(String id) {

        try {
            DeleteParams deleteParams = new DeleteParams();
            deleteParams.addFilter(EkgSchemaField.ID, id);
            client.delete(deleteParams);
            client.commit();

            EkgLookup.lookup(EkgEventBus.class).publish(new RepositoryChangeEvent(null, DELETE, this));
        } catch (RepositoryException e) {
            LOGGER.warn("Could not remove the types with id {} from solr", id, e);
        }
    }

    @Override
    public void deleteRepository(EkgRepository repository) {
        deleteRepository(repository.getId());
    }

}
