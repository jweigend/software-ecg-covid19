package de.qaware.ekg.awb.da.elasticsearch;

import de.qaware.ekg.awb.da.elasticsearch.expression.ElasticSearchExpressionRenderer;
import de.qaware.ekg.awb.da.elasticsearch.utils.SchemaBuilder;
import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.schema.Field;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.List;

/**
 * A factory for ElasticSearch search, facet, or update queries.
 */
public class ElasticSearchQueryFactory {

    private final ElasticSearchExpressionRenderer renderer;

    /**
     * Default constructor that will create a new instance of this
     * factory with the default implementation of ElasticSearchExpressionRenderer
     * as renderer for the filter expressions.
     */
    public ElasticSearchQueryFactory() {
        this(new ElasticSearchExpressionRenderer());
    }

    /**
     * Parameterized constructor with will create a new instance of this
     * factory with the given implementation of ElasticSearchExpressionRenderer
     * as renderer for the filter
     *
     * @param renderer a ElasticSearchExpressionRenderer instance used to render the filter expressions
     */
    /* package-private */ ElasticSearchQueryFactory(ElasticSearchExpressionRenderer renderer) {
        this.renderer = renderer;
    }


    public SearchRequest createFacetQuery(FacetParams facetParams, String elasticSearchIndex) {

        // we limit it to one index, wildcard patterns are working
        SearchRequest searchRequest = new SearchRequest(elasticSearchIndex);

        // set document type, since v6 optional
        searchRequest.types(SchemaBuilder.DOC_TYPE_KEY);

        // Use a builder to construct the search query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // we wan't zero search results (documents as list) because we are only interested in the aggregations
        searchSourceBuilder.size(0);

        QueryBuilder queryBuilder = toQueryFilters(facetParams.getFilterQueries());

        searchSourceBuilder.query(queryBuilder);      //set query part

        for (Field field : facetParams.getFacetFields()) {
            TermsAggregationBuilder aggregation = AggregationBuilders.terms(field.getName());
            aggregation.field(field.getName());
            searchSourceBuilder.aggregation(aggregation); //set aggs part
        }

        // assign search query to search request
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }

    /**
     * Renders the given API filter expressions into filter queries that are provided
     * by an ElasticSearch QueryBuilder instance.
     * If the List is empty or null an MatchAll query will returned.
     *
     * @param expressions The filter expressions that should converted
     * @return The filter query as QueryBuilder.
     */
    private QueryBuilder toQueryFilters(List<Expression> expressions) {

        if (expressions == null) {
            return QueryBuilders.matchAllQuery();
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        expressions.stream().map(renderer::render).forEach(boolQueryBuilder::filter);

        return boolQueryBuilder;
    }



    public ActionRequest createScrollableSearchQuery(SearchParams searchParams, String elasticSearchIndex) {

        // if this is the n-th request during scrolling we can go the short way
        if (searchParams.hasCursor() && !"*".equals(searchParams.getCursorMark())) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(searchParams.getCursorMark());
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
            return scrollRequest;
        }

        // the initial scroll request always needs the filter queries
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(toQueryFilters(searchParams.getFilterQueries()));
        searchSourceBuilder.size(searchParams.getMaxRows());

        for (SortField sortField : searchParams.getSortFields()) {
            searchSourceBuilder.sort(sortField.getField().getName(),sortField.getMode() == SortField.SortMode.ASC ?
                    SortOrder.ASC : SortOrder.DESC);
        }

        SearchRequest searchRequest = new SearchRequest(elasticSearchIndex);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));

        return searchRequest;
    }
}
