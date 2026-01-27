package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query;

public class SortClause {

    private String field;

    private SortOrder order;

    private SortClause(String field, SortOrder order) {
        this.field = field;
        this.order = order;
    }

    public static SortClause asc(String field) {
        return new SortClause(field, SortOrder.ASC);
    }

    public static SortClause dsc(String field) {
        return new SortClause(field, SortOrder.DSC);
    }

    public String getField() {
        return field;
    }

    public SortOrder getOrder() {
        return order;
    }
}
