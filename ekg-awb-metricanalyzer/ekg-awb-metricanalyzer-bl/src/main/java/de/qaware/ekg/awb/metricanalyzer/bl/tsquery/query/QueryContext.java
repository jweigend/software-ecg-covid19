package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query;

public class QueryContext {

    private QueryFilterParams queryFilterParams;

    private QuerySourceParams querySourceParams;

    private QueryComputeParams queryComputeParams;


    public QueryFilterParams getQueryFilterParams() {
        return queryFilterParams;
    }

    public QuerySourceParams getQuerySourceParams() {
        return querySourceParams;
    }

    public QueryComputeParams getQueryComputeParams() {
        return queryComputeParams;
    }
}
