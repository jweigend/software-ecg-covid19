package de.qaware.ekg.awb.repository.bl;

import de.qaware.ekg.awb.repository.api.model.AbstractEt;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

import static de.qaware.ekg.awb.repository.api.schema.EkgSchemaField.*;

/**
 * Bean that represents a time series with values compressed
 * as byte[]
 */
public class BinaryTimeSeries extends AbstractEt {

    /**
     * The project the time series belongs to
     */
    @PersistedField(PROJECT_NAME)
    private String project;

    // ----------------- classic filter dimension ------------------

    /**
     * The host.
     */
    @PersistedField(TS_HOST_GROUP_NAME)
    private String hostGroup;

    /**
     * The host.
     */
    @PersistedField(TS_HOST_NAME)
    private String host;


    // -------------- cloud native filter dimension ----------------

    /**
     * The host.
     */
    @PersistedField(TS_NAMESPACE_NAME)
    private String namespace;

    /**
     * The host.
     */
    @PersistedField(TS_SERVICE_NAME)
    private String service;

    /**
     * The host.
     */
    @PersistedField(TS_POD_NAME)
    private String pod;

    /**
     * The host.
     */
    @PersistedField(TS_CONTAINER_NAME)
    private String container;



    // ----------------- generic filter dimension ------------------

    /**
     * The process.
     */
    @PersistedField(TS_PROCESS_NAME)
    private String process;

    /**
     * The group.
     */
    @PersistedField(TS_METRIC_GROUP)
    private String group;

    /**
     * The series.
     */
    @PersistedField(TS_MEASUREMENT)
    private String measurement;

    /**
     * The metric.
     */
    @PersistedField(TS_METRIC_NAME)
    private String metric;

    /**
     * The start.
     */
    @PersistedField(TS_START)
    private Date start;

    /**
     * The end.
     */
    @PersistedField(TS_STOP)
    private Date end;

    /**
     * The ag.
     */
    @PersistedField(TS_AGGREGATION_LEVEL)
    private String ag;

    /**
     * A key to access the series data at remote repositories than
     * split sources are in use. This key can be a serialized data structure
     * (for example at Prometheus integration) that stores various information's
     * to identify the searched series correctly.
     */
    @PersistedField(TS_REMOTE_SERIES_KEY)
    private String remoteSeriesKey;

    /**
     * The whole time series data with tuples of
     * timestamps (long) and values (double) stored
     * as byte array with 16 bytes per tuple
     */
    @PersistedField(TS_DATA)
    private byte[] data;

    /**
     * The amount of time series point in the whole
     * time series stored in the field 'data'
     */
    @PersistedField(TS_DATA_AMOUNT_VALUES)
    private Integer amountOfSeriesValues;

    /**
     * A hash key that includes all set filter dimensions and the metric name
     * it self. This key can used to find all time series records that belongs
     * together with a single filter argument.
     *
     * This attribute is primarily used by the EKG collector during the metric
     * compaction service.
     */
    @PersistedField(TS_GROUP_KEY_HASH)
    private Integer groupKeyHash;

    /**
     * Default constructor
     */
    public BinaryTimeSeries() {
        setType(DocumentType.TIME_SERIES.toString());
    }

    public int getGroupKeyHash() {
        return groupKeyHash;
    }

    public void setGroupKeyHash(int groupKeyHash) {
        this.groupKeyHash = groupKeyHash;
    }

    public String getProjectName() {
        return project;
    }

    public String getHostGroup() {
        return hostGroup;
    }

    public void setHostGroup(String hostGroup) {
        this.hostGroup = hostGroup;
    }

    public String getRemoteSeriesKey() {
        return remoteSeriesKey;
    }

    public void setRemoteSeriesKey(String remoteSeriesKey) {
        this.remoteSeriesKey = remoteSeriesKey;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public void setProjectName(String projectName) {
        this.project = projectName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getMetricGroup() {
        return group;
    }

    public void setMetricGroup(String group) {
        this.group = group;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getMetricName() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    /**
     * Get the start timestamp of this timeseries.
     *
     * @return the start timestamp.
     */
    public long getStart() {
        return start != null ? start.getTime() : -1;
    }

    /**
     * Get the start timestamp of this timeseries.
     *
     * @param start the start timestamp.
     */
    public void setStart(long start) {
        this.start = new Date(start);
    }

    /**
     * Get the end timestamp of this timeseries.
     *
     * @return the end timestamp.
     */
    public long getEnd() {
        return end != null ? end.getTime() : -1;
    }

    /**
     * Set the end timestamp of this timeseries.
     *
     * @param end the end timestamp.
     */
    public void setEnd(long end) {
        this.end = new Date(end);
    }

    public String getAg() {
        return ag;
    }

    public void setAg(String ag) {
        this.ag = ag;
    }

    /**
     * @return the data of the time series (points)
     */
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.amountOfSeriesValues = data.length / 16;
    }

    public int getAmountOfSeriesValues() {
        return amountOfSeriesValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BinaryTimeSeries)) {
            return false;
        }

        BinaryTimeSeries that = (BinaryTimeSeries) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getProjectName(), that.getProjectName())
                .append(getHost(), that.getHost())
                .append(getProcess(), that.getProcess())
                .append(getMetricGroup(), that.getMetricGroup())
                .append(getMeasurement(), that.getMeasurement())
                .append(getMetricName(), that.getMetricName())
                .append(getStart(), that.getStart())
                .append(getEnd(), that.getEnd())
                .append(getAg(), that.getAg())
                .append(getData(), that.getData())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getProjectName())
                .append(getHost())
                .append(getProcess())
                .append(getMetricGroup())
                .append(getMeasurement())
                .append(getMetricName())
                .append(getStart())
                .append(getEnd())
                .append(getAg())
                .append(getData())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("project", project)
                .append("host", host)
                .append("process", process)
                .append("group", group)
                .append("measurement", measurement)
                .append("metric", metric)
                .append("start", start)
                .append("end", end)
                .append("ag", ag)
                .append("data", data)
                .toString();
    }
}
