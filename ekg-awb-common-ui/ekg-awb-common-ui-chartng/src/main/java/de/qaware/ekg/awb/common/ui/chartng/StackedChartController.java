package de.qaware.ekg.awb.common.ui.chartng;


import de.qaware.ekg.awb.common.ui.chartng.axis.SpawningAxis;
import de.qaware.ekg.awb.common.ui.chartng.compute.TimeSeriesSliceAnalyzer;
import de.qaware.ekg.awb.common.ui.chartng.legend.StackedChartLegend;
import de.qaware.ekg.awb.common.ui.chartng.zoom.Zoomable;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.*;

import static de.qaware.ekg.awb.common.ui.chartng.compute.TimeSeriesSliceAnalyzer.SliceDataSet;
import static de.qaware.ekg.awb.common.ui.chartng.compute.TimeSeriesSliceAnalyzer.SliceValueContainer;


/**
 * Controller that manages a single ZoomableStackedChart instance of implements
 * a couple of high level operations by orchestrating the API methods of the stacked chart
 * and it's components (like axis).
 */
@SuppressWarnings("UnusedReturnValue")
public class StackedChartController {

    /**
     * The logger used by the controller to notify errors and debug information's
     */
    private static final Logger LOGGER = EkgLogger.get();

    private static final double ONE_HUNDRED_PERCENT = 100.0;

    private ZoomableStackedChart zoomableStackedChart;

    private StackedChartLegend chartLegend;

    private BooleanProperty yAxisAlignmentActive = new SimpleBooleanProperty(false);


    /**
     * Constructs a new instance of StackedChartController that need at least
     * a ZoomableStackedChart instance to work properly.
     *
     * @param zoomableStackedChart a instance of ZoomableStackedChart that will managed by the controller
     */
    public StackedChartController(ZoomableStackedChart zoomableStackedChart) {
        this.zoomableStackedChart = zoomableStackedChart;
    }

    //================================================================================================================
    //  the chart actions the controller will provide as public API
    //================================================================================================================

    /**
     * Active/deactivates the alignment of the y-axis of base chart and background charts.
     * If the alignment is active, all background charts will have the same absolute bounds and scaling
     * as the base chart. If deactivated the y-axis of the background charts can scale independent
     * from each other.
     *
     * @param doAlign a boolean flag that indicates if the y-alignment is active or not
     */
    public synchronized void alignYAxis(boolean doAlign) {

        if (doAlign == yAxisAlignmentActive.get()) {
            return;
        }

        SpawningAxis<Double> baseYAxis = zoomableStackedChart.getBaseChart().getYAxis();

        if (doAlign) {
            DoubleProperty baseYLowerBoundProperty = baseYAxis.lowerBoundProperty();
            DoubleProperty baseYUpperBoundProperty = baseYAxis.upperBoundProperty();

            for(ColoredChart bgChart : zoomableStackedChart.getBackgroundCharts()) {

                SpawningAxis<Double> backgroundYAxis = bgChart.getYAxis();
                backgroundYAxis.castToAxis().setAutoRanging(false);

                boolean isEnforced = false;
                if (backgroundYAxis.upperBoundProperty().get() > baseYUpperBoundProperty.get() &&
                        (Double.isNaN(baseYAxis.getEnforcedUpperBound())
                                || backgroundYAxis.upperBoundProperty().get() > baseYAxis.getEnforcedUpperBound())) {
                    baseYAxis.forceUpperBound(backgroundYAxis.upperBoundProperty().get());
                    isEnforced = true;
                }

                if (backgroundYAxis.lowerBoundProperty().get() > baseYLowerBoundProperty.get()&&
                        (Double.isNaN(baseYAxis.getEnforcedLowerBound())
                                || backgroundYAxis.lowerBoundProperty().get() > baseYAxis.getEnforcedLowerBound())) {
                    baseYAxis.forceLowerBound(backgroundYAxis.lowerBoundProperty().get());
                    isEnforced = true;
                }

                if (isEnforced) {
                    baseYAxis.layoutCustomBounds();
                }

                bgChart.getYAxis().lowerBoundProperty().bind(baseYLowerBoundProperty);
                bgChart.getYAxis().upperBoundProperty().bind(baseYUpperBoundProperty);

                bgChart.getYAxis().castToAxis().requestAxisLayout();
                bgChart.getYAxis().castToAxis().layout();
            }

        } else {
            for(ColoredChart bgChart : zoomableStackedChart.getBackgroundCharts()) {
                bgChart.getYAxis().lowerBoundProperty().unbind();
                bgChart.getYAxis().upperBoundProperty().unbind();
                bgChart.getYAxis().castToAxis().setAutoRanging(true);
                bgChart.getYAxis().castToAxis().layout();
            }

            baseYAxis.forceUpperBound(Double.NaN);
            baseYAxis.layoutCustomBounds();
        }



        yAxisAlignmentActive.set(doAlign);
    }

    /**
     * Change the chart with the specified id to the given type
     * (AreaChart, LineChart, StackChart, ...).
     * Changing the type will cause a recreation of the chart (clone and  new rendering)
     *
     * @param chartId the id of the chart the type should changed to the specified one
     * @param chartType the new type of the chart
     */
    public void changeChartType(String chartId, ChartType chartType) {

        ColoredChart chart = getChartById(chartId);

        // early exist if nothing to do
        if (chartType == chart.getChartType()) {
            return;
        }

        if (chart == zoomableStackedChart.getBaseChart()) {
            changeBaseChartType(chartType);
            chartLegend.setBaseChart(zoomableStackedChart.getBaseChart());
        } else {
            changeBackgroundChartType(chart, chartType);
        }
    }

    /**
     * Pushes the a clone of the base chart to the stack and replaces and clearBase the base chart
     * to make it ready for new series
     *
     * @param newBaseChartId an optional id for the new chart that will replace the pushed base chart
     * @return the new background chart (clone of the base)
     */
    public ColoredChart pushBaseToBackgroundChart(String newBaseChartId) {

        ColoredChart baseChart = zoomableStackedChart.getBaseChart();

        // set the current base chart as another background chart
        ColoredChart newBackgroundChart = ChartCloneUtil.clone(baseChart);

        // it is important we never use similar id's for different charts
        if (StringUtils.isNotBlank(newBaseChartId) || baseChart.getId().equals(newBaseChartId)) {
            newBackgroundChart.setId(newBaseChartId);
        } else {
            newBackgroundChart.setId(UUID.randomUUID().toString());
        }

        addBackgroundChart(baseChart, newBackgroundChart);
        baseChart.clear(true);
        baseChart.getYAxis().castToAxis().setLabel("-");


        // return the new background chart so other can customize or access it if necessary
        return newBackgroundChart;
    }

    /**
     * Adds a the given {@link ColoredChart} instance as new to the background chart to the chart stack.
     *
     * @param baseChart the primary based chart just used to retrieve axis properties used for data binding
     * @param newBackgroundChart the new background chart that will add to the the stack and bind to the base axis values
     *
     * @return the added background chart
     */
    public synchronized ColoredChart addBackgroundChart(ColoredChart baseChart, ColoredChart newBackgroundChart) {

        // we freeze the current range of the base chart so independent of new values
        // this will be the the minimum until all background charts are deleted
        SpawningAxis<Long> xAxisBase = baseChart.getXAxis();
        double minDateBound = resolveMinDateLowerBound(xAxisBase.lowerBoundProperty().get());
        double maxDateBound = resolveMaxDateUpperBound(xAxisBase.upperBoundProperty().get());

        xAxisBase.forceLowerBound(minDateBound);
        xAxisBase.forceUpperBound(maxDateBound);

        // now bind axis ranges of the new cloned chart to the background chart
        Axis yAxis = bindChartAxisToBase(baseChart, newBackgroundChart);

        // style y-axis
        yAxis.setSide(Side.RIGHT);
        if (StringUtils.isEmpty(yAxis.getLabel())) {
            yAxis.setLabel("Series " + (zoomableStackedChart.getBackgroundCharts().size() + 1));
        }

        newBackgroundChart.toXYChart().getStyleClass().add("backgroundChart");
        newBackgroundChart.setChartColor(Color.BLACK);
        newBackgroundChart.toXYChart().setVerticalZeroLineVisible(false);
        newBackgroundChart.toXYChart().setHorizontalZeroLineVisible(false);
        newBackgroundChart.toXYChart().setVerticalGridLinesVisible(false);
        newBackgroundChart.toXYChart().setHorizontalGridLinesVisible(false);

        yAxis.setPrefWidth(ZoomableStackedChart.AXIS_WIDTH);
        yAxis.setMaxWidth(ZoomableStackedChart.AXIS_WIDTH);


        // make line chart ready for panning
        Zoomable<Long, Double> zoom = new Zoomable<>(newBackgroundChart.toXYChart());
        zoom.setIsHorizontalPanningAllowed(false);

        zoomableStackedChart.getZoomableBgChartList().add(zoom);
        zoomableStackedChart.getBackgroundCharts().add(newBackgroundChart);

        return newBackgroundChart;
    }

    /**
     * Deletes one ColoredChart instance specified by it's id from
     * the background stack.
     * If no chart could found to the id an exception will thrown.
     *
     * @param backgroundChartId the id of the chart that should removed from background
     */
    public void deleteBackgroundChart(String backgroundChartId) {

        ColoredChart backgroundChart = getChartById(backgroundChartId);
        zoomableStackedChart.getBackgroundCharts().remove(backgroundChart);
        backgroundChart.clear(true);

        if (zoomableStackedChart.getBackgroundCharts().isEmpty()) {
            zoomableStackedChart.getBaseChart().getXAxis().castToAxis().setAutoRanging(true);
            zoomableStackedChart.getBaseChart().getXAxis().forceUpperBound(Double.NaN);
            zoomableStackedChart.getBaseChart().getXAxis().forceLowerBound(Double.NaN);
        }

    }

    /**
     * Removes all background chart that are currently on the stack.
     *
     * @param keepComputedCharts boolean flag that controls in computed should keep or not
     */
    public void deleteAllBackgroundCharts(boolean keepComputedCharts) {

        if (keepComputedCharts) {

            ObservableList<ColoredChart> backgroundCharts = zoomableStackedChart.getBackgroundCharts();
            FilteredList<ColoredChart> chartsToDelete = backgroundCharts.filtered(ColoredChart::isNotComputed);
            backgroundCharts.removeAll(chartsToDelete);
            chartsToDelete.forEach(chart -> chart.clear(true));

            return;
        }

        zoomableStackedChart.getBackgroundCharts().forEach(chart -> chart.clear(true));
        zoomableStackedChart.getBackgroundCharts().clear();
    }

    /**
     * Clear all background charts that the bindings of the axis values between
     * background and base chart.
     */
    public void clearAll() {
        zoomableStackedChart.getBaseChart().toXYChart().getData().clear();
        zoomableStackedChart.getBaseChart().getXAxis().forceUpperBound(Double.NaN);
        zoomableStackedChart.getBaseChart().getXAxis().forceLowerBound(Double.NaN);
        zoomableStackedChart.getBaseChart().getXAxis().layoutCustomBounds();
        changeChartType(zoomableStackedChart.getBaseChart().getId(), ChartType.LINE);

        zoomableStackedChart.getBaseChart().getYAxis().forceUpperBound(Double.NaN);
        zoomableStackedChart.getBaseChart().getYAxis().forceLowerBound(Double.NaN);
        zoomableStackedChart.getBaseChart().getYAxis().layoutCustomBounds();
        deleteAllBackgroundCharts(false);
    }

    /**
     * Resolves a chart by it's id and returns it.
     * Either the base chart and the background charts will included in the search.
     * If the chart couldn't found an runtime exception will thrown because this is
     * is an unexpected case.
     *
     * @param id the id of the chart to looking for
     * @return the chart that belongs to the id
     */
    public ColoredChart getChartById(String id) {
        if (zoomableStackedChart.getBaseChart().getId().equals(id)) {
            return zoomableStackedChart.getBaseChart();
        }

        for (ColoredChart bgChart : zoomableStackedChart.getBackgroundCharts()) {
            if (bgChart.getId().equals(id)) {
                return bgChart;
            }
        }

        throw new IllegalArgumentException("No chart with id '" + id + "' exists!");
    }

    public void absoluteCombineBackgroundChartsToBase() {
        ColoredChart baseChart = zoomableStackedChart.getBaseChart();

        List<ColoredChart> backgroundCharts = zoomableStackedChart.getBackgroundCharts().filtered(chart -> !chart.isComputed());
        if (backgroundCharts.isEmpty()) {
            LOGGER.error("No background charts exists that can combined to the base chart.");
            return;
        }

        backgroundCharts.forEach(coloredChart -> coloredChart.getAllSeries().forEach(baseChart::addSeries));
        baseChart.setComputed(true);
        deleteAllBackgroundCharts(true);

        pushBaseToBackgroundChart(UUID.randomUUID().toString());
    }

    /**
     * Uses the time series data of the first series of the base chart to define a 100% baseline.
     * All time series of any background charts will recalculated with it's values relative compared to base line.
     * The recalculated series will combined together in a single background chart. The base line in a dedicated
     * background chart. All other charts and data will deleted.
     *
     * To enable the relative comparison of values the base line will segmented into time slices and the series
     * of all background chart too. Only values that are in similar slices with a base line value will computed
     * the other ones skipped.
     *
     * The method provides a special treatment of values with are not located in time slices of the base line.
     * If the interpolate option = true, values of the same time slice with no base line value will combined and
     * the combined value is the new 100%. So the relative value to each other will calculated.
     *
     * @param interpolate a boolean flag that controls if interpolation of non matching value is active or not
     */
    public void proportionCombineBackgroundChartsToBase(boolean interpolate) {

        ColoredChart baseChart = zoomableStackedChart.getBaseChart();

        // early exist if no data (should prevent via deactivated UI buttons)
        if (baseChart.getAllSeries().isEmpty()) {
            LOGGER.error("The base chart date is empty. Proportional combine won't work!");
            return;
        }

        // early exist if multiple series exists and the 100% baseline wouldn't clearBase
        // (should also prevent via deactivated UI buttons)
        if (baseChart.getAllSeries().size() > 1) {
            LOGGER.error("The base chart has multiple series that will make the base-line ambiguous. " +
                    "Proportional combine won't work!");
            return;
        }

        List<ColoredChart> backgroundCharts = zoomableStackedChart.getBackgroundCharts().filtered(chart -> !chart.isComputed());
        if (backgroundCharts.isEmpty()) {
            LOGGER.error("No background charts exists that can relative combined to the base chart.");
            return;
        }

        // lets take the single series and analyze it to find out which time intervals are in use
        XYChart.Series<Long, Double> topSeries = baseChart.getAllSeries().get(0);
        int divisor = TimeSeriesSliceAnalyzer.analyzeSeries(topSeries.getData());

        // fill the following map and series with reference data (the absolute values of 100% baseline)
        Map<Long, Double> referenceMap = new HashMap<>();
        ObservableList<XYChart.Data<Long, Double>> newSeriesData = FXCollections.observableArrayList();

        for (XYChart.Data<Long, Double> data : topSeries.getData()) {
            // integer/long division will cut decimal places and results in fix time slots all values (sampling rate)
            // example: (1234ms / 1000ms) * 1000ms = 1000ms
            long key = (data.getXValue() / divisor) * divisor;
            referenceMap.put(key, data.getYValue());

            // the series has 100% as static value because it is the base line
            newSeriesData.add(new XYChart.Data<>(key, ONE_HUNDRED_PERCENT));
        }

        // collect and merge the data points of all series hold by all background charts
        SliceValueContainer sliceValues = new SliceValueContainer(referenceMap, divisor, interpolate);
        backgroundCharts.forEach(chart -> TimeSeriesSliceAnalyzer.assignToFilledSlices(sliceValues, chart.getAllSeries()));

        // calculate the percentage value and add result as new series
        List<XYChart.Series<Long, Double>> relativeChildSeriesList = new ArrayList<>();
        for (SliceDataSet dataSet : sliceValues) {
             relativeChildSeriesList.add(new XYChart.Series<>(
                     dataSet.getName(),
                     dataSet.dataAsObservableList((hundredPercentValue, currentSetValue) -> {
                         double onePercentValue = hundredPercentValue / ONE_HUNDRED_PERCENT;
                         double result = currentSetValue / onePercentValue;
                         return Double.isNaN(result) ? 0 : result;
                     })
             ));
        }

        deleteAllBackgroundCharts(true);

        baseChart.clear(true);
        String seriesName = topSeries.getName() + "-[100% Basis]";
        baseChart.addSeries(new XYChart.Series<>(seriesName, newSeriesData));
        baseChart.setLegendLabel(baseChart.getLegendLabel() + " [100% Basis]");
        baseChart.setSeriesColor(seriesName, new Color(0.6784314f, 0.84705883f, 0.9019608f, 1));
        baseChart.setComputed(true);
        baseChart.toXYChart().layout();

        baseChart.getYAxis().castToAxis().requestAxisLayout();
        baseChart.getYAxis().castToAxis().layout();

        String base100Id = UUID.randomUUID().toString();
        pushBaseToBackgroundChart(base100Id);
        ColoredChart newBgChart = getChartById(base100Id);
        changeBackgroundChartType(newBgChart, ChartType.AREA);
        newBgChart.toXYChart().layout();

        baseChart.clear(true);
        relativeChildSeriesList.forEach(baseChart::addSeries);
        baseChart.setLegendLabel(baseChart.getLegendLabel() + " [Relative values]");
        baseChart.setComputed(true);
        pushBaseToBackgroundChart(UUID.randomUUID().toString());

        baseChart.clear(true);
    }


    /**
     * Set the chart legend that will manages this chart and displays the metadata
     * of it's series/chart data.
     * Adding the legend will result in a bidirectional coupling including event listeners.
     *
     * @param chartLegend the chart legend that should coupled with the stacked chart.
     */
    /* package private */ void setChartLegend(StackedChartLegend chartLegend) {
        this.chartLegend = chartLegend;
        this.zoomableStackedChart.getBackgroundCharts().addListener((InvalidationListener)  observable ->
                this.chartLegend.setBackgroundChart(zoomableStackedChart.getBackgroundCharts()));

    }

    public BooleanProperty yAxisAlignmentActiveProperty() {
        return yAxisAlignmentActive;
    }

    //================================================================================================================
    // internal helper
    //================================================================================================================


    private double resolveMaxDateUpperBound(double baseUpperBound) {
        double bgMax = zoomableStackedChart.getBackgroundCharts()
                .stream().map(chart -> chart.getXAxis().upperBoundProperty().get())
                .max(Comparator.comparing(Double::valueOf))
                .orElse(0.0);

        return Math.max(bgMax, baseUpperBound);
    }

    private double resolveMinDateLowerBound(double baseLowerBound) {
        double bgMin = zoomableStackedChart.getBackgroundCharts()
                .stream().map(chart -> chart.getXAxis().lowerBoundProperty().get())
                .max(Comparator.comparing(Double::valueOf))
                .orElse(0.0);

        return (bgMin > 0 && bgMin < baseLowerBound) ? bgMin : baseLowerBound;
    }

    private Axis bindChartAxisToBase(ColoredChart baseChart, ColoredChart clonedChart) {
        Axis<Long> xAxis = clonedChart.getXAxis().castToAxis();

        // if the cloned DateAxis is not equal to the one of the base chart bind it
        if (xAxis != baseChart.getXAxis()) {
            xAxis.setVisible(false);
            // somehow the upper setVisible does not work (JavaFx doLayout() set axis explicitly to visible=true)
            xAxis.setOpacity(0.0);

            clonedChart.getXAxis().forceLowerBound(Double.NaN);
            clonedChart.getXAxis().forceUpperBound(Double.NaN);

            xAxis.setAutoRanging(false);

            clonedChart.getXAxis().lowerBoundProperty().bind(baseChart.getXAxis().lowerBoundProperty());
            clonedChart.getXAxis().upperBoundProperty().bind(baseChart.getXAxis().upperBoundProperty());
        }

        // bind yAxis scaling if active
        SpawningAxis spawningYAxis = clonedChart.getYAxis();
        Axis yAxis = spawningYAxis.castToAxis();
        yAxis.setAutoRanging(!yAxisAlignmentActive.get());
        if (yAxisAlignmentActive.get()) {
            clonedChart.getYAxis().lowerBoundProperty().bind(baseChart.getYAxis().lowerBoundProperty());
            clonedChart.getYAxis().upperBoundProperty().bind(baseChart.getYAxis().upperBoundProperty());
        }
        return yAxis;
    }

    /**
     * Changes the base chart to a new chart type
     *
     * @param chartType new chart type
     */
    private void changeBaseChartType(ChartType chartType) {

        boolean alignmentActive = yAxisAlignmentActive.get();

        if (alignmentActive) {
            // because replacing the base chart will break the axis binding. So we handle this explicitly.
            alignYAxis(false);
        }

        ColoredChart baseChart = zoomableStackedChart.getBaseChart();
        baseChart = ChartCloneUtil.cloneAs(baseChart, chartType);
        zoomableStackedChart.initBaseChart(baseChart, baseChart.getColor());

        alignYAxis(alignmentActive);
    }

    /**
     * Changes the base chart to a new chart type
     *
     * @param chartType new chart type
     */
    private void changeBackgroundChartType(ColoredChart backgroundChart, ChartType chartType) {

        // early exist if nothing to do
        if (chartType == backgroundChart.getChartType()) {
            return;
        }

        // clone with new type
        ColoredChart typeChangedChart = ChartCloneUtil.cloneAs(backgroundChart, chartType);

        // ensure that the cloned chart will scale with the base chart
        bindChartAxisToBase(zoomableStackedChart.getBaseChart(), typeChangedChart);

        // make line chart ready for panning
        Zoomable<Long, Double> zoom = new Zoomable<>(typeChangedChart.toXYChart());
        zoom.setIsHorizontalPanningAllowed(false);

        // replace existing root chart and zoomable
        int backgroundIndex = zoomableStackedChart.getBackgroundCharts().indexOf(backgroundChart);
        zoomableStackedChart.getBackgroundCharts().set(backgroundIndex, typeChangedChart);
        zoomableStackedChart.getZoomableBgChartList().set(backgroundIndex, zoom);

        zoomableStackedChart.rebuildChart();

        alignYAxis(yAxisAlignmentActive.get());
    }

}