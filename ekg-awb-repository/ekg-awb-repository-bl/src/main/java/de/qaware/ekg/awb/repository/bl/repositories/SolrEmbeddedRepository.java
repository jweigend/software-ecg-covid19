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
import de.qaware.ekg.awb.repository.api.RepositoryClientFactory;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.repository.api.types.Embedded;
import de.qaware.ekg.awb.repository.api.types.Solr;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implements the local embedded solr types, which runs within the same jvm as the current instance.
 */
@Default
@Embedded
@Singleton
public class SolrEmbeddedRepository extends SolrClassicRepository {

    private RepositoryClient cachedClient;

    @Inject
    @Solr
    @Embedded
    protected RepositoryClientFactory repositoryClientFactory;

    /**
     * Initialize the embedded solr types.
     */
    public SolrEmbeddedRepository() {
        super("local", "Local Repository", null, EkgRepositoryDbType.SOLR_EMBEDDED,
                ResourceAuthType.NONE, null, null);
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    @Override
    public void close() {
        try {
            cachedClient.close();
        } catch (RepositoryException e) {
            // do nothing
        }
    }

    @Override
    public RepositoryClient getRepositoryClient() {

        if (cachedClient == null) {
            cachedClient = repositoryClientFactory.createRepositoryClient(null, null, null);
        }

        return cachedClient;
    }


    /**
     * Factory to create the embedded solr types.
     */
    @Default
    @Singleton
    @Embedded
    public static class Factory implements EkgRepository.Factory {

        @Inject
        @Embedded
        private SolrClassicRepository repository;

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
                throw new IllegalArgumentException("Can not create embedded solr types of type "
                        + ekgRepositoryDbType);
            }

            return repository;
        }


        @Override
        public boolean isTypeSupported(EkgRepositoryDbType type) {
            return type == EkgRepositoryDbType.SOLR_EMBEDDED;
        }
    }
}
