package de.qaware.ekg.awb.da.solr.embedded;

import de.qaware.ekg.awb.da.solr.SolrRepositoryClientProvider;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryClientFactory;
import de.qaware.ekg.awb.repository.api.types.Embedded;
import de.qaware.ekg.awb.repository.api.types.Solr;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Factory class that provides a RepositoryClient that provides
 * the read/write access to an embedded Solr instance.
 */
@Solr
@Embedded
@Singleton
public class EmbeddedRepositoryClientProvider implements RepositoryClientFactory {

    private static final String CORE_NAME = "ekgdata";

    private SolrClient solrEmbeddedClient;

    private RepositoryClient cachedClient;

    @Inject
    private CoreContainer coreContainer;


    /**
     * Post initializes the solr server.
     */
    @PostConstruct
    public synchronized void initSolrServer() {

        if (solrEmbeddedClient != null) {
            return;
        }

        solrEmbeddedClient = new EmbeddedSolrServer(coreContainer, CORE_NAME);
    }

    @Override
    public synchronized RepositoryClient createRepositoryClient(String connectionUrl, String userName, String password) {

        if (cachedClient == null) {
            SolrRepositoryClientProvider clientProvider = new SolrRepositoryClientProvider(solrEmbeddedClient);
            cachedClient = clientProvider.getClient(CORE_NAME);
        }

        return cachedClient;
    }

    @Override
    public RepositoryClient createRepositoryClient(String clusterManagerUrl, String repositoryName,
                                                   String userName, String password) {

        return createRepositoryClient(null, null, null);
    }
}
