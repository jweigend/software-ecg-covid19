package de.qaware.ekg.awb.da.solr;


import de.qaware.ekg.awb.da.solr.export.SolrExportIteratorFactory;
import de.qaware.ekg.awb.da.solr.utils.LoggingUtils;
import de.qaware.ekg.awb.da.solr.utils.ObjectBinder;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryNotAvailableException;
import de.qaware.ekg.awb.repository.api.dataobject.delete.DeleteParams;
import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.dataobject.facet.Facet;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetEntry;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetParams;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.ExportParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.dataobject.status.QueryStatus;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.schema.Field;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.io.SolrClientCache;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A {@link RepositoryClient} providing access to Apache SolrCloud.
 */
public final class SolrRepositoryClient implements RepositoryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrRepositoryClient.class);
    private static final String IO_ERROR_MESSAGE = "I/O error accessing Solr.";

    /**
     * The name of the system property to set the maximum length of all filter-queries.
     * If the maximum is exceeded, the logging of the fq-parameter will be suppressed.
     */
    private static final String PROP_MAX_FQ_LENGTH = "psmghub.searchIndexAccess.maxFqLength";

    /**
     * The default value for the maximum length of all filter-queries.
     * If the maximum is exceeded, the logging of the fq-parameter will be suppressed.
     */
    private static final int DEFAULT_MAX_FQ_LENGTH = 2048;

    // If zkHost is set, we internally use a CloudSolrClient, otherwise we use a HttpSolrClient - see constructor.
    private final SolrClientCache solrClientCache;
    private final String zkHost;
    private final String baseUrl;
    private final String username;
    private final String password;

    private final String collection;
    private final SolrClient solrClient;
    private final SolrQueryFactory queryFactory;
    private final SolrExportIteratorFactory exportIteratorFactory;

    /**
     * Constructs a {@link SolrRepositoryClient} bound to the given Solr collection, i.e. all queries will be directed
     * to this collection.
     *
     * @param collection  the Solr collection
     * @param clientCache cache providing {@link CloudSolrClient}s and
     *                    {@link org.apache.solr.client.solrj.impl.HttpSolrClient}s
     * @param zkHosts     ZooKeeper hosts as comma-separated string (may be null). If this is set, we internally
     *                    use a {@link CloudSolrClient} - always use this for internal access from within the
     *                    cluster
     * @param baseUrl     The baseUrl of one Solr node (may be null). If this is set, we internally use a
     *                    {@link org.apache.solr.client.solrj.impl.HttpSolrClient} - use this for external access
     *                    from outside the cluster (e.g. from a test driver in module tests).
     * @param username      the username for BasicAuth authentication or null if no authentication required
     * @param password      the password for BasicAuth authentication or null if no authentication required
     */
    /* package-private */ SolrRepositoryClient(String collection, SolrClientCache clientCache, String zkHosts,
                                               String baseUrl, String username, String password) {
        this(collection, clientCache, zkHosts, baseUrl, username, password,
                new SolrQueryFactory(), new SolrExportIteratorFactory());
    }

    /* package-private */ SolrRepositoryClient(String collection, SolrClientCache clientCache, String zkHosts,
                                               String baseUrl, String username, String password, SolrQueryFactory queryFactory,
                                               SolrExportIteratorFactory exportIteratorFactory) {
        this.collection = collection;
        this.solrClientCache = clientCache;
        this.zkHost = zkHosts;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.solrClient = null;
        this.queryFactory = queryFactory;
        this.exportIteratorFactory = exportIteratorFactory;
    }

    /* package-private */ SolrRepositoryClient(SolrClient solrClient) {
        this.collection = null;
        this.solrClientCache = new SolrClientCache();
        this.zkHost = null;
        this.baseUrl = null;
        this.username = null;
        this.password = null;
        this.solrClient = solrClient;
        this.queryFactory = new SolrQueryFactory();
        this.exportIteratorFactory = new SolrExportIteratorFactory();
    }


    @Override
    public <T> SearchResult<T> search(Class<T> type, SearchParams searchParams) throws RepositoryException {
        try {
            validateSearchParams(type, searchParams);

            SolrQuery solrQuery = queryFactory.createSelectQuery(type, searchParams);

            if (searchParams.hasCursor()) {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, searchParams.getCursorMark());
            }

            return executeSearchQuery(type, solrQuery);

        } catch (SolrServerException e) {

            if (e.getRootCause() instanceof ConnectException) {
                throw new RepositoryNotAvailableException(RepositoryNotAvailableException.Cause.HOST_NOT_AVAILABLE, e);
            }

            throw new RepositoryException(IO_ERROR_MESSAGE, e);

        } catch (Exception e) {

            if (e instanceof HttpSolrClient.RemoteSolrException && e.getMessage().contains("Not Found")) {
                throw new RepositoryNotAvailableException(RepositoryNotAvailableException.Cause.INDEX_NOT_AVAILABLE, e);
            }

            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public <T> Stream<T> export(Class<T> type, ExportParams exportParams) throws RepositoryException {
        Validate.notNull(type);
        Validate.notNull(exportParams);
        Validate.notNull(exportParams.getFilterQueries());
        Validate.notEmpty(exportParams.getSortFields(), "Exporting requires at least one sort field");

        Iterator<T> it = createExportIterator(type, exportParams);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
    }

    @Override
    public <T> SearchResult<T> collapse(Class<T> type, SearchParams searchParams, Field groupingField,
                                        SortField groupSortField)
            throws RepositoryException {
        try {
            validateSearchParams(type, searchParams);
            Validate.notNull(groupingField);

            String sortExpr = "";
            if(groupSortField != null) {
                sortExpr = " sort='" + groupSortField.getField().getName() + " "
                        + groupSortField.getMode().toString() + "'";
            }
            SolrQuery solrQuery = queryFactory.createSelectQuery(type, searchParams);
            solrQuery.addFilterQuery("{!collapse field=" + groupingField.getName() + sortExpr + "}");
            return executeSearchQuery(type, solrQuery);
        } catch (SolrServerException | IOException e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public long sumFieldValue(EkgSchemaField field, List<Expression> filterQueries) throws RepositoryException {

        try {
            SolrQuery solrQuery = queryFactory.createSumQuery(field, filterQueries);

            QueryResponse response = executeSolrQuery(solrQuery);

            response.getResponse().get("facets");

            return Double.valueOf(((SimpleOrderedMap)response.getResponse().get("facets")).get("sum").toString()).longValue();

        } catch (SolrServerException e) {

            if (e.getRootCause() instanceof ConnectException) {
                throw new RepositoryNotAvailableException(RepositoryNotAvailableException.Cause.HOST_NOT_AVAILABLE, e);
            }

            throw new RepositoryException(IO_ERROR_MESSAGE, e);

        } catch (Exception e) {

            if (e instanceof HttpSolrClient.RemoteSolrException && e.getMessage().contains("Not Found")) {
                throw new RepositoryNotAvailableException(RepositoryNotAvailableException.Cause.INDEX_NOT_AVAILABLE, e);
            }

            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public FacetResult facet(FacetParams facetParams) throws RepositoryException {
        try {
            Validate.notNull(facetParams);
            Validate.notEmpty(facetParams.getFacetFields());
            Validate.notNull(facetParams.getFacetMissing());
            Validate.notNull(facetParams.getFilterQueries());

            SolrQuery solrQuery = queryFactory.createFacetQuery(facetParams);
            QueryResponse response = executeSolrQuery(solrQuery);
            return new FacetResult(extractFacets(response), createQueryStatus(response));
        } catch (SolrServerException | IOException e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void add(Iterator<?> entities) throws RepositoryException {
        try {
            UpdateRequest updateRequest = new UpdateRequest();

            updateRequest.setDocIterator(new Iterator<>() {

                @Override
                public boolean hasNext() {
                    return entities.hasNext();
                }

                @Override
                public SolrInputDocument next() {
                    Object o = entities.next();
                    if (o == null) return null;
                    return ObjectBinder.mapToSolrInputDocument(o);
                }

                @Override
                public void remove() {
                    entities.remove();
                }
            });

            if (StringUtils.isNotBlank(username)) {
                updateRequest.setBasicAuthCredentials(username, password);
            }

            updateRequest.process(getSolrClient(), collection);

        } catch (SolrServerException | IOException e) {
            LOGGER.error("Exception at Solr persistence layer occurred: ", e);
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void add(Object entity) throws RepositoryException {
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.add(ObjectBinder.mapToSolrInputDocument(entity));
            updateRequest.setCommitWithin(-1);

            if (StringUtils.isNotBlank(username)) {
                updateRequest.setBasicAuthCredentials(username, password);
            }

            updateRequest.process(getSolrClient(), collection);
        } catch (SolrServerException | IOException e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void deleteAll() throws RepositoryException {
        try {
            UpdateRequest deleteRequest = new UpdateRequest();
            deleteRequest.deleteByQuery("*:*");
            deleteRequest.setCommitWithin(-1);

            if (StringUtils.isNotBlank(username)) {
                deleteRequest.setBasicAuthCredentials(username, password);
            }

            deleteRequest.process(getSolrClient(), collection);
        } catch (SolrServerException | IOException e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    public void delete(DeleteParams deleteParams) throws RepositoryException {
        try {
            UpdateRequest deleteRequest = new UpdateRequest();
            deleteRequest.deleteByQuery(deleteParams.toDeleteFilterQueryString());
            deleteRequest.setCommitWithin(-1);

            if (StringUtils.isNotBlank(username)) {
                deleteRequest.setBasicAuthCredentials(username, password);
            }

            deleteRequest.process(getSolrClient(), collection);
        } catch (Exception e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void commit() throws RepositoryException {
        try {
            UpdateRequest commitRequest = new UpdateRequest();
            commitRequest.setAction(UpdateRequest.ACTION.COMMIT, true, true);

            if (StringUtils.isNotBlank(username)) {
                commitRequest.setBasicAuthCredentials(username, password);
            }

            commitRequest.process(getSolrClient(), collection);
        } catch (SolrServerException | IOException e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void close() {
        try {
            solrClient.close();
        } catch (IOException e) {
            // nothing to do in this case
        }
    }

    private <T> void validateSearchParams(Class<T> type, SearchParams searchParams) {
        Validate.notNull(type);
        Validate.notNull(searchParams);
        Validate.notNull(searchParams.getFilterQueries());
    }

    private <T> SearchResult<T> executeSearchQuery(Class<T> type, SolrQuery solrQuery)
            throws SolrServerException, IOException {
        QueryResponse response = executeSolrQuery(solrQuery);
        List<T> documents = ObjectBinder.mapToBean(type, response.getResults());
        return new SearchResult<>(documents, createQueryStatus(response));
    }

    /**
     * Returns a {@link SolrClient} to use for requests. A {@link CloudSolrClient} is used if {@link #zkHost} is set,
     * otherwise a {@link org.apache.solr.client.solrj.impl.HttpSolrClient} with {@link #baseUrl}.
     *
     * @return the {@link SolrClient}
     */
    private SolrClient getSolrClient() {
        if (solrClient != null) {
            return solrClient;
        } else if (!StringUtils.isBlank(zkHost)) {
            return solrClientCache.getCloudSolrClient(zkHost);
        } else if (!StringUtils.isBlank(baseUrl)) {
            return solrClientCache.getHttpSolrClient(baseUrl);
        } else {
            throw new IllegalStateException("Neither zkHost nor baseUrl are set - can't create a SolrClient");
        }
    }

    /**
     * Extracts the {@link Facet}s from the given {@link QueryResponse} converting Solr's {@link FacetField}s to our
     * {@link Facet}s.
     *
     * @param response the {@link QueryResponse}
     * @return the {@link Facet}s
     */
    private List<Facet> extractFacets(QueryResponse response) {
        List<FacetField> facetFields = response.getFacetFields();
        List<Facet> facets = new ArrayList<>(facetFields.size());

        for (FacetField facetField : facetFields) {
            List<FacetField.Count> values = facetField.getValues();
            List<FacetEntry> facetEntries = new ArrayList<>(values.size());
            for (FacetField.Count value : values) {
                facetEntries.add(new FacetEntry(value.getName(), value.getCount()));
            }
            facets.add(new Facet(facetField.getName(), facetEntries));
        }

        return facets;
    }

    /**
     * Executes the given {@link SolrQuery}.
     *
     * @param solrQuery the query
     * @return the response
     * @throws SolrServerException if there is an error on the server
     * @throws IOException         If there is a low-level I/O error.
     */
    private QueryResponse executeSolrQuery(SolrQuery solrQuery) throws SolrServerException, IOException {
        SolrQuery modSolrQuery = reduceLogOverhead(solrQuery);
        if (LOGGER.isDebugEnabled()) {
            String queryForLogging = LoggingUtils.getShortenedStringForLogging(modSolrQuery);
            LOGGER.debug("Executing Solr query: {}", queryForLogging);
        }

        StopWatch stopWatch = StopWatch.createStarted();
        SolrClient solrClient = getSolrClient();

        QueryRequest queryRequest = new QueryRequest(modSolrQuery, SolrRequest.METHOD.GET);

        if (StringUtils.isNotBlank(username)) {
            queryRequest.setBasicAuthCredentials(username, password);
        }

        QueryResponse response = queryRequest.process(solrClient, collection);
        long queryTime = stopWatch.getTime(TimeUnit.MILLISECONDS);

        if (LOGGER.isDebugEnabled()) {
            String headerForLogging = LoggingUtils.getShortenedStringForLogging(response.getHeader());
            LOGGER.debug("Response: found {} documents, returned {} documents, in {}ms, header: {}",
                    response.getResults().getNumFound(), response.getResults().size(), queryTime, headerForLogging);
        }

        return response;
    }

    /**
     * Disables logging of the fq-parameter of the length of all filter-queries exceeds a configurable maximum.
     *
     * @param solrQuery The Solr query.
     * @return The modified Solr query.
     */
    private SolrQuery reduceLogOverhead(SolrQuery solrQuery) {
        // if the query contains no filter-queries, then abort
        String[] filterQueries = solrQuery.getFilterQueries();
        if (filterQueries == null || filterQueries.length == 0) {
            return solrQuery;
        }

        // if the length of the filter-queries doesn't exceed the maximum, then abort
        int currFqLength = Arrays.stream(filterQueries).mapToInt(String::length).sum();
        if (currFqLength <= getMaxFilterQueryLength()) {
            return solrQuery;
        }

        // otherwise, log only the non-fq parameters
        Set<String> paramNames = new TreeSet<>(solrQuery.getParameterNames());
        paramNames.remove(CommonParams.FQ);
        String logParamsList = StringUtils.join(paramNames, ",");

        return solrQuery.setParam(CommonParams.LOG_PARAMS_LIST, logParamsList);
    }

    /**
     * Returns the maximum length of all filter-queries. If this value is exceeded, then logging of the
     * fq-parameter is suppressed.
     * <p/>
     * The parameter can be set via the system property {@value #PROP_MAX_FQ_LENGTH}. If unset, then
     * the default value {@value DEFAULT_MAX_FQ_LENGTH} is used.
     *
     * @return The maximum length of all filter-queries.
     */
    private int getMaxFilterQueryLength() {
        String value = System.getProperty(PROP_MAX_FQ_LENGTH);
        return StringUtils.isNumeric(value)
                ? Integer.parseUnsignedInt(value)
                : DEFAULT_MAX_FQ_LENGTH;
    }

    /**
     * Creates a {@link QueryStatus} for the given Solr {@link QueryResponse}.
     *
     * @param response the Solr {@link QueryResponse}.
     * @return the {@link QueryStatus}
     */
    private QueryStatus createQueryStatus(QueryResponse response) {
        return new QueryStatus(response.getResults().getNumFound(), response.getQTime(), response.getNextCursorMark());
    }

    /**
     * Creates an iterator that executes a Solr query against the /export request handler and returns the matching
     * documents as entities in a streaming fashion.
     *
     * @param type         the Class of the entities
     * @param exportParams the export parameters
     * @param <T>          the type of the entities
     * @return the iterator
     * @throws RepositoryException on errors opening the Solr stream
     */
    private <T> Iterator<T> createExportIterator(Class<T> type, ExportParams exportParams) throws RepositoryException {
        SolrParams solrParams = queryFactory.createExportQuery(type, exportParams);
        StreamContext streamContext = new StreamContext();
        streamContext.setSolrClientCache(solrClientCache);

        if (solrClient != null) {
            return exportIteratorFactory.create(solrClient, type, solrParams, streamContext);
        } else {
            return exportIteratorFactory.create(collection, type, solrParams, streamContext, zkHost, baseUrl, username, password);
        }
    }
}
