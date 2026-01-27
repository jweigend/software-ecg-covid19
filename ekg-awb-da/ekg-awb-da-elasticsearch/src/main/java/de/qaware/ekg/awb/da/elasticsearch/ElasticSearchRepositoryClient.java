package de.qaware.ekg.awb.da.elasticsearch;

import de.qaware.ekg.awb.da.elasticsearch.utils.ObjectBinder;
import de.qaware.ekg.awb.da.elasticsearch.utils.SchemaBuilder;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
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
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * A implementation of the {@link RepositoryClient} that is
 * build on top of the ElasticSearch database and it's RestHighLevelClient
 * proxy API.
 */
public class ElasticSearchRepositoryClient implements RepositoryClient {

    private static final Logger LOGGER = EkgLogger.get();

    private static final int BATCH_UPDATE_BULK_SIZE = 10_000;

    private static final String DELETE_BY_QUERY_HANDLER = "/_delete_by_query";

    private static final String IO_ERROR_MESSAGE = "I/O error accessing ElasticSearch server.";

    private String elasticSearchIndex;

    private ElasticSearchQueryFactory queryFactory;

    private RestHighLevelClient elasticSearchClient;

    public ElasticSearchRepositoryClient(RestHighLevelClient elasticClient, String dbIndex) {
        this.elasticSearchIndex = dbIndex;
        this.elasticSearchClient = elasticClient;
        this.queryFactory = new ElasticSearchQueryFactory();
    }

    @Override
    public <T> SearchResult<T> search(Class<T> type, SearchParams searchParams) throws RepositoryException {

        try {
            validateSearchParams(type, searchParams);

            ActionRequest searchQuery = queryFactory.createScrollableSearchQuery(searchParams, elasticSearchIndex);

            SearchResponse response;
            if (searchQuery instanceof SearchRequest) {
                response = elasticSearchClient.search((SearchRequest) searchQuery, RequestOptions.DEFAULT);
            } else {
                response = elasticSearchClient.scroll((SearchScrollRequest) searchQuery, RequestOptions.DEFAULT);
            }

            if (!checkResponse(response.status(), searchParams.getFilterQueries(), "Search ")) {
                throw new RepositoryException("the facet search could proceed correctly. Retrieve unexpected " +
                        "response status: " + response.status().name());
            }

            List<T> documents = ObjectBinder.mapToBeans(type, response.getHits());
            return new SearchResult<>(documents, createSearchQueryStatus(response));

        } catch (Exception e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public <T> Stream<T> export(Class<T> type, ExportParams exportParams) {
        throw new NotImplementedException("Not implemented yet");
    }

    @Override
    public <T> SearchResult<T> collapse(Class<T> type, SearchParams searchParams, Field groupingField,
                                        SortField groupSortField) {
        throw new NotImplementedException("Not implemented yet");
    }

    @Override
    public long sumFieldValue(EkgSchemaField field, List<Expression> filterQueries) {
        return -1;
    }

    @Override
    public FacetResult facet(FacetParams facetParams) throws RepositoryException {

        try {
            Validate.notNull(facetParams);
            Validate.notEmpty(facetParams.getFacetFields());
            Validate.notNull(facetParams.getFacetMissing());
            Validate.notNull(facetParams.getFilterQueries());

            SearchRequest facetQuery = queryFactory.createFacetQuery(facetParams, elasticSearchIndex);

            SearchResponse response = elasticSearchClient.search(facetQuery, RequestOptions.DEFAULT);
            if (!checkResponse(response.status(), facetParams.getFilterQueries(), "Facet search ")) {
                throw new RepositoryException("the facet search could proceed correctly. Retrieve unexpected " +
                        "response status: " + response.status().name());
            }

            return new FacetResult(extractFacets(response), createSearchQueryStatus(response));

        } catch (Exception e) {
            LOGGER.error("Exception at Solr persistence layer occurred: ", e);
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void add(Iterator<?> entities) throws RepositoryException {
        try {

            BulkRequest bulkRequest = new BulkRequest();

            int count = 0;

            while (entities.hasNext()) {

                Object entity = entities.next();

                // every time the loop exceed the bulk size all changes will send to ElasticSearch
                // and a new chunk will started.
                if (count > BATCH_UPDATE_BULK_SIZE) {
                    BulkResponse updateResponse = elasticSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                    if (!checkResponse(updateResponse.status(), "", "Add a batch of entities ") &&
                            checkFailures(updateResponse.hasFailures(), updateResponse.buildFailureMessage())) {
                        break;
                    }

                    bulkRequest = new BulkRequest();
                }

                // add the single document to the bulk request
                IndexRequest indexRequest = new IndexRequest(elasticSearchIndex,
                                                        SchemaBuilder.DOC_TYPE_KEY, UUID.randomUUID().toString());

                indexRequest.source(ObjectBinder.mapToContentMap(entity));

                bulkRequest.add(indexRequest);

                count++;
            }

            // send the remaining document of the unfinished chunk if necessary
            if (bulkRequest.numberOfActions() > 0) {
                BulkResponse updateResponse = elasticSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                checkResponse(updateResponse.status(), "", "Add a batch of entities ");
                checkFailures(updateResponse.hasFailures(), updateResponse.buildFailureMessage());
            }

        } catch (Exception e) {
            LOGGER.error("Exception at Solr persistence layer occurred: ", e);
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }


    @Override
    public void add(Object entity) throws RepositoryException {

        try {
            Map<String, Object> entityAsMap = ObjectBinder.mapToContentMap(entity);

            if (StringUtils.isNotBlank((String) entityAsMap.get("id"))) {
                UpdateRequest updateRequest = new UpdateRequest(elasticSearchIndex,
                        SchemaBuilder.DOC_TYPE_KEY, (String) entityAsMap.get("id"));

                updateRequest.doc(entityAsMap);

                // update data
                UpdateResponse updateResponse = elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
                if (checkResponse(updateResponse.status(), entity, "Add new entity ")) {
                    commit();
                }

            } else {

                IndexRequest indexRequest = new IndexRequest(elasticSearchIndex,
                                SchemaBuilder.DOC_TYPE_KEY, UUID.randomUUID().toString());

                indexRequest.source(entityAsMap);

                // add data
                IndexResponse updateResponse = elasticSearchClient.index(indexRequest, RequestOptions.DEFAULT);
                if (checkResponse(updateResponse.status(), entity, "Add new entity ")) {
                    commit();
                }
            }

        } catch (Exception e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void deleteAll() throws RepositoryException {
        delete(null);
    }

    @Override
    public void delete(DeleteParams deleteParams) throws RepositoryException {

        try {
            Request request = new Request("POST", elasticSearchIndex + DELETE_BY_QUERY_HANDLER);
            request.setJsonEntity(convertSourceToJson(convertToSourceBuilder(deleteParams)));

            Response response = elasticSearchClient.getLowLevelClient().performRequest(request);

            if (response.getStatusLine().getStatusCode() >= 400) {
                LOGGER.error("Error occurred trying to delete records from ElasticSearch. Reason: {}",
                        response.getStatusLine().getReasonPhrase());
            }

            commit();

        } catch (Exception e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void commit() throws RepositoryException {
        try {
            // commit the change
            FlushResponse flushResponse = elasticSearchClient.indices().flush(new FlushRequest(elasticSearchIndex),
                    RequestOptions.DEFAULT);

            checkResponse(flushResponse.getStatus(), elasticSearchIndex, "Commit index ");
        } catch (Exception e) {
            throw new RepositoryException(IO_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void close() throws RepositoryException {
        try {
            elasticSearchClient.close();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    //=================================================================================================================
    // internal helpers
    //=================================================================================================================


    private boolean checkFailures(boolean hasFailures, String buildFailureMessage) {
        if (hasFailures) {
            LOGGER.error(buildFailureMessage);
        }

        return hasFailures;
    }


    private <T> void validateSearchParams(Class<T> type, SearchParams searchParams) {
        Validate.notNull(type);
        Validate.notNull(searchParams);
        Validate.notNull(searchParams.getFilterQueries());
    }


    /**
     * Creates a {@link QueryStatus} for the given Solr {@link SearchResponse}.
     *
     * @param response the ElasticSearch {@link SearchResponse}.
     * @return the {@link QueryStatus}
     */
    private QueryStatus createSearchQueryStatus(SearchResponse response) {
      return new QueryStatus(response.getHits().getTotalHits().value,
              (int) response.getTook().getMillis(), response.getScrollId());
    }

    /**
     * Extracts the {@link Facet}s from the given {@link SearchResponse} converting ElasticSearch's {@link Aggregation}s
     * to our {@link Facet}s. The methods expect that the aggregations are the result of the term bucket so it can
     * converted to ParsedStringTerms.
     *
     * @param response the {@link SearchResponse} of the facet query
     * @return the {@link Facet}s
     */
    private List<Facet> extractFacets(SearchResponse response) {

        Aggregations aggregations = response.getAggregations();

        List<Facet> facetList = new ArrayList<>();
        for (Aggregation facetAggregation : aggregations.asList()) {
            ParsedStringTerms facets = (ParsedStringTerms) facetAggregation;

            List<FacetEntry> facetEntries = new ArrayList<>();
            for (Terms.Bucket facetBucket : facets.getBuckets()) {
                facetEntries.add(new FacetEntry(facetBucket.getKey().toString(), facetBucket.getDocCount()));
            }

            facetList.add(new Facet(facets.getName(), facetEntries));
        }

        return facetList;
    }


    private SearchSourceBuilder convertToSourceBuilder(DeleteParams deleteParams) {

        if (deleteParams == null) {
            return new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
        }

        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        booleanQueryBuilder.must(QueryBuilders.matchAllQuery());

        for (Map.Entry<EkgSchemaField, String> filterExpressions : deleteParams.getFilterExpMap().entrySet()) {
            booleanQueryBuilder.filter(
                    QueryBuilders.wildcardQuery(filterExpressions.getKey().getName(), filterExpressions.getValue()));
        }

        return new SearchSourceBuilder().query(booleanQueryBuilder);
    }

    private boolean checkResponse(RestStatus status, Object reqContent, String action) {

        if (status != RestStatus.OK && status != RestStatus.CREATED) {
            LOGGER.error(action + reqContent + " at/to ElasticSearch repository results in an " +
                    "unexpected result " + status);

            return false;
        }

        return true;
    }

    public static String convertSourceToJson(SearchSourceBuilder source) throws IOException {
        XContentBuilder xContent = source.toXContent(XContentFactory.jsonBuilder(), ToXContent.EMPTY_PARAMS);
        return Strings.toString(xContent);
    }
}
