package de.qaware.ekg.awb.common.ui.chartng.legend;

import de.qaware.ekg.awb.common.ui.chartng.ChartType;
import javafx.scene.paint.Color;

public class LegendActionCallbacks {

    /**
     * @param interpolate
     */
    public void combineBgChartsRelativeToBase(boolean interpolate) {
        // default: no operation
    }

    public void combineBgAbsolute() {
        // default: no operation
    }

    public void pushToBackground(String newChartId) {
        // default: no operation
    }

    public void clearAll() {
        // default: no operation
    }

    public void deleteBgChart(String chartId) {
        // default: no operation
    }

    public void deleteAllBgCharts() {
        // default: no operation
    }

    public void changeChartType(String chartId, ChartType newChartType) {
        // default: no operation
    }

    public void setChartVisible(String chartId, boolean isVisible) {
        // default: no operation
    }

    public void setChartSeriesVisible(String chartId, String seriesName, boolean isVisible) {
        // default: no operation
    }

    public void zoomToFit() {
        // default: no operation
    }

    public void changeChartColor(String chartId, Color newColor) {
        // default: no operation
    }

    public void changeSeriesColor(String chartId, String seriesName, Color newColor) {
        // default: no operation
    }
}
