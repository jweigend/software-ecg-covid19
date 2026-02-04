//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Stack;

/**
 * Represents a bookmark that stores a search query for metrics.
 */
public final class MetricQueryBookmark extends QueryBookmark<QueryFilterParams> {

    private String metric;

    private String exclude;

    private boolean regex;

    private boolean multiMetricMode;

    private boolean expertMode;

    private String rawQuery;

    private Stack<MetricQueryBookmark> subBookmarks;

    public String getExclude() {
        return exclude;
    }

    public boolean isRegex() {
        return regex;
    }

    public String getMetric() {
        return metric;
    }

    public boolean isMultiMetricMode() {
        return multiMetricMode;
    }

    public boolean isExpertMode() {
        return expertMode;
    }


    public String getRawQuery() {
        return rawQuery;
    }

    /**
     * Get the Bookmark as regular {@link QueryFilterParams}.
     *
     * @return The {@link QueryFilterParams} for the Bookmark.
     */
    @Override
    public QueryFilterParams asQueryContext() {
        QueryFilterParams.Builder builder = new QueryFilterParams.Builder()
                .withMetric(metric)
                .withExclude(exclude)
                .withMultiMetricMode(multiMetricMode)
                .withExpertMode(expertMode)
                .withRawQuery(rawQuery);

        buildQueryContext(builder);
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetricQueryBookmark that = (MetricQueryBookmark) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(isRegex(), that.isRegex())
                .append(isMultiMetricMode(), that.isMultiMetricMode())
                .append(getRawQuery(), that.getRawQuery())
                .append(isExpertMode(), that.isExpertMode())
                .append(getMetric(), that.getMetric())
                .append(getExclude(), that.getExclude())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getMetric())
                .append(getExclude())
                .append(isRegex())
                .append(isMultiMetricMode())
                .append(isExpertMode())
                .toHashCode();
    }


    /**
     * Returns a Stack of Sub bookmarks
     *
     * @return a Stack of Sub bookmarks
     */
    public Stack<MetricQueryBookmark> getSubBookmarks() {
        return subBookmarks == null ? new Stack<>() : subBookmarks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("metric", metric)
                .append("exclude", exclude)
                .append("regex", regex)
                .append("multiMetricMode", multiMetricMode)
                .append("expertMode", expertMode)
                .append("rawQuery", rawQuery)
                .append("subBookmarks", subBookmarks)
                .toString();
    }

    /**
     * Builder to create a new {@link MetricQueryBookmark}.
     */
    public static class Builder extends QueryBookmark.Builder<MetricQueryBookmark> {
        private String exclude;
        private String metric;
        private boolean regex;
        private boolean multiMetricMode;
        private boolean expertMode;
        private String rawQuery;
        private Stack<MetricQueryBookmark> subBookmarks;

        /**
         * Init a new {@link MetricQueryBookmark.Builder} without any predefined values.
         */
        public Builder() {
        }

        /**
         * Init a new {@link MetricQueryBookmark.Builder MetricQueryBookmark builder}
         * and take the given log query to predefine the values.
         *
         * @param context the predefined values
         */
        public Builder(QueryFilterParams context) {
            super(context);
            this.exclude = context.getExcludeMetricName();
            this.multiMetricMode = context.isMultiMetricMode();
            this.expertMode = context.isExpertMode();
            this.rawQuery = context.getRawQuery();
            this.metric = context.getMetricName();
        }

        /**
         * Create a builder from an existing bookmark.
         *
         * @param bookmark the initial values for the builder.
         */
        public Builder(MetricQueryBookmark bookmark) {
            super(bookmark);
            this.exclude = bookmark.getExclude();
            this.regex = bookmark.isRegex();
            this.multiMetricMode = bookmark.isMultiMetricMode();
            this.expertMode = bookmark.isExpertMode();
            this.metric = bookmark.getMetric();
        }

        /**
         * Add exclude to builder.
         *
         * @param exclude the exclude name
         * @return this for fluent interface
         */
        public Builder withExclude(String exclude) {
            this.exclude = exclude;
            return this;
        }

        /**
         * Add metric to builder.
         *
         * @param metric the metric name
         * @return this for fluent interface
         */
        public Builder withMetric(String metric) {
            this.metric = metric;
            return this;
        }

        /**
         * Add Query bookmark metrics to builder.
         *
         * @param metrics the Query bookmark metric
         * @return this for fluent interface
         */
        public Builder withMetricQueryBookmarks(Stack<MetricQueryBookmark> metrics) {
            this.subBookmarks = metrics;
            return this;
        }

        /**
         * Clears the metrics values.
         *
         * @return this for fluent interface
         */
        public Builder clearMetrics() {
            metric = null;
            return this;
        }

        /**
         * Add regex to builder.
         *
         * @param regex the regex name
         * @return this for fluent interface
         */
        public Builder withRegex(boolean regex) {
            this.regex = regex;
            return this;
        }

        /**
         * Add multiMetricMode to builder.
         *
         * @param multiMetricMode the multiMetricMode name
         * @return this for fluent interface
         */
        public Builder withMultiMetricMode(boolean multiMetricMode) {
            this.multiMetricMode = multiMetricMode;
            return this;
        }

        /**
         * Add expertMode to builder.
         *
         * @param expertMode the expertMode name
         * @return this for fluent interface
         */
        public Builder withExpertMode(boolean expertMode) {
            this.expertMode = expertMode;
            return this;
        }

        /**
         * Add rawQuery to builder.
         *
         * @param rawQuery the rawQuery
         * @return this for fluent interface
         */
        public Builder withRawQuery(String rawQuery) {
            this.rawQuery = rawQuery;
            return this;
        }

        /**
         * Instantiates a new bookmark with the given bookmark values.
         *
         * @return The new bookmark
         */
        public MetricQueryBookmark build() {
            MetricQueryBookmark bookmark = new MetricQueryBookmark();
            build(bookmark);
            bookmark.exclude = this.exclude;
            bookmark.regex = this.regex;
            bookmark.multiMetricMode = this.multiMetricMode;
            bookmark.expertMode = this.expertMode;
            bookmark.rawQuery = this.rawQuery;
            bookmark.subBookmarks = this.subBookmarks;
            bookmark.metric = this.metric;

            return bookmark;
        }


    }
}
