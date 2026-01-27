package de.qaware.ekg.awb.metricanalyzer.bl.visualisation;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryComputeParams;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.repository.api.RepositoryClientAware;

/**
 * This interface represents series that can use to fetch and compute time series from the repositories.
 * Compute means TimeSeries post processing with vectorization, aggregation and smoothing.
 * Which data will fetched and how it will post computed can controlled via call parameters.
 */
public interface MetricQueryService extends RepositoryClientAware {

    /**
     * Gets all counters by the given criteria.
     *
     * @param filterParams the query parameter that define which metrics in which time range should be fetched
     * @param computeParams the compute parameters that define various setting for the post computing steps on the
     *                    fetched time series data like
     * @param metricLimit the maximum amount of metrics that should be fetched.
     *                    If more metrics matches to the query, it will aborted.
     * @return a response object that contains metadata to number of series, it's limits and the TimeSeries payload as list
     */
    ComputedTimeSeriesResponse getComputedTimeSeries(QueryFilterParams filterParams,
                                                     QueryComputeParams computeParams, int metricLimit);

}
