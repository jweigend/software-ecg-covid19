package de.qaware.ekg.awb.repository.api.dataobject.search;

import de.qaware.ekg.awb.repository.api.dataobject.status.QueryStatus;
import org.apache.commons.lang3.Validate;

import java.util.stream.Stream;

/**
 * Response wrapper that encapsulate a data stream
 * with additional status information beside of it.
 */
public class StreamingSearchResult<T> {

    private final QueryStatus queryStatus;

    private final Stream<T> resultStream;

    public StreamingSearchResult(QueryStatus queryStatus, Stream<T> resultStream) {
        Validate.notNull(resultStream);

        this.queryStatus = queryStatus;
        this.resultStream = resultStream;
    }

    public QueryStatus getQueryStatus() {
        return queryStatus;
    }

    public Stream<T> getResultStream() {
        return resultStream;
    }
}
