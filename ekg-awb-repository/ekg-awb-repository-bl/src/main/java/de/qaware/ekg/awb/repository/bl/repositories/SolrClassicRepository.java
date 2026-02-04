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
package de.qaware.ekg.awb.repository.bl.repositories;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryClientFactory;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.repository.api.types.Remote;
import de.qaware.ekg.awb.repository.api.types.Solr;
import de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * An EkgRepository implementation that represents a Software-EKG repository build
 * on top of a classic standalone Solr search index.
 * This class has also a factory used as builder for it's own and some additional
 * properties like the member 'dbIndexName' that are not used by itself but belongs
 * to the EkgRepository definition and used by derived classes.
 */
@SuppressWarnings("CdiInjectionPointsInspection") // wouldn't inspect correctly
public class SolrClassicRepository extends EkgRepositoryBase {

    /**
     * A factory implementation that will provide a {@link RepositoryClient} instance
     * based on the repository properties like the connection url and credentials.
     * The factory will injected via CDI and is specialized on Solr remote resources.
     */
    @Inject
    @Solr // only inject factories that produce Solr
    @Remote // do inject factories for remote Solr databases, not inject for clients with Solr embedded binding
    protected RepositoryClientFactory repositoryClientFactory;


    //=================================================================================================================
    // SolrClassicRepository constructors
    //=================================================================================================================

    /**
     * Default constructor, for internal use and CDI.
     */
    public SolrClassicRepository() {
        // no op
    }

    /**
     * Initialize a solr types.
     *
     * @param repositoryName the the alias name of the types
     * @param connectionUrl  the connection url used to reach Solr in the network (at Solr cloud this is only host + ip)
     * @param ekgRepositoryDbType  the type of this types.
     * @param resourceAuthType   the authentication type the types used for client connections
     * @param username the optional username used for authentication if authentication type is not "none".
     * @param password the optional username used for authentication if authentication type is not "none".
     */
    public SolrClassicRepository(String id,
                                 String repositoryName,
                                 String connectionUrl,
                                 EkgRepositoryDbType ekgRepositoryDbType,
                                 ResourceAuthType resourceAuthType,
                                 String username,
                                 String password) {

        this.id = id;
        this.repositoryName = repositoryName;
        this.connectionUrl = connectionUrl;
        this.ekgRepositoryDbType = ekgRepositoryDbType;
        this.repositoryAuthType = resourceAuthType;
        this.username = username;
        this.password = password;
    }

    //=================================================================================================================
    //  API of the base class that has to implement
    //=================================================================================================================

    @Override
    protected RepositoryClientFactory getRepositoryClientFactory() {
        return repositoryClientFactory;
    }

    //=================================================================================================================
    //  custom client factory implementation for this repository type
    //=================================================================================================================

    /**
     * Factory to create a {@link SolrClassicRepository}.
     */
    @Singleton
    public static class Factory implements EkgRepository.Factory {

        @Inject
        @New
        private Instance<SolrClassicRepository> repositories;

        protected Instance<? extends SolrClassicRepository> getRepositories() {
            return repositories;
        }

        @Override
        public EkgRepository getInstance(String id,
                                         String name,
                                         String connectionUrl,
                                         String dbIndexName,
                                         EkgRepositoryDbType ekgRepositoryDbType,
                                         ResourceAuthType resourceAuthType,
                                         String username,
                                         String password) {

            if (!isTypeSupported(ekgRepositoryDbType)) {
                throw new IllegalArgumentException("Can not create solr types of type " + ekgRepositoryDbType);
            }

            SolrClassicRepository repository = getRepositories().get();
            repository.id = id;
            repository.repositoryName = name;
            repository.connectionUrl = connectionUrl;
            repository.dbIndexName = dbIndexName;
            repository.ekgRepositoryDbType = ekgRepositoryDbType;
            repository.repositoryAuthType = resourceAuthType;
            repository.username = username;
            repository.password = password;

            return repository;
        }

        @Override
        public boolean isTypeSupported(EkgRepositoryDbType type) {
            return type == EkgRepositoryDbType.SOLR_STANDALONE;
        }
    }
}
