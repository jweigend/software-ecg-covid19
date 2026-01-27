package de.qaware.ekg.awb.repository.api;

import de.qaware.ekg.awb.repository.api.dataobject.delete.DeleteParams;
import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetParams;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.ExportParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.schema.Field;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.awbapi.repository.SeriesImportService;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A client providing read and write access to a search index, i.e. to a database optimized for fast search.
 * The client is always bound to a specific collection. Make sure to {@link #close()} the client after usage.
 * <p>
 * For managing the collections use {@link SearchIndexAdminClient} instead.
 */
public interface RepositoryClient extends AutoCloseable {

    /**
     * Searches for entities and returns them as Collection. Use this method for searches with
     * {@link SearchParams#getMaxRows() } smaller than or equal to {@link SearchParams#MAX_ROWS_LIMIT}.
     * <p>
     * For searches without limit, use {@link #export(Class, ExportParams)} instead.
     *
     * @param type         the {@link Class} of the entities
     * @param searchParams the search parameters
     * @param <T>          the type of the entities
     * @return the entities as Collection plus meta data
     * @throws RepositoryException on errors accessing the search index
     */
    <T> SearchResult<T> search(Class<T> type, SearchParams searchParams) throws RepositoryException;

    /**
     * Searches for entities and returns them as Stream. Use this method for searches without limit.
     * <p>
     * For searches with a limit smaller than or equal to {@link SearchParams#MAX_ROWS_LIMIT}, use
     * {@link #search(Class, SearchParams)} instead as it is faster.
     *
     * @param type         the {@link Class} of the entities
     * @param exportParams the export parameters
     * @param <T>          the type of the entities
     * @return the entities as Stream
     * @throws RepositoryException on errors accessing the search index
     */
    <T> Stream<T> export(Class<T> type, ExportParams exportParams) throws RepositoryException;

    /**
     * Searches for entities and collapses groups of entities based on a grouping field into one representative entity.
     * <p>
     * Note: In a distributed Search Index, all documents with the same value in the grouping field must be placed
     * on the same shard.
     *
     * @param type           the {@link Class} of the entities
     * @param searchParams   the search parameters
     * @param groupingField  the grouping field to collapse
     * @param groupSortField the field on which to sort within a group when selecting the representative entity
     * @param <T>            the type of the entities
     * @return the grouped entities as Collection plus meta data
     * @throws RepositoryException on errors accessing the search index
     */
    <T> SearchResult<T> collapse(Class<T> type, SearchParams searchParams, Field groupingField,
                                 SortField groupSortField) throws RepositoryException;

    /**
     * Return the sum of all values stored in the repository at the
     * specified field. For this the field has to be a numeric one.
     * Floating point value will round to longs.
     *
     * @param field the field that store the values that will aggregated
     * @param filterQueries the search params to find the documents the caller is interested in
     * @return the sum of all values
     */
    long sumFieldValue(EkgSchemaField field, List<Expression> filterQueries) throws RepositoryException;

    /**
     * Performs a facet search, i.e. calculates distinct values with counts.
     *
     * @param facetParams the facet parameters
     * @return the facet result
     * @throws RepositoryException on errors accessing the search index
     */
    FacetResult facet(FacetParams facetParams) throws RepositoryException;

    /**
     * Adds entities to the search index. Will only be visible after a {@link #commit()}.
     *
     * @param entities the entities
     * @throws RepositoryException on errors accessing the search index
     */
    void add(Iterator<?> entities) throws RepositoryException;

    /**
     * Adds a single bean to the repository that will automatically converted
     * to the data model of the underlying database using the Repository {@link Field} annotations.
     *
     * @param entity the entity that should persisted in the repository
     * @throws RepositoryException thrown than the repository isn't accessible or at any other problem
     */
    void add(Object entity) throws RepositoryException;

    /**
     * Clears the collection, i.e. deletes all data. Will only be visible after a {@link #commit()}.
     *
     * @throws RepositoryException on errors accessing the search index
     */
    void deleteAll() throws RepositoryException;

    /**
     * Deletes all records from the repository that matches to the filter defined in the
     * given delete parameters.
     *
     * @param deleteParams  the delete parameters that define filter used to find the records to delete
     * @throws RepositoryException thrown than the repository isn't accessible or at any other problem
     */
    void delete(DeleteParams deleteParams) throws RepositoryException;

    /**
     * Commits the current transaction.
     * <p>
     * Note on transactions: If the search index does only support "global" transactions like SolrCloud, the commit will
     * apply all changes of all threads/processes/cluster nodes. To avoid unexpected behaviour, it is mandatory to
     * only allow write access to one thread in one process in one cluster node at the same time.
     *
     * @throws RepositoryException on errors accessing the search index
     */
    void commit() throws RepositoryException;

    /**
     * Closes the {@link SeriesImportService}.
     *
     * @throws RepositoryException on errors accessing the search index
     */
    @Override
    void close() throws RepositoryException;
}
