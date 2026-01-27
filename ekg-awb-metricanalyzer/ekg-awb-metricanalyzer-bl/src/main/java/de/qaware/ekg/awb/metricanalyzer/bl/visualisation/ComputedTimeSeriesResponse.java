package de.qaware.ekg.awb.metricanalyzer.bl.visualisation;

import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;

import java.util.ArrayList;
import java.util.List;

public class ComputedTimeSeriesResponse {

    private long totalSeries;

    private int usedMaxSeriesLimit;

    private boolean requestAborted = false;

    private Throwable occurredError = null;

    List<TimeSeries> timeSeries = new ArrayList<>();

    public ComputedTimeSeriesResponse(long totalHits, int maxMetricLimit) {
        this.usedMaxSeriesLimit = maxMetricLimit;
        this.totalSeries = totalHits;
        this.requestAborted = true;
    }

    public ComputedTimeSeriesResponse(int maxMetricLimit, RepositoryException exception) {
        this.usedMaxSeriesLimit = maxMetricLimit;
        this.totalSeries = 0;
        this.requestAborted = true;
        this.occurredError = exception;
    }

    public ComputedTimeSeriesResponse(boolean requestAborted, int maxMetricLimit) {
        this.requestAborted = requestAborted;
        this.usedMaxSeriesLimit = maxMetricLimit;
    }

    public ComputedTimeSeriesResponse(List<TimeSeries> timeSeries, int maxMetricLimit) {
        this.timeSeries = timeSeries;
        this.usedMaxSeriesLimit = maxMetricLimit;
        this.totalSeries = timeSeries.size();
    }

    public Throwable getOccurredError() {
        return occurredError;
    }

    public long getTotalHits() {
        return totalSeries;
    }

    public int getUsedMaxSeriesLimit() {
        return usedMaxSeriesLimit;
    }

    public boolean isRequestAborted() {
        return requestAborted || isMaxSeriesLimitExceeded();
    }

    public boolean isMaxSeriesLimitExceeded() {
        return totalSeries > usedMaxSeriesLimit;
    }

    public List<TimeSeries> getTimeSeries() {
        return timeSeries;
    }

    public boolean hasErrorPayload() {
        return occurredError != null;
    }
}
