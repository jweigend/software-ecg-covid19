package de.qaware.ekg.awb.metricanalyzer.bl.visualisation;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingType;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryComputeParams;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.TimeSeriesQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.TimeSeriesQueryResponse;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine.CombinerFactory;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine.TimeSeriesCombiner;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.SimplificationService;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.ValueChangeSimplificationService;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing.SmootherFactory;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing.TimeSeriesSmoother;
import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.text.NumberFormat;
import java.util.*;

import static de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode.CONCAT;

/**
 * The default implementation of MetricsQueryService that will fetch the time series
 * data from types and do post processing on it like smoothing and vectorization.
 */
@SuppressWarnings("unused") // used via CDI / reflection
public class MetricQueryServiceImpl implements MetricQueryService {

    /**
     * Logger to protocol errors and special events
     */
    private static final Logger LOGGER = EkgLogger.get();

    /**
     * The MetricQueryService that provides the metric time-series low-level access to types data
     */
    private MetricDataAccessService metricDataAccess;

    @Inject
    protected EkgEventBus eventBus;

    /**
     * An SimplificationService used for vectorisation of the retrieved time series data
     */
    @Inject
    private SimplificationService simplificationService;

    /**
     * The SmootherFactory that will provide different TimeSeriesSmoother implementations that matches to the parameters
     */
    @Inject
    private SmootherFactory smootherFactory;


    @Override
    public void initializeService(RepositoryClient client) {
        this.metricDataAccess = ServiceDiscovery.lookup(MetricDataAccessService.class, client);
    }


    //================================================================================================================
    //  MetricsQueryService API
    //================================================================================================================

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.metricanalyzer.bl.api.MetricsQueryService#getComputedTimeSeries(...)
     */
    public ComputedTimeSeriesResponse getComputedTimeSeries(QueryFilterParams filterParams,
                                                            QueryComputeParams computeParams, int maxMetricLimit) {

        StopWatch stopWatch = StopWatch.createStarted();

        // query we use to fetch the data
        int transitiveSeriesLimit = filterParams.getProject().useSplitSource() ? maxMetricLimit : Integer.MAX_VALUE;
        TimeSeriesQuery query = new TimeSeriesQuery(filterParams, transitiveSeriesLimit);

        boolean seriesMergeActive = computeParams.getSeriesCombineMode() != SeriesCombineMode.NONE;
        boolean isConcatMode = computeParams.getSeriesCombineMode() == SeriesCombineMode.CONCAT;


        // the result list that will returned at the end
        List<TimeSeries> result = new ArrayList<>();
        Map<String, TimeSeries> groupingMap = new HashMap<>();

        long totalResults;
        long totalPoints = 0;
        long totalPointsInProject;

        try {
            String cursorId = TimeSeriesQuery.INITIAL_CURSOR_ID;
            while (true) {

                // we use cursor to fetch data as fast as possible
                query.setCursorId(cursorId);

                // query and retrieve data
                TimeSeriesQueryResponse response = metricDataAccess.queryTimeSeriesData(query);

                totalResults = response.getTotalHits();

                if (response.isRequestAborted()) {
                    return new ComputedTimeSeriesResponse(response.getTotalHits(), maxMetricLimit);
                }

                // all chunks of the streamed data consumed?
                if (response.isConsumed() && response.getData().isEmpty()) {
                    break;
                }

                // token to address the next chunk
                cursorId = response.getCursorId();

                Collection<TimeSeries> rowsChunk = response.getData();

                for (TimeSeries fetchedSeries : rowsChunk) {

                    if (Thread.currentThread().isInterrupted()) {
                        return new ComputedTimeSeriesResponse(true, maxMetricLimit); // normal termination, return empty result
                    }

                    // use unique names as metric key
                    String groupingKey = fetchedSeries.getGroupingKey(computeParams.getSeriesCombineMode() == CONCAT);
                    TimeSeries mainSeries = groupingMap.get(groupingKey);

                    if (mainSeries == null) {
                        mainSeries = fetchedSeries;
                        totalPoints += mainSeries.getValues().size();

                        if (isConcatMode) {
                            mainSeries.setMeasurement("*");
                        }

                        groupingMap.put(groupingKey, mainSeries);
                        result.add(mainSeries);
                    } else {
                        totalPoints += fetchedSeries.getValues().size();
                        mainSeries.addAll(fetchedSeries.getValues());
                    }
                }

                if (response.isConsumed()) {
                    break;
                }
            }

            stopWatch.suspend();
            totalPointsInProject = metricDataAccess.getAmountMeasuredPointsInProject(filterParams.getProjectName());


        } catch (RepositoryException e) {
            LOGGER.error("Exception raised while getting the counters from types.", e);
            return new ComputedTimeSeriesResponse(maxMetricLimit, e);
        }

        // break if to much series because we can't show it in a useful way and it cost a lot of performance
        if (!seriesMergeActive && result.size() > maxMetricLimit ||(
                isConcatMode && groupingMap.size() > maxMetricLimit)) {
            return new ComputedTimeSeriesResponse(result.size(), maxMetricLimit);
        }

        //-----------------------------------------------------------------------------------------------------------
        // at this point the data must be fully fetched from repository because the further steps need all data
        // for analyze and compute the time series correctly
        //-----------------------------------------------------------------------------------------------------------

        // this threshold means that only value changes in the series should proceed.
        // this have to be applied before the series will merged by the combiner
        if (computeParams.getThreshold() == 0) {
            result = new ValueChangeSimplificationService().simplify(result);
        }

        // add values of all time series to a single one if requested
        result = combineTimeSeriesData(query.getQueryParams().getMetricName(), result,
                computeParams.getSeriesCombineMode());

        // flatten the value vector to make it nicer to view at UI if requested
        smoothingTimeSeriesData(result, computeParams.getSeriesSmoothingGranularity(),
                computeParams.getSeriesSmoothingType());

        // reduce the data points time series using some kind of sampling or vectorization algorithm
        result = simplifyTimeSeriesData(result, computeParams.getThreshold());

        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMAN);

        eventBus.publish((new ProgressEvent("Processed " + formatter.format(totalResults) + " series with "
                + formatter.format(totalPoints) + " values of "+ formatter.format(totalPointsInProject)
                + " total in " + stopWatch.getTime()  + "ms.", 1.0, this)));

        return new ComputedTimeSeriesResponse(result, maxMetricLimit);
    }

    //================================================================================================================
    //  private helper to delegate post processing of time series
    //================================================================================================================

    /**
     * Reduce the amount of data points in the given time series to a maximum defined
     * by the seriesValueLimit parameter.
     * The seriesValueLimit will threaded as overall amount limit. If the time series together have
     * less than the limit the given series will keep untouched.
     *
     * @param timeSeriesList the ingoing list of time series that should visualisation.
     * @param seriesValueLimit the maximum amount of data points over all time series in the list.
     * @return a modified list of time series with reduced amount of data point if necessary.
     */
    private List<TimeSeries> simplifyTimeSeriesData(List<TimeSeries> timeSeriesList, int seriesValueLimit) {
        StopWatch stopwatch = StopWatch.createStarted();
        List<TimeSeries> simplified = simplificationService.simplify(timeSeriesList, seriesValueLimit);
        LOGGER.info("Simplify {} time series in {}", timeSeriesList.size(), stopwatch);

        return simplified;
    }

    /**
     * If requested this method will delegate each time series in the given list
     * to a processor that will visualisation a smoothing of the value graph based on the
     * specified smoothing typ and granularity. This will result in a flatten graph of
     * the series in the chart.
     *
     * The implementation will modify the series instances itself, so it have to be mutable.
     *
     * @param timeSeriesList a list of time series it's data will be modified (smoothed)
     * @param smoothingGranularity the sampling rate used to visualisation the smoothing
     * @param smoothingType the type of smoothing the call want's for the time series data
     */
    private void smoothingTimeSeriesData(final List<TimeSeries> timeSeriesList,
                                                     final SeriesSmoothingGranularity smoothingGranularity,
                                                     final SeriesSmoothingType smoothingType) {
        // early exit if nothing to do
        if (smoothingType == SeriesSmoothingType.NONE || timeSeriesList.isEmpty()) {
            return;
        }

        TimeSeriesSmoother seriesSmoother = smootherFactory.resolveSmoother(smoothingType, smoothingGranularity);

        // visualisation the smoothing of each time series multi-threaded
        timeSeriesList.parallelStream().forEach(timeSeries ->
                timeSeries.setSortedValues(seriesSmoother.computeSmoothing(timeSeries.getValues())));
    }

    /**
     * Combine multiple time series to a single one. Different values in same time increments
     * (overlapping of time series) will reduce by the chosen mode.
     *
     * None overlapping time series wan't change in any way
     *
     * @param newMetricName the metric name that have to use for the returned time series
     * @param timeSeriesList a list of time series that should combined if it overlap
     * @param combineMode the algorithm used to addValues overlapping parts of the time series.
     * @return a SingletonList with a time series that contains the whole time range of each given one
     */
    private List<TimeSeries> combineTimeSeriesData(String newMetricName, List<TimeSeries> timeSeriesList,
                                                   SeriesCombineMode combineMode) {

        if (combineMode == SeriesCombineMode.NONE || combineMode == CONCAT || timeSeriesList.isEmpty()) {
            return timeSeriesList;
        }

        StopWatch stopWatch = StopWatch.createStarted();
        LOGGER.info("Begin combining time series list with {} elements.", timeSeriesList.size());
        TimeSeriesCombiner combiner = CombinerFactory.resolveCombiner(combineMode);
        List<TimeSeries> result = Collections.singletonList(combiner.combine(newMetricName, timeSeriesList));
        LOGGER.info("Finished addValues {} time series to a single one in {}.", timeSeriesList.size(), stopWatch);

        return result;
    }
}
