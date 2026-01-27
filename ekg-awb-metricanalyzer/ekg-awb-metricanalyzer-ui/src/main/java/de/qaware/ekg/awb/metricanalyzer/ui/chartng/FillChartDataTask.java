package de.qaware.ekg.awb.metricanalyzer.ui.chartng;

import de.qaware.ekg.awb.common.ui.chartng.ZoomableStackedChart;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryComputeParams;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.ComputedTimeSeriesResponse;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.MetricQueryService;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.core.events.AwbErrorEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import org.slf4j.Logger;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Task that will fetch computed time series data from types,
 * convert and it to JavaFX series data and write it directly to the chart data model
 */
public class FillChartDataTask extends Task<List<XYChart.Series<Long, Double>>> {

    /**
     * Logger to protocol errors and special events
     */
    private static final Logger LOGGER = EkgLogger.get();

    /**
     * One day in milliseconds (factors: 1sec, 1min, 1 hour, 1 day)
     */
    private static final long ONE_DAY_IN_MS = 1000 * 60 * 60 * 24;

    /**
     * The maximum amount of time series returned to the caller of fetching data from the EKG repository.
     * If exceeded the fetching and compute logic stops and return an empty array (and notifies about it).
     */
    private static final int MAX_ALLOWED_SERIES_EKG_REPO = 250;

    /**
     * The maximum amount of time series returned to the caller in case of split-source projects that
     * fetches the data from an remote repository. If exceeded the fetching and compute logic stops
     * and return an empty array (and notifies about it).
     */
    private static final int MAX_ALLOWED_SERIES_REMOTE_REPO = 50;

    /**
     * the query parameter that will use to fetch the time series
     */
    private QueryFilterParams filterParams;

    /**
     * the compute parameters that define various setting for the post computing
     * steps on the fetched time series data like sampling or aggregation settings
     */
    private QueryComputeParams computeParams;

    /**
     * boolean flag that controls if the chart axis should reset than new data will loaded or not.
     * To to reset axis will need for use cases like zooming that will request new data but wan't
     * display it with auto ranging axis.
     */
    private boolean resetChartAxis;

    /**
     * the EKG repository that stores the data to fetch
     */
    private EkgRepository repository;

    /**
     * the target of all fetched and computed data
     */
    private ZoomableStackedChart zoomableStackedChart;

    /**
     * Constructs a new instance of FillChartDataTask which will use
     * the given setup parameters for fetching and postprocessing the data.
     *
     * @param filterParams the query parameter that define which metrics in which time range should be fetched
     * @param computeParams the compute parameters that define various setting for the post computing steps on the
     *                    fetched time series data like sampling or aggregation settings
     * @param resetChartAxis do reset chart axis on load or not
     * @param repository the concrete EKG repository that stores the data to fetch
     * @param zoomableStackedChart the target chart of the data
     */
    public FillChartDataTask(QueryFilterParams filterParams, QueryComputeParams computeParams, boolean resetChartAxis,
                             EkgRepository repository, ZoomableStackedChart zoomableStackedChart) {

        this.filterParams = filterParams;
        this.computeParams = computeParams;
        this.resetChartAxis = resetChartAxis;
        this.repository = repository;
        this.zoomableStackedChart = zoomableStackedChart;
    }

    @Override
    protected List<XYChart.Series<Long, Double>> call() {

        long startTime = System.currentTimeMillis();
        LOGGER.info("Start with chart data fetching & post-processing");

        try {
            // the query service that belongs to the types we use
            MetricQueryService service = repository.getBoundedService(MetricQueryService.class);

            // list as collector for the results
            List<XYChart.Series<Long, Double>> result = new ArrayList<>();

            // be aware the filter also is used then the user span a zoom rectangle
            // see getZoomStart(), we need to add on day to the end, otherwise the user didn't get the expected range
            if (filterParams.getEnd() > 0) {
                filterParams.setEnd(filterParams.getEnd() + ONE_DAY_IN_MS);
            }

            boolean isSplitSourceProject = filterParams.getProject().useSplitSource();
            int metricLimit;

            if (isSplitSourceProject) {
                metricLimit = MAX_ALLOWED_SERIES_REMOTE_REPO;
            } else {
                metricLimit = MAX_ALLOWED_SERIES_EKG_REPO;
            }

            // query counters from database and retrieve a parallel stream as result
            ComputedTimeSeriesResponse response = service.getComputedTimeSeries(filterParams, computeParams, metricLimit);

            if (response.isRequestAborted()) {

                if (response.isMaxSeriesLimitExceeded()) {
                    LOGGER.info("Retrieved a max series limit exceeded error after {}ms.",
                            (System.currentTimeMillis() - startTime));
                    showMaxSeriesExceededDialog(response.getTotalHits(), metricLimit, isSplitSourceProject);

                }else if (response.hasErrorPayload()) {
                    LOGGER.info("The computation of the request has aborted as result of an error. Exception message: {} ",
                            response.getOccurredError().getMessage());
                } else {
                    LOGGER.info("The computation of the request has aborted as result of an interruption. Compute time: {}ms.",
                            (System.currentTimeMillis() - startTime));
                }

                return new ArrayList<>();
            }

            LOGGER.info("Retrieved time series data after {}ms", (System.currentTimeMillis() - startTime));

            zoomableStackedChart.clearBase(resetChartAxis);
            zoomableStackedChart.setBaseYAxisLabel(filterParams.getFullQualifiedDisplayName());

            if (response.getTimeSeries().size() > MAX_ALLOWED_SERIES_EKG_REPO) {
                LOGGER.warn("The result has more than '" + MAX_ALLOWED_SERIES_EKG_REPO + "' series. Return empty array to protect the UI.");
                return new ArrayList<>();
            }

            response.getTimeSeries().forEach(series -> {

                super.updateMessage("Processing: " + series.getMetricName());

                // we do not sort metric values because this should happen at the import process!!
                ObservableList<XYChart.Data<Long, Double>> chartSeriesData = FXCollections.observableArrayList();

                // convert values returned time series to XYChart.Data points
                for (Value value : series.getValues()) {
                    chartSeriesData.add(new XYChart.Data<>(value.getTimestamp(), value.getValue()));
                }

                // create new JavaFX series and addAndSum to list that will returned
                XYChart.Series<Long, Double> chartSeries = new XYChart.Series<>(chartSeriesData);
                chartSeries.nameProperty().setValue(series.getDisplayName());

                zoomableStackedChart.addSeriesToBase(chartSeries);
                result.add(chartSeries);
            });

            super.updateMessage("Successfully loaded " + result.size() + " series.");
            LOGGER.info("Calculate chart data in " + (System.currentTimeMillis() - startTime) + "ms");

            return result;

        } catch (Exception e) {

            if (e instanceof IllegalStateException && e.getCause() instanceof InterruptedException) {
                return new ArrayList<>();
            }

            LOGGER.error("Exception occurred during fetch of time series data for the AWB chart.", e);
            EkgEventBus bus = EkgLookup.lookup(EkgEventBus.class);
            bus.publish(new AwbErrorEvent(this, e));
            throw e;
        }
    }

    private void showMaxSeriesExceededDialog(long totalHitTimeSeries, int seriesLimit, boolean isSplitSourceProject) {
        Platform.runLater(() -> {

            String errorDetailSplitSource = "\nWhen using split source projects the maximum number\n" +
                    "of dedicated metrics is limited to ";

            String errorDetailStandard = "This should be too much even for you!\n\n The limit is ";

            String errorDetail = isSplitSourceProject ? errorDetailSplitSource : errorDetailStandard;

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("When megalomania takes over!");
            alert.setHeaderText("Maximum number of allowed result data sets exceeded!");
            alert.setContentText("\nRequest with the chosen filter returned "
                    + NumberFormat.getInstance(Locale.GERMAN).format(totalHitTimeSeries)
                    + " different time series. " + errorDetail +
                    + seriesLimit + ". \nFurther restrict the filters or choose 'Combine metrics' " +
                    "to visualize data.");

            alert.setHeight(300);

            alert.showAndWait();
        });
    }

    @Override
    protected void succeeded() {
        super.succeeded();
    }

    @Override
    protected void failed() {
        super.failed();
    }
}
