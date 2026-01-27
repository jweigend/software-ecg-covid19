package de.qaware.ekg.awb.da.solr;

import de.qaware.ekg.awb.da.solr.expression.SolrExpressionRenderer;
import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.ExportParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.schema.Field;
import de.qaware.ekg.awb.repository.api.schema.FieldReader;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A factory for Solr export, facet, or select queries.
 */
/* package-private */ class SolrQueryFactory {

    /**
     * Cached Solr fields for {@link #getFieldList(Class, Collection)}.
     */
    private static final ConcurrentHashMap<Class<?>, Set<String>> cachedSolrFields = new ConcurrentHashMap<>();

    private final SolrExpressionRenderer renderer;

    /**
     * Constructor.
     */
    public SolrQueryFactory() {
        this(new SolrExpressionRenderer());
    }

    /* package-private */ SolrQueryFactory(SolrExpressionRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Creates a Solr export query based on the given entity type and export parameters.
     * <p/>
     * The entity type is used to reduce the number of fetched fields to those annotated with
     * {@link Field} in the given entity type.
     *
     * @param type         The type of entity to fetch.
     * @param exportParams The export parameters.
     * @return The Solr query.
     */
    public SolrQuery createExportQuery(Class<?> type, ExportParams exportParams) {
        return createBasicSolrQuery()
                .setRequestHandler("/export")
                .setFields(getFieldList(type, exportParams.getSortFields()))
                .setSorts(getSortClauses(exportParams.getSortFields()))
                .setFilterQueries(getFilterQueries(exportParams.getFilterQueries()));
    }

    /**
     * Creates a Solr facet query based on the given facet parameters.
     *
     * @param facetParams The facet parameters.
     * @return The Solr query.
     */
    public SolrQuery createFacetQuery(FacetParams facetParams) {
        SolrQuery solrQuery = createBasicSolrQuery()
                .setFacet(true)
                .setRows(0)
                .setFacetSort(facetParams.getFacetSort())
                .setFacetLimit(facetParams.getLimit())
                .setFacetMinCount(facetParams.getMinCount())
                .addFacetField(extractFieldNames(facetParams.getFacetFields()))
                .setFilterQueries(getFilterQueries(facetParams.getFilterQueries()));

        // include a count of all documents missing the the following facets
        for (Field field : facetParams.getFacetMissing()) {
            String param = String.format("f.%s.%s", field.getName(),
                    org.apache.solr.common.params.FacetParams.FACET_MISSING);
            solrQuery.set(param, true);
        }

        return solrQuery;
    }

    /**
     * Creates a Solr select query based on the given entity type and search parameters.
     * <p/>
     * The entity type is used to reduce the number of fetched fields to those annotated with
     * {@link Field} in the given entity type.
     *
     * @param type         The type of entity to fetch.
     * @param searchParams The search parameters.
     * @return The Solr query.
     */
    public SolrQuery createSelectQuery(Class<?> type, SearchParams searchParams) {
        return createBasicSolrQuery()
                .setStart(searchParams.getStartRow())
                .setRows(searchParams.getMaxRows())
                .setFields(getFieldList(type, searchParams.getSortFields()))
                .setSorts(getSortClauses(searchParams.getSortFields()))
                .setFilterQueries(getFilterQueries(searchParams.getFilterQueries()));
    }

    public SolrQuery createSumQuery(EkgSchemaField field, List<Expression> filterQueries) {

        SolrQuery solrQuery = createBasicSolrQuery()
                .setRows(0)
                .setFilterQueries(getFilterQueries(filterQueries));


        solrQuery.set("json.facet", "{sum:'sum(" + field.getName() + ")'}");

        return solrQuery;
    }

    /**
     * Creates a basic "match all" Solr query.
     *
     * @return The Solr query.
     */
    private SolrQuery createBasicSolrQuery() {
        return new SolrQuery("*:*")
                .setParam(CommonParams.WT, CommonParams.JAVABIN);
                // Note: PREFER_LOCAL_SHARDS was removed in Solr 9 - local shard preference is now default behavior
    }

    /**
     * Returns all Solr fields required for binding documents to the given entity class. Additionally, the given
     * {@link SortField} will also be included, even if it is not part of the entity class.
     *
     * @param type       the type of the entities
     * @param sortFields the field on which to sort
     * @return the Solr fields
     */
    private String[] getFieldList(Class<?> type, Collection<SortField> sortFields) {
        Set<String> solrFields = new HashSet<>(cachedSolrFields.computeIfAbsent(type,
                clazz -> FieldReader.getSchemaFields(clazz).keySet()));

        if (solrFields.isEmpty()) {
            throw new IllegalStateException(
                    "No Solr field annotations found on class " + type.getCanonicalName());
        }

        // The /export handler requires that all sort attributes are in the field list. Thus the sort fields must be
        // included even if they are not part of the entity class.
        for (SortField sortField : sortFields) {
            solrFields.add(sortField.getField().getName());
        }

        return solrFields.toArray(new String[0]);
    }

    /**
     * Converts the given PSMG-Hub sort fields to Solr sort clauses.
     *
     * @param sortFields The sort fields or {@code null}.
     * @return The sort clauses.
     */
    private List<SolrQuery.SortClause> getSortClauses(Collection<SortField> sortFields) {
        if (sortFields == null) {
            return Collections.emptyList();
        }

        return sortFields.stream()
                .map(this::toSortClause)
                .collect(Collectors.toList());
    }

    /**
     * Renders the given filter expressions into Solr filter queries.
     *
     * @param expressions The filter expressions or {@code null}.
     * @return The filter queries.
     */
    private String[] getFilterQueries(Collection<Expression> expressions) {
        if (expressions == null) {
            return new String[0];
        }

        return expressions.stream()
                .map(renderer::render)
                .toArray(String[]::new);
    }

    /**
     * Extracts the names of the given PSMG-Hub fields.
     *
     * @param fields The fields or {@code null}.
     * @return The field names.
     */
    private String[] extractFieldNames(Collection<Field> fields) {
        if (fields == null) {
            return new String[0];
        }

        return fields.stream()
                .map(Field::getName)
                .toArray(String[]::new);
    }

    /**
     * Converts a sort field into a Solr sort clause.
     *
     * @param sortField The sort field.
     * @return The sort clause.
     */
    private SolrQuery.SortClause toSortClause(SortField sortField) {
        return new SolrQuery.SortClause(
                sortField.getField().getName(),
                sortField.getMode().toString());
    }
}
