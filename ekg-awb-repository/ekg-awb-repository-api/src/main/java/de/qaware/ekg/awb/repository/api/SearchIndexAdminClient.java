package de.qaware.ekg.awb.repository.api;

import de.qaware.ekg.awb.repository.api.dataobject.admin.CollectionProperties;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;

import java.util.Collection;

/**
 * A client providing admin access to a search index, i.e. managing the collections. Make sure to {@link #close()} the
 * client after usage.
 * <p>
 * For read and write access to the content of collections use {@link RepositoryClient} instead.
 */
public interface SearchIndexAdminClient extends AutoCloseable {

    /**
     * Sets an alias identifier for a collection.
     * <p>
     * If the alias does not exist yet, it will be created and assigned to the specified collection.
     * If the alias already exists and points to another collection, it will be changed to point to the given
     * collection.
     *
     * @param alias          the alias name
     * @param collectionName the name (unique identifier) of the collection the alias should point to
     * @throws RepositoryException on errors accessing the search index
     */
    void setAlias(String alias, String collectionName) throws RepositoryException;

    /**
     * Deletes an alias.
     * <p>
     * If the alias does not exist, nothing happens.
     * If the alias exists, it will be removed.
     *
     * @param alias the alias to remove
     * @throws RepositoryException on errors accessing the search index
     */
    void deleteAlias(String alias) throws RepositoryException;

    /**
     * Creates a new collection with the given properties.
     * <p>
     * The implementation ensures the newly created collection is online and accessible.
     *
     * @param collectionProperties     config properties that define the collection to create
     * @throws RepositoryException on errors accessing the search index
     */
    void create(CollectionProperties collectionProperties) throws RepositoryException;

    /**
     * Rebalances the shard leaders.
     *
     * @param collectionName the name of the collection to rebalance the shard leaders for
     * @throws RepositoryException on errors accessing the search index
     */
    void rebalanceShardLeaders(String collectionName) throws RepositoryException;

    /**
     * Deletes the specified collection.
     * <p>
     * All of its data and dedicated configuration files will be deleted.
     *
     * @param collectionName the name (unique identifier - not the alias) of the collection to delete
     * @throws RepositoryException on errors accessing the search index
     */
    void delete(String collectionName) throws RepositoryException;

    /**
     * Returns the name of the collection to which the given alias points.
     *
     * @param alias the alias name
     * @return the name of the collection or null if the alias does not exist
     * @throws RepositoryException on errors accessing the search index
     */
    String getCollectionNameForAlias(String alias) throws RepositoryException;

    /**
     * Returns the {@link CollectionProperties}s for all existing collections.
     *
     * @return the {@link CollectionProperties}s of all existing collections
     * @throws RepositoryException on errors accessing the search index
     */
    Collection<CollectionProperties> listCollections() throws RepositoryException;

    /**
     * Closes the {@link SearchIndexAdminClient}.
     *
     * @throws RepositoryException on errors accessing the search index
     */
    @Override
    void close() throws RepositoryException;
}
