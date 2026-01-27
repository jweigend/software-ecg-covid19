package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query;

import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class TimeSeriesQueryResponse {

    private String cursorId;

    private long totalHits;

    private boolean requestAborted;

    private Collection<TimeSeries> responseData = Collections.synchronizedList(new ArrayList<>());

    /**
     *
     * @param cursorId
     */
    public TimeSeriesQueryResponse(String cursorId, long totalHits, boolean requestAborted) {
        this.cursorId = cursorId;
        this.totalHits = totalHits;
        this.requestAborted = requestAborted;
    }

    /**
     *
     * @return
     */
    public boolean isRequestAborted() {
        return requestAborted;
    }

    public void setRequestAborted(boolean requestAborted) {
        this.requestAborted = requestAborted;
    }

    /**
     *
     * @return
     */
    public long getTotalHits() {
        return totalHits;
    }

    /**
     *
     * @return
     */
    public boolean isConsumed() {
       return cursorId == null || responseData == null || responseData.isEmpty();
    }

    /**
     *
     * @return
     */
    public Collection<TimeSeries> getData() {
       return responseData;
    }

    /**
     *
     * @return
     */
    public String getCursorId() {
        return cursorId;
    }

    public void addTimeSeries(TimeSeries timeSeries) {
        responseData.add(timeSeries);
    }
}
