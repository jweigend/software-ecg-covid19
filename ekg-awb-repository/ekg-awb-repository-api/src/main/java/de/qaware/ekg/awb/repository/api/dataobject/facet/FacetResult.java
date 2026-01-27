package de.qaware.ekg.awb.repository.api.dataobject.facet;

import de.qaware.ekg.awb.repository.api.dataobject.status.QueryStatus;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A result of a facet search.
 */
public class FacetResult {

    private List<Facet> facets;

    private final QueryStatus queryStatus;

    /**
     * Constructs a {@link FacetResult}.
     *
     * @param facets the result facets
     * @param queryStatus the query status
     */
    public FacetResult(List<Facet> facets, QueryStatus queryStatus) {
        Validate.notNull(facets);

        this.facets = Collections.unmodifiableList(new ArrayList<>(facets));
        this.queryStatus = queryStatus;
    }

    /**
     * Returns the facets.
     *
     * @return the facets
     */
    public List<Facet> getFacets() {
        return Collections.unmodifiableList(facets);
    }

    /**
     * Returns the Status information of the executed query.
     *
     * @return the query status
     */
    public QueryStatus getQueryStatus() {
        return queryStatus;
    }

    /**
     * Tells whether this search result is empty.
     *
     * @return whether this search result is empty
     */
    public boolean isEmpty() {
        return facets.isEmpty();
    }

    /**
     * Returns the actual result size.
     *
     * @return actual result size. Equal to {@code getFacets().size()}
     */
    public int size() {
        return facets.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacetResult that = (FacetResult) o;
        return Objects.equals(facets, that.facets) &&
                Objects.equals(queryStatus, that.queryStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(facets, queryStatus);
    }
}
