package de.qaware.ekg.awb.metricanalyzer.bl.tsquery;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.*;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Container;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Pod;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Service;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.Host;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.HostGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.TimeSeriesQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.TimeSeriesQueryResponse;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.dataobject.delete.DeleteParams;
import de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory;
import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.dataobject.facet.Facet;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetEntry;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchParams;
import de.qaware.ekg.awb.repository.api.dataobject.search.SearchResult;
import de.qaware.ekg.awb.repository.api.dataobject.search.SortField;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.bl.BinaryTimeSeries;
import de.qaware.ekg.awb.sdk.awbapi.repository.ImporterSourceRepository;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.core.DateValuePairMapper;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import de.qaware.ekg.awb.sdk.importer.api.RemoteSeriesDataFetcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.qaware.ekg.awb.repository.api.dataobject.expr.ExprFactory.*;
import static de.qaware.ekg.awb.repository.api.schema.EkgSchemaField.*;

/**
 * MetricDataAccessService default implementation that will provide functionality
 * to query and post compute time series and according facets for each filter dimension.
 */
@SuppressWarnings("unused") // used via CDI classpath scan
public class MetricDataAccessServiceImpl implements MetricDataAccessService {

    private static final Logger LOGGER = EkgLogger.get();

    private SeriesDataFetcherRegistry fetcherRegistry = EkgLookup.lookup(SeriesDataFetcherRegistry.class);

    private RepositoryClient repositoryClient;

    private static Map<ImporterSourceRepository, RemoteSeriesDataFetcher> FETCHER_CACHE = new ConcurrentHashMap<>();

    //=================================================================================================================
    //  various constructors
    //=================================================================================================================

    /**
     * Default constructor that is need than instantiating
     * this class via CDI default mechanism.
     */
    public MetricDataAccessServiceImpl() {
        // no op
    }

    /**
     * Parameterized constructor of this services that will use the given
     * RepositoryClient for it's work.
     *
     * @param client the repository client to use
     */
    public MetricDataAccessServiceImpl(RepositoryClient client) {
        this.repositoryClient = client;
    }

    //=================================================================================================================
    // API of the MetricDataAccessService interface and MetricDataAccessServiceImpl class itself
    //=================================================================================================================

    @Override
    public void deleteTimeSeriesByProjectName(String projectName) {

        try {
            DeleteParams deleteQuery = new DeleteParams();
            deleteQuery.addFilter(DOC_TYPE, DocumentType.TIME_SERIES.toString());
            deleteQuery.addFilter(PROJECT_NAME, projectName);
            repositoryClient.delete(deleteQuery);
            repositoryClient.commit();
        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised while trying to delete time series data.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Fetches time series data from persistence storage that matches to the specified filters in
     * the given query and post filter all values in the series that are not inside an (optional)
     * specified time interval
     *
     * @param query a query instance that defines the filter parameters for time series search
     * @return a response with 0 to n time series data
     */
    @Override
    public TimeSeriesQueryResponse queryTimeSeriesData(TimeSeriesQuery query) throws RepositoryException {

        try {
            QueryFilterParams queryParams = query.getQueryParams();
            Project project = queryParams.getProject();

            // prepare time series query
            SearchParams searchParams = new SearchParams();
            List<Expression> filterExpressions = new ArrayList<>();
            populateFilter(filterExpressions, queryParams);

            searchParams.withFilterQueries(filterExpressions.toArray(new Expression[0]));
            searchParams.withSortFields(List.of(
                    new SortField(TS_START, SortField.SortMode.ASC),
                    new SortField(ID, SortField.SortMode.ASC)
            ));

            searchParams.withCursor(query.getCursorId());

            // fetch time series with filter query as export stream
            SearchResult<BinaryTimeSeries> searchResult = repositoryClient.search(BinaryTimeSeries.class, searchParams);
            long totalHits = searchResult.getQueryStatus().getNumberOfHits();

            if (totalHits > query.getMaxMetricLimit()) {
                return new TimeSeriesQueryResponse(query.getCursorId(), totalHits, true);
            }

            List<BinaryTimeSeries> timeSeriesList = searchResult.getRows();
            String nextCursor = searchResult.getQueryStatus().getCursor();

            if (query.getCursorId() != null && query.getCursorId().equals(nextCursor)) {
                nextCursor = null;
            }

            // populate TimeSeriesQueryResponse instance with time series data (in case of remote projects) fetch
            // payload from remote data sources using a RemoteSeriesDataFetcher
            TimeSeriesQueryResponse response = new TimeSeriesQueryResponse(nextCursor, totalHits, false);
            final RemoteSeriesDataFetcher fetcher = retrieveFetcher(queryParams, project);

            for (BinaryTimeSeries binaryTimeSeries : timeSeriesList) {
                // create new TimeSeries based on public API POJO's
                TimeSeries timeSeries = mapToTimeSeries(binaryTimeSeries);

                // fetch data from remote data source if required
                if (project.useSplitSource() && fetcher != null) {

                    Value[] data = fetcher.fetchSeriesData(timeSeries.getRemoteSeriesKey(),
                            queryParams.getStart(), queryParams.getEnd());

                    if (data == null) {
                        // this occurs in error cases for example if remote repository isn't available
                        response.addTimeSeries(timeSeries);
                        response.setRequestAborted(true);
                        return response;
                    }

                    Stream.of(data).forEach(timeSeries::addValue);

                } else {
                    // post filter data and addAndSum all remaining tuples Value instance to the TimeSeries
                    DateValuePairMapper.uncompressAndDecodePlainBytes(binaryTimeSeries.getData(), (timeStamp, value) -> {
                        if(insideInterval(timeStamp, queryParams.getStart(), queryParams.getEnd())) {
                            timeSeries.addValue(new Value(timeStamp, value));
                        }
                    });
                }

                response.addTimeSeries(timeSeries);
            }

            return response;

        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised while retrieving data from Repository.", ex);
            throw ex;
        }
    }

    @Override
    public long getAmountMeasuredPointsInProject(String projectName) {
        try {
            if (StringUtils.isNotBlank(projectName)) {
                return repositoryClient.sumFieldValue(TS_DATA_AMOUNT_VALUES,
                        List.of(ExprFactory.exactFilter(PROJECT_NAME, projectName)));
            }

            repositoryClient.sumFieldValue(TS_DATA_AMOUNT_VALUES, new ArrayList<>());

            return 0;
        } catch (RepositoryException ex) {
            LOGGER.error("Exception raised while trying to delete time series data.", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public List<Process> getProcesses(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withProcess("*").build();
        return queryFacets(queryClone, TS_PROCESS_NAME,
                new Process("*", "all"), facetEntry -> new Process(facetEntry.getName(), facetEntry.getName()));
    }

    @Override
    public List<HostGroup> getHostGroups(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withHostGroup("*").build();
        return queryFacets(queryClone,
                TS_HOST_GROUP_NAME, new HostGroup("*"), facetEntry -> new HostGroup(facetEntry.getName()));

    }

    @Override
    public List<Host> getHosts(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withHost("*").build();
        return queryFacets(queryClone, TS_HOST_NAME,
                new Host("*"), facetEntry -> new Host(facetEntry.getName()));
    }

    @Override
    public List<Namespace> getNamespaces(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withNamespace("*").build();
        return queryFacets(query.getQueryParams(), TS_NAMESPACE_NAME,
                new Namespace("*"), facetEntry -> new Namespace(facetEntry.getName()));

    }

    @Override
    public List<Service> getServices(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withService("*").build();
        return queryFacets(query.getQueryParams(), TS_SERVICE_NAME,
                new Service("*"), facetEntry -> new Service(facetEntry.getName()));
    }

    @Override
    public List<Pod> getPods(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withPod("*").build();
        return queryFacets(queryClone, TS_POD_NAME,
                new Pod("*"), c -> new Pod(c.getName()));
    }

    @Override
    public List<Container> getContainers(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withContainer("*").build();
        return queryFacets(queryClone, TS_CONTAINER_NAME,
                new Container("*"), facetEntry -> new Container(facetEntry.getName()));
    }

    @Override
    public List<Measurement> getMeasurements(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withMeasurement("*").build();
        return queryFacets(queryClone, TS_MEASUREMENT,
                new Measurement("*"), facetEntry -> new Measurement(facetEntry.getName()));
    }

    @Override
    public List<MetricGroup> getMetricGroups(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withMetricGroup("*").build();
        return queryFacets(queryClone, TS_METRIC_GROUP,
                new MetricGroup("*"), facetEntry -> new MetricGroup(facetEntry.getName()));
    }

    @Override
    public List<Metric> getMetricsNames(MetricQuery query) {
        QueryFilterParams queryClone = new QueryFilterParams.Builder(query.getQueryParams()).withMetric("*").build();
        return queryFacets(queryClone, TS_METRIC_NAME,
                new Metric("*"), facetEntry -> new Metric(facetEntry.getName()));
    }

    @Override
    public void addEntities(Stream<TimeSeries> timeSeriesStream, boolean addEntities) throws RepositoryException {

        if (timeSeriesStream == null) {
            return;
        }

        repositoryClient.add(timeSeriesStream.map(timeSeries -> {

            BinaryTimeSeries binaryTimeSeries = new BinaryTimeSeries();

            // physical metric filter attributes
            binaryTimeSeries.setHost(timeSeries.getHost());
            binaryTimeSeries.setHostGroup(timeSeries.getHostGroup());

            // logical metric filter attributes
            binaryTimeSeries.setNamespace(timeSeries.getNamespace());
            binaryTimeSeries.setService(timeSeries.getService());
            binaryTimeSeries.setPod(timeSeries.getPod());
            binaryTimeSeries.setContainer(timeSeries.getContainer());

            // common metric filter attributes
            binaryTimeSeries.setMeasurement(timeSeries.getMeasurement());
            binaryTimeSeries.setProcess(timeSeries.getProcess());
            binaryTimeSeries.setMetric(timeSeries.getMetricName());
            binaryTimeSeries.setMetricGroup(timeSeries.getMetricGroup());
            binaryTimeSeries.setStart(timeSeries.getStartDate());
            binaryTimeSeries.setEnd(timeSeries.getEndDate());
            binaryTimeSeries.setProjectName(timeSeries.getProject());

            // other
            binaryTimeSeries.setRemoteSeriesKey(timeSeries.getRemoteSeriesKey());
            binaryTimeSeries.setData(DateValuePairMapper.compressAndEncodeDataPlainBytes(timeSeries.getValues()));

            return binaryTimeSeries;

        }).iterator());
    }

    @Override
    public void commitOrRollback() {
        try {
            repositoryClient.commit();
        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Initialize this services with a repository client that should used.
     * This is necessary than the service is instantiated via CDI and doesn't has an initial client.
     *
     * @param client the repository client to use
     */
    public void initializeService(RepositoryClient client) {
        this.repositoryClient = client;
    }

    /* ======================================== private helper ============================================= */

    private <T> List<T> queryFacets(QueryFilterParams queryParams, EkgSchemaField facetField,
                                    T wildCardEntry, Function<FacetEntry, T> converter) {

        try {
            FacetParams facetParams = new FacetParams()
                    .withLimit(2500)
                    .withMinCount(1)
                    .withSort(queryParams.getFacetSort())
                    .withFacetFields(facetField);

            List<Expression> andExpressions = new ArrayList<>();
            populateFilter(andExpressions, queryParams);
            facetParams.withFilterQueries(and(andExpressions.toArray(new Expression[0])));

            List<Facet> facets = repositoryClient.facet(facetParams).getFacets();

            List<T> resultList = new ArrayList<>(facets.size() + 1);
            if (facets.size() > 0) {
                resultList.add(0, wildCardEntry);
            }

            for (Facet facet : facets) {
                for (FacetEntry facetEntry : facet.getEntries()) {
                    resultList.add(converter.apply(facetEntry));
                }
            }

            return resultList;


        } catch (RepositoryException ex) {
            LOGGER.error("Error occurred during querying the facets from types.", ex);
            return Collections.emptyList();
        }
    }

    private TimeSeries mapToTimeSeries(BinaryTimeSeries timeSeriesEntity) {
        TimeSeries timeSeries = new TimeSeries(
                timeSeriesEntity.getProjectName(),

                timeSeriesEntity.getHostGroup(),
                timeSeriesEntity.getHost(),

                timeSeriesEntity.getNamespace(),
                timeSeriesEntity.getService(),
                timeSeriesEntity.getPod(),
                timeSeriesEntity.getContainer(),

                timeSeriesEntity.getMeasurement(),

                timeSeriesEntity.getProcess(),
                timeSeriesEntity.getMetricGroup(),
                timeSeriesEntity.getMetricName());

        timeSeries.setRemoteSeriesKey(timeSeriesEntity.getRemoteSeriesKey());

        return timeSeries;
    }

    private void populateFilter(List<Expression> andExpressions, QueryFilterParams queryParams) {

        // static parts that must always match
        andExpressions.add(exactFilter(DOC_TYPE, DocumentType.TIME_SERIES.toString()));
        andExpressions.add(exactFilter(PROJECT_NAME, queryParams.getProjectName()));

        // the facet filter for the different dimensions
        addWildcardExpressionIfNotNull(andExpressions, TS_HOST_GROUP_NAME, queryParams.getHostGroup());
        addWildcardExpressionIfNotNull(andExpressions, TS_HOST_NAME, queryParams.getHost());
        addWildcardExpressionIfNotNull(andExpressions, TS_NAMESPACE_NAME, queryParams.getNamespace());
        addWildcardExpressionIfNotNull(andExpressions, TS_SERVICE_NAME, queryParams.getService());
        addWildcardExpressionIfNotNull(andExpressions, TS_POD_NAME, queryParams.getPod());
        addWildcardExpressionIfNotNull(andExpressions, TS_CONTAINER_NAME, queryParams.getContainer());
        addWildcardExpressionIfNotNull(andExpressions, TS_PROCESS_NAME, queryParams.getProcess());
        addWildcardExpressionIfNotNull(andExpressions, TS_MEASUREMENT, queryParams.getMeasurement());
        addWildcardExpressionIfNotNull(andExpressions, TS_METRIC_GROUP, queryParams.getMetricGroup());

        if (StringUtils.isNotBlank(queryParams.getExcludeMetricName())) {
            andExpressions.add(ExprFactory.not(wildcardFilter(TS_METRIC_NAME, queryParams.getExcludeMetricName())));
        }

        // special treatment for the metric name that supports multi selection
        List<String> metricNames = queryParams.getMetricNames();
        if (StringUtils.isNotBlank(queryParams.getMetricName())) {
            addWildcardExpressionIfNotNull(andExpressions, TS_METRIC_NAME, queryParams.getMetric());

        } else if (metricNames.size() > 1) {
            List<Expression> orExpressions = new ArrayList<>(metricNames.size());
            metricNames.forEach(name -> orExpressions.add(wildcardFilter(TS_METRIC_NAME, name)));
            andExpressions.add(or(orExpressions.toArray(new Expression[0])));

        } else if (metricNames.size() == 1) {
            andExpressions.add(wildcardFilter(TS_METRIC_NAME, metricNames.get(0)));
        }

        // last but not least the filter for the requested time interval
        if (queryParams.getStart() > 0) {
            andExpressions.add(dateTimeRangeFilter(TS_STOP, Instant.ofEpochMilli(queryParams.getStart()).plus(1, ChronoUnit.MINUTES), null));
        }

        if (queryParams.getEnd() > 0) {
            andExpressions.add(dateTimeRangeFilter(TS_START, null, Instant.ofEpochMilli(queryParams.getEnd()).minus(1, ChronoUnit.MINUTES)));
        }
    }

    private void addWildcardExpressionIfNotNull(List<Expression> expressions, EkgSchemaField field, NamedValueEntity entity) {

        if (entity != null && StringUtils.isNotBlank(entity.getValueName()) && !"*".equals(entity.getValueName())) {
            String exp = entity.getValueName();
            if (exp.startsWith("-(") && exp.endsWith(")")) {
                expressions.add(not(wildcardFilter(field, exp.substring(2, exp.length() - 1))));

            } else if(exp.contains("|")) {
                String[] filterTokens = exp.replaceAll("[()]", "").split("\\|");

                if (filterTokens.length > 1) {
                    List<Expression> orFilterExpressions = new ArrayList<>();

                    for (String token : filterTokens) {
                        orFilterExpressions.add(wildcardFilter(field, token));
                    }

                    expressions.add(or(orFilterExpressions.toArray(new Expression[0])));

                } else if (filterTokens.length == 1) {
                    expressions.add(wildcardFilter(field, filterTokens[0]));

                }

            } else {
                expressions.add(wildcardFilter(field, entity.getValueName()));
            }
        }
    }

    private RemoteSeriesDataFetcher retrieveFetcher(QueryFilterParams queryParams, Project project) {

        RemoteSeriesDataFetcher fetcher;
        if (project.useSplitSource()) {

            fetcher = FETCHER_CACHE.get(queryParams.getImporterSourceRepository());

            if (fetcher != null) {
                return fetcher;
            }

            fetcher = fetcherRegistry.retrieveSeriesDataFetcher(project.getImporterId());
            if (fetcher == null) {
                throw new IllegalStateException("Unable to retrieve fetcher for importerId '"
                        + project.getImporterId() + "'");
            }

            fetcher.setSourceRepositoryDefinition(queryParams.getImporterSourceRepository());

            FETCHER_CACHE.put(queryParams.getImporterSourceRepository(), fetcher);

        } else {
            fetcher = null;
        }
        return fetcher;
    }

    /**
     * Checks if a timestamp is inside a given interval.
     *
     * @param timestamp the timestamp.
     * @param begin     the begin - if null only the end will be checked
     * @param end       @return
     */
    private static boolean insideInterval(long timestamp, long begin, long end) {

        if (begin < 0)  {
            if (end > 0) {
                return timestamp < end;
            }
        } else {
            if (end > 0) {
                return begin < timestamp && timestamp < end;
            } else {
                return begin < timestamp;
            }
        }

        return true;
    }
}
