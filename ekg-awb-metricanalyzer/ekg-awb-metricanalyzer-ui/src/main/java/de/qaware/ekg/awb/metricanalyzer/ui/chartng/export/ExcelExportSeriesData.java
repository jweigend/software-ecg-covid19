package de.qaware.ekg.awb.metricanalyzer.ui.chartng.export;

import javafx.scene.chart.XYChart;

import java.util.List;

/**
 * Tiny Container to storage series name and the series data points
 */
public class ExcelExportSeriesData {

    private String seriesName;

    private List<XYChart.Data<Long, Double>> seriesData;

    public ExcelExportSeriesData(String seriesName, List<XYChart.Data<Long, Double>> seriesData) {
        this.seriesName = seriesName;
        this.seriesData = seriesData;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public List<XYChart.Data<Long, Double>> getSeriesData() {
        return seriesData;
    }

    public XYChart.Data<Long, Double> getDataAt(int idx) {

        if (idx >= seriesData.size()) {
            return null;
        }

        return seriesData.get(idx);
    }
}
