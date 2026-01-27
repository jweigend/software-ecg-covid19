package de.qaware.ekg.awb.common.ui.chartng.legend;

import de.qaware.ekg.awb.common.ui.chartng.ColoredChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.HBox;

import java.util.UUID;

/**
 * This class is a view component that provides a header bar with meta data
 * and (view) actions to the base chart and a legend list of all series
 * (represented by the {@link LegendSeriesItem} class) that belongs to the chart.
 *
 * In addition to the super class {@link LegendChartItem} and in contrast to the
 * {@link LegendBackgroundChartItem} this component provides additional buttons
 * that triggers various actions like proportional combine or clear all series.
 */
public class LegendBaseChartItem extends LegendChartItem {

    /**
     * The decorated (styled) chart that is represented
     * by this LegendChartItem.
     */
    private ColoredChart baseChart;

    /**
     * A wrapper container to layout labels for the chart
     * and according buttons that trigger the legend view
     * actions belongs to the chart.
     */
    private HBox baseChartCtrlBar = new HBox();

    /**
     * Constructs a new instance of the LegendBaseChartItem
     * that will bind to the given chart.
     *
     * @param baseChart the underlying base chart that will bind to this legend item.
     */
    public LegendBaseChartItem(ColoredChart baseChart) {
        super(baseChart.getId());

        getStyleClass().add("legendBaseChartItem");

        this.baseChart = baseChart;
        initBaseCtrlBar();
        initSeriesItems(baseChart);
    }


    //================================================================================================================
    // internal helper to initialize the UI component
    //================================================================================================================

    private void initBaseCtrlBar() {
        Label baseChartLabel = new Label();
        baseChartLabel.textProperty().bind(baseChart.toXYChart().getYAxis().labelProperty());

        boolean isEmpty = baseChart.isEmpty();

        baseChartCtrlBar.getStyleClass().add("baseChartCtrlBar");
        baseChartCtrlBar.getChildren().add(createChartVisibleCheckBox(true));
        baseChartCtrlBar.getChildren().add(baseChartLabel);
        baseChartCtrlBar.getChildren().add(createChartTypeCombo(baseChart, isEmpty));
        baseChartCtrlBar.getChildren().add(createPushBaseToBackgroundButton(isEmpty));
        baseChartCtrlBar.getChildren().add(createProportionalCombineButton(isEmpty));
        setTop(baseChartCtrlBar);
    }


    //================================================================================================================
    // Action handler for the LegendBaseChartItem main bar (checkbox, combo box & buttons)
    //================================================================================================================

    private Button createPushBaseToBackgroundButton(boolean isDisabled) {
        Button pushToBackgroundButton = new Button("Push Chart");
        pushToBackgroundButton.setDisable(isDisabled);
        pushToBackgroundButton.setOnMouseClicked(event -> {
            actions.pushToBackground(UUID.randomUUID().toString());
            LegendBaseChartItem.this.requestFocus();
        });
        return pushToBackgroundButton;
    }

    private Button createClearButton(boolean isDisabled) {
        Button clearButton = new Button("Clear");
        clearButton.setDisable(isDisabled);
        clearButton.setOnMouseClicked(event -> {
            actions.clearAll();
            LegendBaseChartItem.this.requestFocus();
        });
        return clearButton;
    }

    private Button createZoomFitButton(boolean isDisabled) {
        Button zoomFitButton = new Button("ZoomFit");
        zoomFitButton.setDisable(isDisabled);
        zoomFitButton.setOnMouseClicked(event -> {
            actions.zoomToFit();
            LegendBaseChartItem.this.requestFocus();
        });
        return zoomFitButton;
    }

    private SplitMenuButton createProportionalCombineButton(boolean isDisabled) {

        MenuItem combineAbsolute = new MenuItem("Combine background charts with this");
        combineAbsolute.setOnAction(event -> {
            actions.combineBgAbsolute();
            LegendBaseChartItem.this.requestFocus();
        });

        SplitMenuButton button = new SplitMenuButton(combineAbsolute);
        button.setDisable(isDisabled);
        button.setText("Relativize background charts with this");
        button.setPrefWidth(240);
        button.setOnAction(event -> {
            actions.combineBgChartsRelativeToBase(false);
            LegendBaseChartItem.this.requestFocus();
        });

        return button;
    }
}