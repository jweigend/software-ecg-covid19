package de.qaware.ekg.awb.repository.bl.repositories;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryClientAware;
import de.qaware.ekg.awb.repository.api.RepositoryClientFactory;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The base class of all EKG repositories that provides the common
 * logic to instantiate the RepositoryClient with the connection settings of the current repository instance.
 */
public abstract class EkgRepositoryBase implements EkgRepository {

    /**
     * The logger used by this instance to communicate errors
     * and special events.
     */
    protected static final Logger LOGGER = EkgLogger.get();

    /**
     * The unique id of the types that will created by Solr itself
     * that the record that defines this types get persisted.
     */
    protected String id;

    /**
     * The type of the types
     * (Solr Cloud, Solr Classic Standalone or ElasticSearch)
     */
    protected EkgRepositoryDbType ekgRepositoryDbType;

    /**
     * The unique alias name of the types
     */
    protected String repositoryName;

    /**
     * The connection URL of the types
     * (host + port or complete URL with URI parts depending on types type)
     */
    protected String connectionUrl;

    /**
     * The name of Solr database index. In case of this classic Solr repository
     * this field keep <code>null</code> because the name of the Solr Core
     * should given via connection string.
     * In case of Solr Cloud repositories it will filled with the Collection name.
     */
    protected String dbIndexName;

    /**
     * The authentication type the types used
     * for client connections
     */
    protected ResourceAuthType repositoryAuthType;

    /**
     * The username used for authentication (optional)
     */
    protected String username;

    /**
     * The password used for authentication (optional)
     */
    protected String password;

    /**
     * The client that provides access to the underlying database layer
     * and implement the low-level API methods for CRUD actions.
     */
    protected RepositoryClient cachedClient;

    /**
     * A simple map used as cache for Service classes resolved via CDI
     * and reflection to save performance than the services will
     * used again and again.
     * Because the service is bind to a specific repository the cache must be here.
     */
    protected Map<Class<?>, Object> serviceCache = new HashMap<>();

    //=================================================================================================================
    // Repository accessor API
    //=================================================================================================================

    @Override
    public boolean isEmbedded() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public String getDBIndexName() {
        return dbIndexName;
    }

    @Override
    public EkgRepositoryDbType getEkgRepositoryDbType() {
        return ekgRepositoryDbType;
    }

    @Override
    public ResourceAuthType getRepositoryAuthType() {
        return repositoryAuthType;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the EKG repository specific client factory.
     * Each deriving class should provide it's own factory implementation.
     *
     * @return the client factory for this repository type
     */
    protected abstract RepositoryClientFactory getRepositoryClientFactory();

    //=================================================================================================================
    // Repository service API
    //=================================================================================================================

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RepositoryClientAware> T getBoundedService(Class<T> serviceInterface) {
        if (serviceCache.containsKey(serviceInterface)) {
            return (T) serviceCache.get(serviceInterface);
        }

        try {
            T service = ServiceDiscovery.lookup(serviceInterface);
            service.initializeService(getRepositoryClient());
            serviceCache.put(serviceInterface, service);
            return service;

        } catch (Exception e) {
            LOGGER.error("Run into an exception during drying to resolve the service for implementation " +
                    "service interface '{}'" + serviceInterface.getName(), e);

            return null;
        }
    }

    @Override
    public RepositoryClient getRepositoryClient() {

        if (cachedClient == null) {
            String connectionUrl = getConnectionUrl();
            if (StringUtils.isNotBlank(getDBIndexName())) {
                connectionUrl = connectionUrl.replaceAll("/$", "/" + getDBIndexName());
            }

            cachedClient = getRepositoryClientFactory().createRepositoryClient(connectionUrl, getUsername(), getPassword());
        }

        return cachedClient;
    }

}
