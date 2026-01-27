package de.qaware.ekg.awb.repository.api.dataobject.search;

import de.qaware.ekg.awb.repository.api.dataobject.status.QueryStatus;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A result of a regular search.
 *
 * @param <T> the type of the returned entities
 */
public class SearchResult<T> {

    private final List<T> rows;

    private final QueryStatus queryStatus;

    /**
     * Constructs a new {@link SearchResult}.
     *
     * @param rows        the row
     * @param queryStatus the query status
     */
    public SearchResult(List<T> rows, QueryStatus queryStatus) {
        Validate.notNull(rows);

        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
        this.queryStatus = queryStatus;
    }

    /**
     * Returns the rows.
     *
     * @return the rows
     */
    public List<T> getRows() {
        return Collections.unmodifiableList(rows);
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
        return rows.isEmpty();
    }

    /**
     * Get the actual result size.
     *
     * @return actual result size. Equal to {@code getRows().size()}
     */
    public int size() {
        return rows.size();
    }

}
