package de.qaware.ekg.awb.repository.api.dataobject.facet;

import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.schema.Field;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 * A parameter set for a facet search.
 */
public class FacetParams {

    private final List<Field> facetFields;
    private final Set<Field> facetMissing;
    private final List<Expression> filterQueries;
    private FacetSort facetSort = FacetSort.COUNT;
    private int limit = -1; // unlimited
    private int minCount = 0;

    /**
     * Default constructor.
     */
    public FacetParams() {
        facetFields = new ArrayList<>();
        facetMissing = new HashSet<>();
        filterQueries = new ArrayList<>();
    }

    /**
     * Copy contructor.
     *
     * @param other the other facet param
     */
    public FacetParams(final FacetParams other) {
        this.limit = other.limit;
        this.minCount = other.minCount;
        this.facetSort = other.facetSort;
        this.facetFields = new ArrayList<>(other.facetFields);
        this.facetMissing = new HashSet<>(other.facetMissing);
        this.filterQueries = new ArrayList<>(other.filterQueries);
    }

    /**
     * Add facet fields.
     * <p/>
     * The fields needs to be indexed.
     *
     * @param facetFields facet fields
     * @return this instance
     */
    public FacetParams withFacetFields(List<Field> facetFields) {
        this.facetFields.addAll(facetFields);
        return this;
    }

    /**
     * Overwrites the default sort mode of
     * the facet query.
     * <p/>
     *
     * @param facetSort the sort mode that is either the count of the facet occurrence or the lexicographic order
     * @return this instance
     */
    public FacetParams withSort(FacetSort facetSort) {
        this.facetSort = facetSort;
        return this;
    }

    /**
     * Add facet fields.
     * <p/>
     * The fields needs to be indexed.
     *
     * @param facetFields facet fields
     * @return this instance
     */
    public FacetParams withFacetFields(Field... facetFields) {
        Collections.addAll(this.facetFields, facetFields);
        return this;
    }

    /**
     * Returns the fields for which the special facet value {@code null} is enabled,
     * to count the results that have no facet value.
     *
     * @return The facet fields.
     */
    public Set<Field> getFacetMissing() {
        return Collections.unmodifiableSet(facetMissing);
    }

    /**
     * Enables the special facet value {@code null} for the given fields,
     * to count the results that have no facet value.
     * <p/>
     * The fields needs to be indexed.
     *
     * @param facetFields facet fields
     * @return this instance
     */
    public FacetParams withFacetMissing(Collection<Field> facetFields) {
        this.facetMissing.addAll(facetFields);
        return this;
    }

    /**
     * Enables the special facet value {@code null} for the given fields,
     * to count the results that have no facet value.
     * <p/>
     * The fields needs to be indexed.
     *
     * @param facetFields facet fields
     * @return this instance
     */
    public FacetParams withFacetMissing(Field... facetFields) {
        Collections.addAll(this.facetMissing, facetFields);
        return this;
    }

    /**
     * Add filter queries.
     *
     * @param filterQueries filter queries
     * @return this {@code SearchParams} instance
     */
    public FacetParams withFilterQueries(List<Expression> filterQueries) {
        this.filterQueries.addAll(filterQueries);
        return this;
    }

    /**
     * Add filter queries.
     *
     * @param filterQueries filter queries
     * @return this {@code SearchParams} instance
     */
    public FacetParams withFilterQueries(Expression... filterQueries) {
        Collections.addAll(this.filterQueries, filterQueries);
        return this;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * This param indicates the maximum number of constraint counts that should be returned for the facet fields.
     * A negative value means unlimited. This parameter can be specified on a per field basis to indicate a
     * separate limit for certain fields.
     *
     * @param limit the facet limit
     * @return this {@code SearchParams} instance
     */
    public FacetParams withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public int getMinCount() {
        return minCount;
    }

    /**
     * This param indicates the minimum counts for facet fields should be included in the response.
     * This parameter can be specified on a per field basis.
     *
     * @param minCount the min count
     * @return this {@code SearchParams} instance
     */
    public FacetParams withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    /**
     * Returns the filter queries.
     *
     * @return the filter queries
     */
    public List<Expression> getFilterQueries() {
        return Collections.unmodifiableList(filterQueries);
    }

    /**
     * Returns the facet field.
     *
     * @return the facet fields
     */
    public List<Field> getFacetFields() {
        return Collections.unmodifiableList(facetFields);
    }

    public String getFacetSort() {
        return facetSort.getName();
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.facetFields)
                .append(this.facetMissing)
                .append(this.filterQueries)
                .append(this.limit)
                .append(this.minCount)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FacetParams other = (FacetParams) obj;
        return new EqualsBuilder()
                .append(this.facetFields, other.facetFields)
                .append(this.facetMissing, other.facetMissing)
                .append(this.filterQueries, other.filterQueries)
                .append(this.limit, other.limit)
                .append(this.minCount, other.minCount)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("facetFields", facetFields)
                .append("facetMissing", facetMissing)
                .append("filterQueries", filterQueries)
                .append("limit", limit)
                .append("minCount", minCount)
                .toString();
    }

    public enum FacetSort {
        COUNT("count"),
        LEXICOGRAPHIC("index");

        private String name;

        FacetSort(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
