package de.qaware.ekg.awb.metricanalyzer.bl.tsquery;

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
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.TimeSeriesQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.TimeSeriesQueryResponse;
import de.qaware.ekg.awb.repository.api.RepositoryClientAware;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface that represents services that provide the access to
 * time series and it metadata.
 * This interface has three groups of methods:
 * - one to query time series data (mass data) with paging/cursor function
 * - a second with some methods query facet data about the time series meta data (filter data/labels)
 * - a third with a couple of methods to write metric data to the repository.
 */
public interface MetricDataAccessService extends RepositoryClientAware {

    /**
     * Deletes all time series from the repository that are assigned to the project
     * with the specified project name.
     * All changes will committed immediately and can't reverted.
     *
     * @param projectName the project name that will used to find the series that should deleted
     */
    void deleteTimeSeriesByProjectName(String projectName);

    /**
     * Add an time series to the project that is defined in the meta data of the time series
     * itself. The implementation will use an calculated key based on the series metadata
     * to identify existing series. Using the 'overrideExistingSeries' flag the caller can
     * control if already existing series will overwritten or not.
     *
     * To persist the series and make them visible for the UI the dedicated commit method has to call.
     *
     * @param timeSeriesStream the time series that should persist in the EKG repository.
     * @throws RepositoryException thrown if the write process fails for example as result of invalid input data.
     */
    void addEntities(Stream<TimeSeries> timeSeriesStream, boolean overrideExistingSeries) throws RepositoryException;


    /**
     * Fetches time series data from persistence storage that matches to the specified filters in
     * the given query and post filter all values in the series that are not inside an (optional)
     * specified time interval
     *
     * @param query a query instance that defines the filter parameters for time series search
     * @return a response with 0 to n time series data
     */
    TimeSeriesQueryResponse queryTimeSeriesData(TimeSeriesQuery query) throws RepositoryException ;

    //==================================================================================================================
    // facet methods for classic / physical time series filter dimensions
    //==================================================================================================================

    /**
     * Fetches and returns the facet data for host groups / cluster.
     * The service method will respect the specified query filters and proceed the query
     * with all filter except filter restrictions of the HostGroup filter dimension self.
     * This will ensure that the facet call don't restricts itself in selection
     * other filter values for this dimension.
     *
     * @param query the filter query that will used to find the right set of facet that matches to the filter set.
     * @return the facets based on the given query sorted in the way defined by the FacetParams.FacetSort property
     */
    List<HostGroup> getHostGroups(MetricQuery query);


    /**
     * Fetches and returns the facet data for the hosts / physical/vm nodes.
     * The service method will respect the specified query filters and proceed the query
     * with all filter except filter restrictions of the Host filter dimension self.
     * This will ensure that the facet call don't restricts itself in selection
     * other filter values for this dimension.
     *
     * @param query the filter query that will used to find the right set of facet that matches to the filter set.
     * @return the facets based on the given query sorted in the way defined by the FacetParams.FacetSort property
     */
    List<Host> getHosts(MetricQuery query);


    //==================================================================================================================
    // facet methods for cloud native / logical time series filter dimensions
    //==================================================================================================================

    /**
     * Fetches and returns the facet data for the CloudPlatform namespace / OpenShift project.
     * The service method will respect the specified query filters and proceed the query
     * with all filter except filter restrictions of the namespace filter dimension self.
     * This will ensure that the facet call don't restricts itself in selection
     * other filter values for this dimension.
     *
     * @param query the filter query that will used to find the right set of facet that matches to the filter set.
     * @return the facets based on the given query sorted in the way defined by the FacetParams.FacetSort property
     */
    List<Namespace> getNamespaces(MetricQuery query);

    /**
     * Fetches and returns the facet data for the CloudPlatform services.
     * The implementation will respect the specified query filters and proceed the query
     * with all filter except filter restrictions of the service filter dimension self.
     * This will ensure that the facet call don't restricts itself in selection
     * other filter values for this dimension.
     *
     * @param query the filter query that will used to find the right set of facet that matches to the filter set.
     * @return the facets based on the given query sorted in the way defined by the FacetParams.FacetSort property
     */
    List<Service> getServices(MetricQuery query);

    /**
     * Fetches and returns the facet data for pods (deployment units of Kubernetes/OpenShift).
     * The implementation will respect the specified query filters and proceed the query
     * with all filter except filter restrictions of the pod filter dimension self.
     * This will ensure that the facet call don't restricts itself in selection
     * other filter values for this dimension.
     *
     * @param query the filter query that will used to find the right set of facet that matches to the filter set.
     * @return the facets based on the given query sorted in the way defined by the FacetParams.FacetSort property
     */
    List<Pod> getPods(MetricQuery query);

    /**
     * Fetches and returns the facet data for the container that are parts of pods.
     * The implementation will respect the specified query filters and proceed the query
     * with all filter except filter restrictions of the container filter dimension self.
     * This will ensure that the facet call don't restricts itself in selection
     * other filter values for this dimension.
     *
     * @param query the filter query that will used to find the right set of facet that matches to the filter set.
     * @return the facets based on the given query sorted in the way defined by the FacetParams.FacetSort property
     */
    List<Container> getContainers(MetricQuery query);


    //==================================================================================================================
    //
    //==================================================================================================================

    long getAmountMeasuredPointsInProject(String projectName);

    /**
     * Returns a list of processes.
     *
     * @param query
     * @return a List of processes.
     */
    List<Process> getProcesses(MetricQuery query);

    /**
     * Return all available series series.
     * @param query
     * @return a list of measurements. The list can be empty but not null.
     */
    List<Measurement> getMeasurements(MetricQuery query);

    /**
     *
     *
     * @param query
     * @return
     */
    List<MetricGroup> getMetricGroups(MetricQuery query);



    List<Metric> getMetricsNames(MetricQuery metricQuery);

    /**
     *
     */
    void commitOrRollback();
}
