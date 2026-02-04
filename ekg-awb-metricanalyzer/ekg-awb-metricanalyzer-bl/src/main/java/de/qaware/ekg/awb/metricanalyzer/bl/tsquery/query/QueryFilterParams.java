//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.MetricGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Container;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Pod;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Service;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.Host;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.HostGroup;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetParams;
import de.qaware.ekg.awb.sdk.awbapi.repository.ImporterSourceRepository;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Context for querying several metrics, log entries and so on from backend.
 */
@SuppressWarnings("unused")
public class QueryFilterParams {

    /**
     * The project that defines the context and is the
     * most important filter of all.
     * Only data that belongs to the project will be in scope any
     * following query and other applied filters
     */
    private Project project;

    /**
     * The optional importer source definition that contains all
     * properties that used to access the remote data.
     * Only need to set than Project.isRemoteProject() == true
     */
    private ImporterSourceRepository importerSourceRepository;

    //-----------------------------------------------------------------------------------------------------------------
    //  classic filter dimensions
    //-----------------------------------------------------------------------------------------------------------------

    /**
     * a group of host for example a physical cluster
     */
    private HostGroup hostGroup;

    /**
     * a single node or device the series belongs to
     */
    private Host host;

    //-----------------------------------------------------------------------------------------------------------------
    //  cloud native filter dimensions
    //-----------------------------------------------------------------------------------------------------------------

    private Namespace namespace;

    private Service service;

    private Pod pod;

    private Container container;

    //-----------------------------------------------------------------------------------------------------------------
    //  generic filter dimensions
    //-----------------------------------------------------------------------------------------------------------------

    /**
     * The name of the measurement.
     * Normally defined by the analyst, who made multiple measurements
     * of the same system/KPI's
     */
    private Measurement measurement;

    /**
     * The (software) process the metric belongs to.
     */
    private Process process;

    /**
     * A group of metrics that combines a couple of metrics
     * that belongs (logically) together like Unix OS metrics,
     * JMX or WindowsPerformance counter metrics.
     */
    private MetricGroup metricGroup;

    /**
     * The name of a single metric
     */
    private Metric metric;

    /**
     * Multi metrics
     */
    private Set<Metric> metrics = Collections.emptySet();

    /**
     * The exclude.
     */
    private Metric exclude;

    /**
     * The expert mode.
     */
    private boolean isExpertMode;

    /**
     * Indicates if the multi metric mode is activated
     */
    private boolean isMultiMetricMode;

    /**
     * The start.
     */
    private long start = -1;

    /**
     * The stop.
     */
    private long stop = -1;

    /**
     * the date time when the import was started
     */
    private long importDate;

    /**
     * An additional query part appended as it is to the of all field queries.
     */
    private String rawQuery;

    /**
     * The sort mode in case of facet queries.
     */
    private FacetParams.FacetSort facetSort = FacetParams.FacetSort.LEXICOGRAPHIC;


    //================================================================================================================
    //  accessor API
    //================================================================================================================

    public String getFullQualifiedDisplayName() {

        StringBuilder displayNameBuilder = new StringBuilder();


        if (StringUtils.isNotBlank(getHostGroupName()) && !"*".equals(getHostGroupName())) {
            displayNameBuilder.append(getHostGroupName());
            displayNameBuilder.append(" | ");
        }

        if (StringUtils.isNotBlank(getHostName()) && !"*".equals(getHostName())) {
            displayNameBuilder.append(getHostName());
            displayNameBuilder.append(" | ");
        }

        if (StringUtils.isNotBlank(getNamespaceName()) && !"*".equals(getNamespaceName())) {
            displayNameBuilder.append(getNamespaceName());
            displayNameBuilder.append(" | ");
        }

        if (StringUtils.isNotBlank(getPodName()) && !"*".equals(getPodName())) {
            displayNameBuilder.append(getPodName());
            displayNameBuilder.append(" | ");
        }

        if (StringUtils.isNotBlank(getProcessName()) && !"*".equals(getProcessName())) {
            displayNameBuilder.append(getProcessName());
            displayNameBuilder.append(" | ");
        }

        if (StringUtils.isNotBlank(getMetricGroupName()) && !"*".equals(getMetricGroupName())) {
            displayNameBuilder.append(getMetricGroupName());
            displayNameBuilder.append(" | ");
        }

        displayNameBuilder.append(getMetricName());

        return displayNameBuilder.toString();
    }

    /**
     * Gets the exclude metric name. Note: Exclude may not be null. There is no setter and if we pass null in the
     * constructor, an empty metric is saved.
     *
     * @return the exclude metric name
     */
    public String getExcludeMetricName() {
        return exclude != null ? exclude.getName() : null;
    }

    /**
     * Gets the exclude metric.
     *
     * @return the exclude metric
     */
    public Metric getExcludeMetric() {
        return this.exclude;
    }

    /**
     * Gets the expert mode.
     *
     * @return the expert mode
     */
    public boolean isExpertMode() {
        return this.isExpertMode;
    }

    /**
     * Gets the multi metric mode
     *
     * @return true if the context contains multiple metrics
     */
    public boolean isMultiMetricMode() {
        return this.isMultiMetricMode;
    }

    /**
     * Gets the metric name. The metric may not be null! For more explanation, see the getExcludeName() method.
     *
     * @return the metric name
     */
    public String getMetricName() {
        return metric.getName();
    }

    /**
     * Returns all metric names from this context
     *
     * @return List with metrics names in this query context
     */
    public List<String> getMetricNames() {
        return metrics.stream().map(Metric::getName).collect(Collectors.toList());
    }

    /**
     * Gets the metric.
     *
     * @return the metric
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Gets all metrics.
     *
     * @return all metrics.
     */
    public Set<Metric> getMetrics() {
        return Collections.unmodifiableSet(metrics);
    }


    public HostGroup getHostGroup() {
        return hostGroup;
    }

    public String getHostGroupName() {
        return hostGroup == null ? "" : hostGroup.getValueName();
    }

    public ImporterSourceRepository getImporterSourceRepository() {
        return importerSourceRepository;
    }

    public void setImporterSourceRepository(ImporterSourceRepository importerSourceRepository) {
        this.importerSourceRepository = importerSourceRepository;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public Host getHost() {
        return host;
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostName() {
        return host == null ? "" : host.getName();
    }


    public Namespace getNamespace() {
        return namespace;
    }

    public String getNamespaceName() {
        return namespace == null ? "" : namespace.getValueName();
    }

    public Service getService() {
        return service;
    }

    public String getServiceName() {
        return service == null ? "" : service.getValueName();
    }

    public Pod getPod() {
        return pod;
    }

    public String getPodName() {
        return pod == null ? "" : pod.getValueName();
    }

    public Container getContainer() {
        return container;
    }

    public String getContainerName() {
        return container == null ? "" : container.getValueName();
    }

    /**
     * Gets the series.
     *
     * @return the series
     */
    public Measurement getMeasurement() {
        return measurement;
    }

    /**
     * Gets the process.
     *
     * @return the process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Gets the project.
     *
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Gets the metricGroup.
     *
     * @return the metricGroup
     */
    public MetricGroup getMetricGroup() {
        return metricGroup;
    }

    /**
     * Gets the project name.
     *
     * @return the project name
     */
    public String getProjectName() {
        return project.getName();
    }


    public String isRemoteProject() {
        return "*";
    }

    /**
     * Gets the series name.
     *
     * @return the series name
     */
    public String getMeasurementName() {
        return measurement == null ? "" : measurement.getName();
    }

    /**
     * Gets the metricGroup name.
     *
     * @return the metricGroup name
     */
    public String getMetricGroupName() {
        return metricGroup == null ? "" : metricGroup.getName();
    }

    /**
     * Gets the process name.
     *
     * @return the process name
     */
    public String getProcessName() {
        return process == null ? "" : process.getName();
    }

    /**
     * Returns the import date
     *
     * @return the import date
     */
    public long getImportDate() {
        return importDate;
    }

    /**
     * Gets the start.
     *
     * @return the start
     */
    public long getStart() {
        if (start > 0) {
            return start; // if explicit set
        }
        if (measurement != null && measurement.getStart() < 0) {
            // if series set
            return measurement.getStart();
        }


        return -1;
    }

    public void setStart(double timestamp) {
        start = (long) timestamp;
    }

    /**
     * Gets the end.
     *
     * @return the end
     */
    public long getEnd() {
        if (stop >= 0) {
            // if explicit set
            return stop;
        }
        if (measurement != null && measurement.getEnd() >= 0) {
            // if series set
            return measurement.getEnd();
        }

        return -1;
    }

    public void setEnd(long endInMs) {
        stop = endInMs;
    }

    public void setEndAsDouble(double timestamp) {
        stop = (long) timestamp;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public FacetParams.FacetSort getFacetSort() {
        return facetSort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof QueryFilterParams)) {
            return false;
        }

        QueryFilterParams that = (QueryFilterParams) o;

        return new EqualsBuilder()
                .append(getProject(), that.getProject())
                .append(getHost(), that.getHost())
                .append(getHostGroup(), that.getHostGroup())
                .append(getNamespace(), that.getNamespace())
                .append(getService(), that.getService())
                .append(getPod(), that.getPod())
                .append(getContainer(), that.getContainer())
                .append(getMetricGroup(), that.getMetricGroup())
                .append(getMeasurement(), that.getMeasurement())
                .append(getProcess(), that.getProcess())
                .append(getImportDate(), that.getImportDate())
                .append(getStart(), that.getStart())
                .append(stop, that.stop)
                .append(getRawQuery(), that.getRawQuery())
                .append(getClass(), that.getClass())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getProject())
                .append(getHostGroup())
                .append(getHost())
                .append(getNamespace())
                .append(getService())
                .append(getPod())
                .append(getContainer())
                .append(getMetricGroup())
                .append(getMeasurement())
                .append(getProcess())
                .append(getImportDate())
                .append(getStart())
                .append(stop)
                .append(getRawQuery())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("project", project)
                .append("hostGroup", hostGroup)
                .append("host", host)
                .append("namespace", namespace)
                .append("service", service)
                .append("pod", pod)
                .append("container", container)
                .append("metricGroup", metricGroup)
                .append("measurement", measurement)
                .append("process", process)
                .append("importDate", importDate)
                .append("start", start)
                .append("stop", stop)
                .append("rawQuery", rawQuery)
                .toString();
    }

    public int getLimit() {
        return 500_000;
    }


    /**
     * Builder for query contexts or derived classes of query context
     *
     * @param <T> The concrete type for this query.
     */
    public static class Builder<T extends QueryFilterParams> {

        private String rawQuery;
        private Project project;
        private ImporterSourceRepository sourceRepositoryDefinition;

        private HostGroup hostGroup;
        private Host host;

        private Namespace namespace;
        private Service service;
        private Pod pod;
        private Container container;

        private Measurement measurement;
        private Process process;
        private MetricGroup metricGroup;

        private Metric metric;
        private Set<Metric> metrics = new ListOrderedSet<>();
        private Metric exclude;

        private boolean isExpertMode;
        private boolean isMultiMetricMode;

        private long start = -1;
        private long stop = -1;
        private long importDate;

        private FacetParams.FacetSort facetSort = FacetParams.FacetSort.LEXICOGRAPHIC;


        /**
         * Init a new query context builder without predefined values
         */
        public Builder() {
        }

        /**
         * Init the builder with predefined values.
         *
         * @param context the context to predefine values.
         */
        public Builder(T context) {
            this.project = context.getProject();
            this.sourceRepositoryDefinition = context.getImporterSourceRepository();

            this.hostGroup = context.getHostGroup();
            this.host = context.getHost();

            this.namespace = context.getNamespace();
            this.service = context.getService();
            this.pod = context.getPod();
            this.container = context.getContainer();

            this.measurement = context.getMeasurement();
            this.process = context.getProcess();
            this.metricGroup = context.getMetricGroup();

            this.metric = context.getMetric();
            this.metrics = context.getMetrics();
            this.exclude = context.getExcludeMetric();

            this.isExpertMode = context.isExpertMode();
            this.isMultiMetricMode = context.isMultiMetricMode();

            this.start = context.getStart();
            this.stop = context.getEnd();
            this.rawQuery = context.getRawQuery();
            this.importDate = context.getImportDate();
        }

        /**
         * Add metric to builder.
         *
         * @param metric the metric name
         * @return this for fluent interface
         */
        public Builder<T> withMetric(Metric metric) {
            this.metric = metric;
            return this;
        }

        /**
         * Add metric to builder.
         *
         * @param metric the metric name
         * @return this for fluent interface
         */
        public Builder<T> withMetric(String metric) {
            this.metric = Metric.valueOf(metric);
            return this;
        }

        /**
         * Add metrics to builder.
         *
         * @param metrics the metrics name
         * @return this for fluent interface
         */
        public Builder<T> withMetrics(Set<Metric> metrics) {
            this.metrics = new ListOrderedSet<>();
            if (metrics != null) {
                this.metrics.addAll(metrics);
            }
            return this;
        }

        /**
         * Add exclude to builder.
         *
         * @param exclude the exclude name
         * @return this for fluent interface
         */
        public Builder<T> withExclude(Metric exclude) {
            this.exclude = exclude;
            return this;
        }

        /**
         * Add exclude to builder.
         *
         * @param exclude the exclude name
         * @return this for fluent interface
         */
        public Builder<T> withExclude(String exclude) {
            this.exclude = Metric.valueOf(exclude);
            return this;
        }

        /**
         * Add isExpertMode to builder.
         *
         * @param isExpertMode the isExpertMode name
         * @return this for fluent interface
         */
        public Builder<T> withExpertMode(boolean isExpertMode) {
            this.isExpertMode = isExpertMode;
            return this;
        }

        /**
         * Add isMultiMetricMode to builder.
         *
         * @param isMultiMetricMode the isMultiMetricMode name
         * @return this for fluent interface
         */
        public Builder<T> withMultiMetricMode(boolean isMultiMetricMode) {
            this.isMultiMetricMode = isMultiMetricMode;
            return this;
        }

        /**
         * Add project to builder.
         *
         * @param project the project name
         * @return this for fluent interface
         */
        public Builder<T> withProject(Project project) {
            this.project = project;
            return this;
        }

        /**
         * Add project to builder.
         *
         * @param projectName the project name
         * @return this for fluent interface
         */
        public Builder<T> withProject(String projectName) {
            this.project = new Project(projectName);
            return this;
        }


        public Builder<T> withImporterSourceRepository(ImporterSourceRepository sourceRepositoryDefinition) {
            this.sourceRepositoryDefinition = sourceRepositoryDefinition;
            return this;
        }

        /**
         * Add a host group to builder.
         *
         * @param hostGroup the host group
         * @return this for fluent interface
         **/
        public Builder<T> withHostGroup(HostGroup hostGroup) {
            this.hostGroup = hostGroup;
            return this;
        }

        /**
         * Add host group name to builder.
         *
         * @param hostGroupName the name of the host group
         * @return this for fluent interface
         **/
        public Builder<T> withHostGroup(String hostGroupName) {
            this.hostGroup = new HostGroup(hostGroupName);
            return this;
        }

        /**
         * Add host to builder.
         *
         * @param host the host name
         * @return this for fluent interface
         **/
        public Builder<T> withHost(Host host) {
            this.host = host;
            return this;
        }

        /**
         * Add host to builder.
         *
         * @param host the host name
         * @return this for fluent interface
         **/
        public Builder<T> withHost(String host) {
            this.host = new Host(host);
            return this;
        }

        /**
         * Add namespace filter to builder.
         *
         * @param namespace a namespace entity
         * @return this for fluent interface
         **/
        public Builder<T> withNamespace(Namespace namespace) {
            this.namespace = namespace;
            return this;
        }

        /**
         * Add namespace filter to builder.
         *
         * @param namespaceName the name of the namespace
         * @return this for fluent interface
         **/
        public Builder<T> withNamespace(String namespaceName) {
            this.namespace = new Namespace(namespaceName);
            return this;
        }

        /**
         * Add service filter to builder.
         *
         * @param service a namespace entity
         * @return this for fluent interface
         **/
        public Builder<T> withService(Service service) {
            this.service = service;
            return this;
        }

        /**
         * Add service filter to builder.
         *
         * @param serviceName the name of the service
         * @return this for fluent interface
         **/
        public Builder<T> withService(String serviceName) {
            this.service = new Service(serviceName);
            return this;
        }

        /**
         * Add pod filter to builder.
         *
         * @param pod a Pod entity
         * @return this for fluent interface
         **/
        public Builder<T> withPod(Pod pod) {
            this.pod = pod;
            return this;
        }

        /**
         * Add pod filter to builder.
         *
         * @param podName the name of the pod
         * @return this for fluent interface
         **/
        public Builder<T> withPod(String podName) {
            this.pod = new Pod(podName);
            return this;
        }

        /**
         * Add container filter to builder.
         *
         * @param container a container entity
         * @return this for fluent interface
         **/
        public Builder<T> withContainer(Container container) {
            this.container = container;
            return this;
        }

        /**
         * Add container filter to builder.
         *
         * @param containerName the name of the container
         * @return this for fluent interface
         **/
        public Builder<T> withContainer(String containerName) {
            this.container = new Container(containerName);
            return this;
        }

        /**
         * Add metricGroup to builder.
         *
         * @param metricGroup the metricGroup name
         * @return this for fluent interface
         */
        public Builder<T> withMetricGroup(MetricGroup metricGroup) {
            this.metricGroup = metricGroup;
            return this;
        }

        /**
         * Add metricGroup to builder.
         *
         * @param group the metricGroup name
         * @return this for fluent interface
         */
        public Builder<T> withMetricGroup(String group) {
            this.metricGroup = new MetricGroup(group);
            return this;
        }

        /**
         * Add series to builder.
         *
         * @param measurement the series name
         * @return this for fluent interface
         */
        public Builder<T> withMeasurement(Measurement measurement) {
            this.measurement = measurement;
            return this;
        }

        /**
         * Add series to builder.
         *
         * @param measurement the series name
         * @return this for fluent interface
         */
        public Builder<T> withMeasurement(String measurement) {
            this.measurement = new Measurement(measurement, -1, -1);
            return this;
        }

        /**
         * Add process to builder.
         *
         * @param process the process name
         * @return this for fluent interface
         */
        public Builder<T> withProcess(Process process) {
            this.process = process;
            return this;
        }

        /**
         * Set the builder value "rawQuery"
         *
         * @param rawQuery raw query
         * @return fluent builder interface
         */
        public Builder<T> withRawQuery(String rawQuery) {
            this.rawQuery = rawQuery;
            return this;
        }

        /**
         * Add process to builder.
         *
         * @param process the process name
         * @return this for fluent interface
         */
        public Builder<T> withProcess(String process) {
            this.process = new Process(process, process);
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
         * Overwrites the default sort mode of
         * the facet query.
         * <p/>
         *
         * @param facetSort the sort mode that is either the count of the facet occurrence or the lexicographic order
         * @return this instance
         */
        public Builder<T> withSort(FacetParams.FacetSort facetSort) {
            this.facetSort = facetSort;
            return this;
        }

        /**
         * Build the query context
         *
         * @return the built query context.
         */
        public T build() {
            return build(new QueryFilterParams());
        }

        /**
         * Build the query context.
         *
         * @param queryFilterParams The instance to fill.
         * @return build query context.
         */
        @SuppressWarnings("unchecked")
        protected T build(QueryFilterParams queryFilterParams) {
            queryFilterParams.rawQuery = this.rawQuery;

            queryFilterParams.project = this.project != null ? this.project : Project.DEFAULT;

            // physical env series filters
            queryFilterParams.host = this.host != null ? this.host : Host.DEFAULT;
            queryFilterParams.hostGroup = this.hostGroup != null ? this.hostGroup : HostGroup.DEFAULT;
            ;

            // logical env series filters
            queryFilterParams.namespace = this.namespace != null ? this.namespace : Namespace.DEFAULT;
            queryFilterParams.pod = this.pod != null ? this.pod : Pod.DEFAULT;
            queryFilterParams.container = this.container != null ? this.container : Container.DEFAULT;
            queryFilterParams.service = this.service != null ? this.service : Service.DEFAULT;

            // common series filters
            queryFilterParams.metricGroup = this.metricGroup != null ? this.metricGroup : MetricGroup.DEFAULT;
            queryFilterParams.metric = this.metric != null ? this.metric : new Metric("*");
            queryFilterParams.exclude = this.exclude != null ? this.exclude : new Metric("");
            queryFilterParams.metrics = Collections.unmodifiableSet(ListOrderedSet.listOrderedSet(this.metrics));
            queryFilterParams.measurement = this.measurement != null ? this.measurement : Measurement.DEFAULT;
            queryFilterParams.process = this.process != null ? this.process : Process.DEFAULT;

            // extended query data
            queryFilterParams.isExpertMode = this.isExpertMode;
            queryFilterParams.isMultiMetricMode = this.isMultiMetricMode && !this.metrics.isEmpty();

            queryFilterParams.start = this.start;
            queryFilterParams.stop = this.stop;
            queryFilterParams.importDate = this.importDate;
            queryFilterParams.facetSort = this.facetSort;

            queryFilterParams.importerSourceRepository = this.sourceRepositoryDefinition;

            return (T) queryFilterParams;
        }
    }
}