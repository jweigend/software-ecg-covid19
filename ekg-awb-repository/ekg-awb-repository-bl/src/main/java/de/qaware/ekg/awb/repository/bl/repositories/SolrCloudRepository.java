//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.repository.bl.repositories;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The cloud solr types is a solr types that connects
 * to a Zookeeper instance of a SolrCloud.
 */
@Singleton
public class SolrCloudRepository extends SolrClassicRepository implements EkgRepository {

    @Override
    public RepositoryClient getRepositoryClient() {

        if (cachedClient == null) {
            cachedClient = repositoryClientFactory.createRepositoryClient(
                    getConnectionUrl(), getDBIndexName(), getUsername(), getPassword());
        }

        return cachedClient;
    }

    /**
     * Factory to create the a cloud solr types.
     */
    @Singleton
    public static class Factory extends SolrClassicRepository.Factory {

        @Inject
        @New
        private Instance<SolrCloudRepository> repositories;

        @Override
        protected Instance<? extends SolrClassicRepository> getRepositories() {
            return repositories;
        }

        @Override
        public boolean isTypeSupported(EkgRepositoryDbType type) {
            return type == EkgRepositoryDbType.SOLR_CLOUD;
        }
    }
}
