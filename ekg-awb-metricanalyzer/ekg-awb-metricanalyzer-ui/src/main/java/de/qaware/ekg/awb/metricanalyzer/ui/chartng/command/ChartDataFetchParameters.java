package de.qaware.ekg.awb.metricanalyzer.ui.chartng.command;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryComputeParams;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;

/**
 * A container that will used to transport some beans with filters,
 * compute settings and further control properties as single object instance
 * that can also serialized to used it in bookmarks.
 *
 * The container data itself is focused to control the data loading action
 * that will fetch the time series data for the chart.
 */
public class ChartDataFetchParameters {

    private QueryComputeParams queryComputeParams = null;

    private QueryFilterParams queryFilterParams;

    private boolean resetChartAxis = true;

    //=================================================================================================================
    // constructors for various use cases
    //=================================================================================================================

    public ChartDataFetchParameters() {
        queryFilterParams = new QueryFilterParams();
    }

    public ChartDataFetchParameters(QueryComputeParams queryComputeParams) {
        this();
        this.queryComputeParams = queryComputeParams;
    }

    public ChartDataFetchParameters(QueryFilterParams queryFilterParams, QueryComputeParams queryComputeParams) {
        this.queryFilterParams = queryFilterParams;
        this.queryComputeParams = queryComputeParams;
    }

    public ChartDataFetchParameters(QueryFilterParams queryFilterParams,
                                    QueryComputeParams queryComputeParams, boolean resetChartAxis) {
        this.queryFilterParams = queryFilterParams;
        this.queryComputeParams = queryComputeParams;
        this.resetChartAxis = resetChartAxis;
    }

    //=================================================================================================================
    // accessor API
    //=================================================================================================================

    public boolean doResetChartAxis() {
        return resetChartAxis;
    }

    public QueryComputeParams getQueryComputeParams() {
        return queryComputeParams;
    }

    public QueryFilterParams getQueryFilterParams() {
        return queryFilterParams;
    }

    public void setQueryComputeParams(QueryComputeParams queryComputeParams) {
        this.queryComputeParams = queryComputeParams;
    }

    public void setQueryFilterParams(QueryFilterParams queryFilterParams) {
        this.queryFilterParams = queryFilterParams;
    }
}
