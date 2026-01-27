package de.qaware.ekg.awb.common.ui.chartng;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A util that produce exact clones of ColoredChart instances
 * by using the knowledge of all their internals.
 */
public class ChartCloneUtil {

    /**
     * Clone the given sourceChart 1:1 including all series data and it's properties.
     * All necessary internal properties will cloned including styling, chart type and bindings.
     *
     * @param sourceChart the source chart to clone
     * @return a 1:1 clone of the given source chart
     */
    public static ColoredChart clone(ColoredChart sourceChart) {
        return cloneAs(sourceChart, sourceChart.getChartType());
    }

    /**
     * Clone the given sourceChart 1:1 but without any series data.
     * All necessary internal properties will cloned including styling, chart type and bindings.
     * The cloned chart will have the same metadata but no series data.
     *
     * @param sourceChart the source chart to clone
     * @return the cloned chart without series data
     */
    public static ColoredChart cloneWithoutData(ColoredChart sourceChart) {
        return cloneAs(sourceChart, sourceChart.getChartType(), false);
    }

    /**
     * Clone the given sourceChart to the target type that is specified by the chartType parameter.
     * All necessary internal properties will cloned including styling and binding except the
     * properties that didn't exists in the target type.
     *
     * @param sourceChart the source chart to clone
     * @param chartType the target chart type (Area, Line, ....)
     * @return the cloned chart in the target type
     */
    public static ColoredChart cloneAs(ColoredChart sourceChart, ChartType chartType) {
        return cloneAs(sourceChart, chartType, true);
    }

    //================================================================================================================
    //  internal logic to clone the charts
    //================================================================================================================

    /**
     * Clone the given sourceChart to the target type that is specified by the chartType parameter.
     * All necessary internal properties will cloned including styling and binding except the
     * properties that didn't exists in the target type and the series data if parameter 'doCloneData' = false.
     *
     * @param source the source chart to clone
     * @param chartType the target chart type (Area, Line, ....)
     * @param doCloneData a boolean flag that controls if series data will also cloned or not.
     *
     * @return the cloned chart in the target type
     */
    private static ColoredChart cloneAs(ColoredChart source, ChartType chartType, boolean doCloneData) {

        Axis<Long> xAxis = source.getXAxis().spawn().castToAxis();
        Axis<Double> yAxis = source.getYAxis().spawn().castToAxis();

        XYChart<Long, Double> xyChart;

        switch (chartType) {
            case AREA:
                xyChart = new AreaChart<>(xAxis, yAxis);
                break;
            case SUM:
                xyChart = new StackedAreaChart<>(xAxis, yAxis);
                break;
            case POINT:
                xyChart = new ScatterChart<>(xAxis, yAxis);
                break;
            case LINE:
            default:
                xyChart = new LineChart<>(xAxis, yAxis);
        }

        ColoredChart clonedChart = new ColoredChart(xyChart, source.getAxisWidth(), source.getColor(), source.getId());
        fillFromSource(clonedChart, source, doCloneData);

        return clonedChart;
    }


    private static void fillFromSource(ColoredChart clonedChart, ColoredChart sourceChart, boolean cloneData) {

        XYChart<Long, Double> internalClonedChart = clonedChart.toXYChart();
        XYChart<Long, Double> internalSourceChart = sourceChart.toXYChart();

        internalClonedChart.resize(internalSourceChart.getWidth(), internalSourceChart.getHeight());

        clonedChart.setComputed(sourceChart.isComputed());

        // resolve internal components of JavaFX chart
        Node target = internalClonedChart.lookup(".chart-content").lookup(".chart-plot-background");
        Node source = sourceChart.toXYChart().lookup(".chart-content").lookup(".chart-plot-background");
        target.setStyle(source.getStyle());

        // copy state of grid lines
        internalClonedChart.setVerticalZeroLineVisible(internalSourceChart.isVerticalZeroLineVisible());
        internalClonedChart.setHorizontalZeroLineVisible(internalSourceChart.isHorizontalZeroLineVisible());
        internalClonedChart.setVerticalGridLinesVisible(internalSourceChart.getVerticalGridLinesVisible());
        internalClonedChart.setHorizontalGridLinesVisible(internalSourceChart.isHorizontalGridLinesVisible());

        // copy styles
        internalClonedChart.setAnimated(false);
        internalClonedChart.setLegendVisible(sourceChart.isLegendVisible());
        internalClonedChart.getStylesheets().addAll(sourceChart.getStylesheets());

        // we can stop here if we don't need to copy the series data
        if (!cloneData) {
            return;
        }

        Map<String, List<ColoredChart.ColoredSeries>> coloredSeriesMap = new HashMap<>();

        List<XYChart.Series<Long, Double>> tempSeriesList = new ArrayList<>();

        for (Map.Entry<String, List<ColoredChart.ColoredSeries>> entry : sourceChart.getColoredSeriesMap().entrySet()) {

            List<ColoredChart.ColoredSeries> coloredSeries = new ArrayList<>();
            coloredSeriesMap.put(entry.getKey(), coloredSeries);

            // clone ColoredSeries by copy it's internal data & properties
            for (ColoredChart.ColoredSeries sourceSeries : entry.getValue()) {
                ObservableList<XYChart.Data<Long, Double>> data =
                        FXCollections.observableArrayList(sourceSeries.getFxSeries().getData());

                // create JavaFX series and add it first to cloned target chart, otherwise the following step will fail
                XYChart.Series<Long, Double> clonedSeries = new XYChart.Series<>(sourceSeries.getSeriesName(), data);
                tempSeriesList.add(clonedSeries);

                // now decorate the series and copy the styles / view states
                ColoredChart.ColoredSeries clone = new ColoredChart.ColoredSeries(clonedSeries,
                        sourceSeries.getColor(), sourceSeries.isVisible(), sourceSeries.isSelected());

                coloredSeries.add(clone);
            }
        }

        internalClonedChart.getData().addAll(tempSeriesList);
        coloredSeriesMap.values().forEach(seriesList -> seriesList.forEach(ColoredChart.ColoredSeries::init));

        clonedChart.setColoredSeriesMap(coloredSeriesMap);
    }
}
