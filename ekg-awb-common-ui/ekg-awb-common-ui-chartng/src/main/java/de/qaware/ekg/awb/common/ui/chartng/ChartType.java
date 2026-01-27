package de.qaware.ekg.awb.common.ui.chartng;

import javafx.scene.chart.*;

import java.util.Map;

/**
 * Enumeration that lists all chart types that
 * are supported by Software-EKG AWB.
 */
public enum ChartType {

    /**
     * Chart type that shows a single line that connects
     * each point in the series.
     */
    LINE,

    /**
     * Area chart that renders a filled area in the bottom
     * of the series line.
     */
    AREA,

    /**
     * Point chart that shows single points for each point
     * in the series instead of lines or filled areas.
     */
    POINT,

    /**
     * Stacked charts that show the sum of
     * all series as upper bound line.
     */
    SUM;

    /**
     * A map that provides the assignment of ChartType enums to
     * the concrete chart type implementations.
     */
    private static final Map<Class, ChartType> CHART_TYPE_MAP = Map.of(
        LineChart.class, LINE,
        AreaChart.class, AREA,
        ScatterChart.class, POINT,
        StackedAreaChart.class, SUM
    );

    public static ChartType valueOf(Class<? extends XYChart> chartClass) {
        return CHART_TYPE_MAP.getOrDefault(chartClass, LINE);
    }
}
