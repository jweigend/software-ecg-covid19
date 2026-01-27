package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query;

public class MetricQuery {

    private QueryFilterParams queryParams;

    public MetricQuery() {

    }

    public MetricQuery(QueryFilterParams queryParams) {
        this.queryParams = queryParams;
    }

    public QueryFilterParams getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(QueryFilterParams queryParams) {
        this.queryParams = queryParams;
    }
}
