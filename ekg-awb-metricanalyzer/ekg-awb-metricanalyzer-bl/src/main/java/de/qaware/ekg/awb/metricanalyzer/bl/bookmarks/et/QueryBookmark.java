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
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static de.qaware.ekg.awb.repository.api.schema.EkgSchemaField.*;

/**
 * Represents a bookmark that stores a query for several metrics, log entries or other.
 *
 * @param <T> The concrete type of the created query context.
 */
public abstract class QueryBookmark<T extends QueryFilterParams> extends Bookmark {

    @PersistedField(PROJECT_NAME)
    private String project;

    @PersistedField(TS_HOST_NAME)
    private String host;

    @PersistedField(TS_METRIC_GROUP)
    private String group;

    @PersistedField(TS_MEASUREMENT)
    private String measurement;

    @PersistedField(TS_PROCESS_NAME)
    private String process;

    @PersistedField(TS_START)
    private Long start;

    @PersistedField(TS_STOP)
    private Long stop;

    @PersistedField(TS_IMPORT_DATE)
    private Long importDate;


    protected QueryBookmark() {
        super(DocumentType.METRIC_BOOKMARK);
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getHost() {
        return host;
    }

    public String getGroup() {
        return group;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getProcess() {
        return process;
    }

    /**
     * get the start value of the query.
     *
     * @return the start value
     */
    public long getStart() {
        return start != null ? start : - 1;
    }

    public void setStart(Long start) {
        this.start = start != null ?  start : -1;
    }

    /**
     * get the stop value of the query.
     *
     * @return the stop value.
     */
    public long getStop() {
        return stop != null ? stop : -1;
    }

    public void setStop(Long stop) {
        this.stop = stop != null ?  stop : -1;
    }

    public void setImportDate(Long importDate) {
        this.importDate = importDate;
    }


    /**
     * @return the date time when the import started
     */
    public long getImportDate() {
        return importDate == null ? -1 : importDate;
    }



    /**
     * Get the {@link Bookmark} as regular {@link QueryFilterParams}.
     *
     * @return The {@link QueryFilterParams} for the {@link Bookmark}.
     */
    public abstract T asQueryContext();

    /**
     * Add the {@link Bookmark Bookmarks} values to the {@link QueryFilterParams}.
     *
     * @param builder The builder for a new {@link QueryFilterParams}.
     */
    protected void buildQueryContext(QueryFilterParams.Builder<T> builder) {
        builder.withProject(project)
                .withHost(host)
                .withMetricGroup(group)
                .withMeasurement(measurement)
                .withProcess(process)
                .withStart(start)
                .withStop(stop);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QueryBookmark)) {
            return false;
        }
        QueryBookmark that = (QueryBookmark) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getProject(), that.getProject())
                .append(getHost(), that.getHost())
                .append(getGroup(), that.getGroup())
                .append(getMeasurement(), that.getMeasurement())
                .append(getProcess(), that.getProcess())
                .append(getStart(), that.getStart())
                .append(getStop(), that.getStop())
                .append(getImportDate(), that.getImportDate())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getProject())
                .append(getHost())
                .append(getGroup())
                .append(getMeasurement())
                .append(getProcess())
                .append(getStart())
                .append(getStop())
                .append(getImportDate())
                .toHashCode();
    }

    /**
     * Builder for new {@link QueryBookmark}.
     *
     * @param <T> The type of the real builder.
     */
    public abstract static class Builder<T extends QueryBookmark> extends Bookmark.Builder<T> {
        protected String series;
        protected String group;
        protected String host;
        protected String measurement;
        protected String process;
        protected long start;
        protected long stop;
        protected long importDate;

        /**
         * Init an empty {@link QueryBookmark.Builder}.
         */
        public Builder() {
        }

        /**
         * Init a new builder with predefined values.
         *
         * @param context The values to predefine.
         */
        protected Builder(QueryFilterParams context) {
            this.series = context.getProjectName();
            this.host = context.getHostName();
            this.group = context.getMetricGroupName();
            this.measurement = context.getMeasurementName();
            this.process = context.getProcessName();
            this.start = context.getStart();
            this.stop = context.getEnd();
            this.importDate = context.getImportDate();
        }

        /**
         * Create a builder from an existing bookmark.
         *
         * @param bookmark the initial values for the builder.
         */
        protected Builder(QueryBookmark bookmark) {
            super(bookmark);
            this.series = bookmark.getProject();
            this.host = bookmark.getHost();
            this.group = bookmark.getGroup();
            this.measurement = bookmark.getMeasurement();
            this.importDate = bookmark.getImportDate();
            this.process = bookmark.getProcess();
            this.start = bookmark.getStart();
            this.stop = bookmark.getStop();
        }

        /**
         * Add group to builder.
         *
         * @param group the group name
         * @return this for fluent interface
         */
        public Builder<T> withGroup(String group) {
            this.group = group;
            return this;
        }

        /**
         * Add series to builder.
         *
         * @param series the series name
         * @return this for fluent interface
         */
        public Builder<T> withProject(String series) {
            this.series = series;
            return this;
        }

        /**
         * Add series to builder.
         *
         * @param measurement the series name
         * @return this for fluent interface
         */
        public Builder<T> withMeasurement(String measurement) {
            this.measurement = measurement;
            return this;
        }

        /**
         * Add host to builder.
         *
         * @param host the host name
         * @return this for fluent interface
         */
        public Builder<T> withHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Add process to builder.
         *
         * @param process the process name
         * @return this for fluent interface
         */
        public Builder<T> withProcess(String process) {
            this.process = process;
            return this;
        }

        /**
         * Set the builder value "importDate"
         *
         * @param importDate import date
         * @return fluent builder interface
         */
        public Builder<T> withImportDate(long importDate) {
            this.importDate = importDate;
            return this;
        }

        /**
         * Add start to builder.
         *
         * @param start the start name
         * @return this for fluent interface
         */
        public Builder<T> withStart(long start) {
            this.start = start;
            return this;
        }

        /**
         * Add stop to builder.
         *
         * @param stop the stop name
         * @return this for fluent interface
         */
        public Builder<T> withStop(long stop) {
            this.stop = stop;
            return this;
        }

        /**
         * Instantiates a new bookmark with the given bookmark values.
         *
         * @param bookmark The new bookmark
         */
        @Override
        protected void build(T bookmark) {
            super.build(bookmark);
            QueryBookmark b = bookmark;
            b.setProject(this.series);
            b.host = this.host;
            b.group = this.group;
            b.measurement = this.measurement;
            b.process = this.process;
            b.setStart(this.start);
            b.setStop(this.stop);
            b.setImportDate(this.importDate);
        }
    }
}
