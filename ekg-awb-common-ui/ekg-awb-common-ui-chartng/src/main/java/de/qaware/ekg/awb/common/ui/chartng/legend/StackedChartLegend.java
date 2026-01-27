package de.qaware.ekg.awb.common.ui.chartng.legend;

import de.qaware.ekg.awb.common.ui.chartng.ColoredChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * UI component that represents the chart legend that shows a block
 * for each stacked chart and a nested list of series items for
 * each of the chart.
 */
public class StackedChartLegend extends BorderPane {

    private static final String CHART_LEGEND_STYLESHEET = "/de/qaware/ekg/awb/common/ui/chartng/StackedChartLegendStyle.css";

    private VBox backgroundChartContainer = new VBox();

    private ColoredChart baseChart;

    private LegendActionCallbacks actions = new LegendActionCallbacks();

    /**
     * Constructs a new instance of StackedChartLegend
     */
    public StackedChartLegend() {
        getStyleClass().addAll("stackedChartLegend");
        getStylesheets().add(getClass().getResource(CHART_LEGEND_STYLESHEET).toExternalForm());
        setCenter(backgroundChartContainer);
    }

    //=================================================================================================================
    // StackedChartLegend API
    //=================================================================================================================

    public void setBaseChart(ColoredChart baseChart) {
        this.baseChart = baseChart;
        this.baseChart.addListener(chart -> updateBaseChartControl());

        updateBaseChartControl();
    }

    public void setBackgroundChart(List<ColoredChart> backgroundCharts) {
        backgroundChartContainer.getChildren().clear();

        for (ColoredChart backgroundChart : backgroundCharts) {
            LegendBackgroundChartItem chartItem = new LegendBackgroundChartItem(backgroundChart);
            chartItem.setActionHandler(actions);
            backgroundChartContainer.getChildren().add(chartItem);
        }
    }

    //=================================================================================================================
    // controller implementation of the chart legend
    //=================================================================================================================

    private void updateBaseChartControl() {
        LegendBaseChartItem baseChartItem = new LegendBaseChartItem(baseChart);
        baseChartItem.setActionHandler(actions);
        setTop(baseChartItem);
    }

    public void setActionHandler(LegendActionCallbacks chartLegendActions) {
        this.actions = chartLegendActions;
    }

}
