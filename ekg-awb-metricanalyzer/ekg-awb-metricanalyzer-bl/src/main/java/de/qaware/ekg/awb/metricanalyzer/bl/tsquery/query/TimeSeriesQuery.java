package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query;

import java.util.ArrayList;
import java.util.List;

public class TimeSeriesQuery extends MetricQuery {

    public static final String INITIAL_CURSOR_ID = "*";

    private List<SortClause> sortClauses = new ArrayList<>();

    private int maxMetricLimit;

    private String cursorId = null;

    public TimeSeriesQuery(QueryFilterParams metricQueryParams, int maxMetricLimit) {
        super(metricQueryParams);
        this.maxMetricLimit = maxMetricLimit;
    }

    public TimeSeriesQuery addSortClause(SortClause sortClause) {
        sortClauses.add(sortClause);
        return this;
    }

    public QueryFilterParams getQueryParams() {
        return super.getQueryParams();
    }

    public void setCursorId(String cursorId) {
        this.cursorId = cursorId;
    }

    public String getCursorId() {
        return cursorId;
    }

    public int getMaxMetricLimit() {
        return maxMetricLimit;
    }

    public List<SortClause> getSortClauses() {
        return sortClauses;
    }
}
