package de.qaware.ekg.awb.da.solr;

import de.qaware.ekg.awb.da.solr.utils.SolrClientCacheFactory;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryClientFactory;
import de.qaware.ekg.awb.repository.api.SearchIndexAdminClient;
import de.qaware.ekg.awb.repository.api.SearchIndexClientProvider;
import de.qaware.ekg.awb.repository.api.types.Remote;
import de.qaware.ekg.awb.repository.api.types.Solr;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.io.SolrClientCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider that creates and manages {@link SolrRepositoryClient}s.
 * <p>
 * The provider instance internally holds a {@link SolrClientCache} which holds one {@link CloudSolrClient}.
 * It is recommended to create only one provider and therefore only one {@link CloudSolrClient} per JVM,
 * e.g. by using a CDI singleton wrapper around the provider.
 * This is important because the {@link CloudSolrClient} holds connections to all ZooKeeper nodes and ZooKeeper can
 * only handle 30-40 connections cluster-wide before becoming increasingly unstable.
 */
@Solr
@Remote
public class SolrRepositoryClientProvider implements SearchIndexClientProvider, RepositoryClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrRepositoryClientProvider.class);

    // If connectionInfo is set, we internally use a CloudSolrClient, otherwise we use a HttpSolrClient
    // - see constructors.
    private SolrClientCacheFactory solrClientCacheFactory;
    private SolrConnectionInfo connectionInfo;
    private String baseUrl;
    private String username;
    private String password;
    private SolrClient solrClient;
    private SolrClientCache solrClientCache;

    private volatile boolean initialized;


    //================================================================================================================
    //  different type of constructors for multiple use cases
    //================================================================================================================

    /**
     * Default constructor for CDI usage.
     * Using the provider via CDI has restrictions because the
     * factory isn't initialized with connection data. Methods
     * like "getClient(String)" wont work.
     */
    public SolrRepositoryClientProvider() {
        this.solrClientCacheFactory = new SolrClientCacheFactory();
    }

    /**
     * Constructs a {@link SolrRepositoryClientProvider} that will connect via {@link CloudSolrClient} to Solr
     * using the ZooKeeper settings from the given {@link SolrConnectionInfo}. Always use this for internal access from
     * within the cluster (e.g. from PSMG Hub tsquery).
     *
     * @param solrConnectionInfo Solr connection parameters
     * @param username the username for BasicAuth authentication or null if no authentication required
     * @param password the password for BasicAuth authentication or null if no authentication required
     */
    public SolrRepositoryClientProvider(SolrConnectionInfo solrConnectionInfo, String username, String password) {
        this(solrConnectionInfo, null, username, password, new SolrClientCacheFactory());
    }

    public SolrRepositoryClientProvider(SolrClient solrClient) {
        this.connectionInfo = null;
        this.baseUrl = null;
        this.username = null;
        this.password = null;
        this.solrClientCacheFactory = new SolrClientCacheFactory();
        this.solrClient = solrClient;
    }

    /**
     * Constructs a {@link SolrRepositoryClientProvider} that will connect via {@link HttpSolrClient} to Solr
     * using the given solrBaseUrl. Use this for external access from outside the cluster (e.g. from a test driver in
     * module tests).
     *
     * @param solrBaseUrl the url for {@link HttpSolrClient} connection.
     * @param username the username for BasicAuth authentication or null if no authentication required
     * @param password the password for BasicAuth authentication or null if no authentication required
     */
    public SolrRepositoryClientProvider(String solrBaseUrl, String username, String password) {
        this(null, solrBaseUrl, username, password, new SolrClientCacheFactory());
    }

    /**
     * Constructs a {@link SolrRepositoryClientProvider} that will connect via {@link HttpSolrClient} to Solr
     * using the given solrBaseUrl. Use this for external access from outside the cluster (e.g. from a test driver in
     * module tests).
     *
     * @param solrConnectionInfo Solr connection parameters
     * @param solrBaseUrl the url for {@link HttpSolrClient} connection.
     * @param username the username for BasicAuth authentication or null if no authentication required
     * @param password the password for BasicAuth authentication or null if no authentication required
     * @param solrClientCacheFactory a client cache factory
     */
    public SolrRepositoryClientProvider(SolrConnectionInfo solrConnectionInfo,
                                        String solrBaseUrl, String username, String password,
                                        SolrClientCacheFactory solrClientCacheFactory) {
        this.connectionInfo = solrConnectionInfo;
        this.baseUrl = solrBaseUrl;
        this.username = username;
        this.password = password;
        this.solrClientCacheFactory = solrClientCacheFactory;
        this.solrClient = null;
    }

    //================================================================================================================
    //  implementation of RepositoryClientFactory interface
    //================================================================================================================

    @Override
    public RepositoryClient createRepositoryClient(String connectionUrl, String username, String password) {
        return new SolrRepositoryClient(null, getSolrClientCache(), null, connectionUrl, username, password);
    }

    @Override
    public RepositoryClient createRepositoryClient(String clusterManagerUrl, String repositoryName, String username, String password) {
        return new SolrRepositoryClient(repositoryName, getSolrClientCache(), clusterManagerUrl, null, username, password);
    }

    //================================================================================================================
    //  implementation of SearchIndexClientProvider interface
    //================================================================================================================


    @Override
    public RepositoryClient getClient(String collection) {

        if (solrClient != null) {
            return new SolrRepositoryClient(solrClient);
        }

        String zkHost = null;
        if (connectionInfo != null) {
            zkHost = connectionInfo.getZkHostsString();
        }

        return new SolrRepositoryClient(collection, getSolrClientCache(), zkHost, baseUrl, username, password);
    }

    @Override
    public SearchIndexAdminClient getAdminClient() {
        SolrClient client;

        if (solrClient != null) {
            client = solrClient;
        } else if (connectionInfo != null) {
            client = getSolrClientCache().getCloudSolrClient(connectionInfo.getZkHostsString());
        } else {
            client = getSolrClientCache().getHttpSolrClient(baseUrl);
        }

        return new SolrRepositoryAdminClient(client);
    }


    //================================================================================================================
    //  internal logic to support the interface implementation
    //================================================================================================================


    // Justification for SuppressWarnings("findbugs:IS2_INCONSISTENT_SYNC"):
    // The method uses double check locking to initialize the SolrClient. The method should only synchronize if the
    // SolrClient has not yet been initialized. A synchronization on every access makes no sense - it only makes
    // the method slower.
    @SuppressWarnings("findbugs:IS2_INCONSISTENT_SYNC")
    private SolrClientCache getSolrClientCache() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                        try {
                            createSolrClientCache();
                        } catch (RuntimeException e) {
                            LOGGER.warn("Error initializing CloudSolrClient.", e);
                            throw e;
                        }
                    initialized = true;
                }
            }
        }
        return solrClientCache;
    }

    private void createSolrClientCache() {
        solrClientCache = solrClientCacheFactory.create();

        // If we have the connectionInfo, we will use CloudSolrClients (see constructor). In this case, initialize
        // the singleton client by setting the other ZK settings.
        if (connectionInfo != null) {
            // initialize the singleton SolrCloudClient with our ZooKeeper settings
            // Note: In Solr 9+, ZK timeouts are configured via the Builder pattern
            // and cannot be set after client creation
            solrClientCache.getCloudSolrClient(connectionInfo.getZkHostsString());
        }
    }

}
