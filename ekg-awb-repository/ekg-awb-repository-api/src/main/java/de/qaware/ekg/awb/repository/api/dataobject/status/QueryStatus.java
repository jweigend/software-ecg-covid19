package de.qaware.ekg.awb.repository.api.dataobject.status;

import java.util.Objects;

/**
 * Status information of an executed query.
 */
public class QueryStatus {
    private final long numberOfHits;
    private final int queryTime;
    private String cursor;

    /**
     * Constructs a {@link QueryStatus}.
     *
     * @param numberOfHits the number of hits, i.e. number of documents matching to the query. This number may be higher
     * than the amount of returned entities (if numberOfHits > maxRows).
     * @param queryTime the time in milliseconds it took to process the query in the search index.
     * @param cursor the next cursor id used for fast paging
     */
    public QueryStatus(long numberOfHits, int queryTime, String cursor) {
        this.numberOfHits = numberOfHits;
        this.queryTime = queryTime;
        this.cursor = cursor;
    }

    /**
     * Returns the next cursor id used for fast paging
     *
     * @return the next cursor id
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Returns the number of hits, i.e. number of documents matching to the query. This number may be higher
     * than the amount of returned entities (if numberOfHits > maxRows).
     *
     * @return the numberOfHits
     */
    public long getNumberOfHits() {
        return numberOfHits;
    }

    /**
     * Returns the time in milliseconds it took to process the query in the search index.
     *
     * @return the query time
     */
    public int getQueryTime() {
        return queryTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryStatus that = (QueryStatus) o;
        return numberOfHits == that.numberOfHits &&
                queryTime == that.queryTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfHits, queryTime);
    }
}
