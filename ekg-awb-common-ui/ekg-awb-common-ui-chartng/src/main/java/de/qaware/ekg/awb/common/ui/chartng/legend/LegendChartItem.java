package de.qaware.ekg.awb.common.ui.chartng.legend;

import de.qaware.ekg.awb.common.ui.chartng.ChartType;
import de.qaware.ekg.awb.common.ui.chartng.ColoredChart;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/**
 * Abstract base class that represents a chart (and it's series) in the legend panel.
 * A chart item will show a header with the display name of the chart and some UI controls for chart actions.
 * Above the the header the chart item displays a vertical list with all series items.
 */
public abstract class LegendChartItem extends BorderPane {

    /**
     * Default empty callback for chart actions.
     * It is designed to be replaced by actions set by other UI components.
     */
    protected LegendActionCallbacks actions = new LegendActionCallbacks();

    /**
     * The unique id of the chart this ChartItem represents
     */
    protected String chartId;

    /**
     * Constructs a new LegendChartItem instance
     * hard bind to the chartId
     *
     * @param chartId the unique chart id
     */
    public LegendChartItem(String chartId) {
        this.chartId = chartId;
    }

    /**
     * Sets/replace the action callbacks with new one that can use to
     * delegate user interaction events to specific business logic in other parts of EKG-UI
     *
     * @param chartLegendActions a LegendActionCallbacks instance that define callback actions for various interactions
     */
    public void setActionHandler(LegendActionCallbacks chartLegendActions) {
        this.actions = chartLegendActions;
    }

    /**
     * Creates and configure for each series in the base chat an according {@link LegendSeriesItem} instance
     * and add it among each other using a VBox to the center area of this LegendBaseChartItem (BorderPane).
     * The series will be instantiated in two modes if multiple series with same name exists.
     * In this case the first one will a lead item with active color chooser, the other ones will be locked.
     *
     * @param chart the chart instance represents by this instance that contains the series to initialize
     */
    protected void initSeriesItems(ColoredChart chart) {
        VBox seriesContainer = new VBox(2);

        for (Map.Entry<String, List<ColoredChart.ColoredSeries>> entry : chart.getColoredSeriesMap().entrySet()) {

            List<ColoredChart.ColoredSeries> coloredSeries = entry.getValue();

            if (coloredSeries.size() > 0) {
                seriesContainer.getChildren().add(initSeriesItem(coloredSeries.get(0),false));
            }

            if (coloredSeries.size() > 1) {
                for (int i=1; i<coloredSeries.size(); i++) {
                    seriesContainer.getChildren().add(initSeriesItem(coloredSeries.get(i),true));
                }
            }
        }

        if (seriesContainer.getChildren().isEmpty()) {
            Label noSeriesDataLabel = new Label("- no data available -");
            noSeriesDataLabel.getStyleClass().add("noSeriesLabel");
            seriesContainer.getChildren().add(noSeriesDataLabel);
        }

        setCenter(seriesContainer);
    }

    /**
     * Creates a LegendSeriesItem, configure it with the displayData and register action handler for
     * controlling visibility and color change.
     *
     * The latter will be locked if specified via colorIsLocked flag. This should the case if a set of series
     * with the same name exists and the color should only change for whole set.
     *
     * @param coloredSeries the display data that specifies name and color of the series
     * @param colorIsLocked a boolean flag that controls if the color change functionality will be active or not
     * @return an created, configured and bind LegendSeriesItem
     */
    protected LegendSeriesItem initSeriesItem(ColoredChart.ColoredSeries coloredSeries, boolean colorIsLocked) {

        LegendSeriesItem seriesItem = new LegendSeriesItem(coloredSeries, colorIsLocked);

        seriesItem.setSeriesColorChangeHandler((seriesName, seriesColor)
                -> actions.changeSeriesColor(chartId, seriesName, seriesColor));

        return seriesItem;
    }

    /**
     * Create a checkbox that shows/controls the visible state of the whole chart and all it's series.
     * The box can locked to a disabled state if the caller doesn't won't that user can change the visible state.
     *
     * @param isDisabled true to lock the checkbox (will always be disabled)
     * @return the created check box bind to the according callback action
     */
    protected CheckBox createChartVisibleCheckBox(boolean isDisabled) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        checkBox.setDisable(isDisabled);
        if (isDisabled) {
            checkBox.setOpacity(0.5);
        }
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            actions.setChartVisible(chartId, newValue);
        });

        return checkBox;
    }

    /**
     * Create a ComboBox that provides the option to switch between the different chart types.
     * The box can locked to a disabled state if the caller doesn't won't that user can change the visible state.
     *
     * @param chart the chart that should changed in it's type than the user selects another type
     * @param isDisabled true to lock the ComboBox (will always be disabled)
     * @return the created ComboBox bind to the according callback action
     */
    protected ComboBox<ChartType> createChartTypeCombo(ColoredChart chart, boolean isDisabled) {

        ComboBox<ChartType> chartTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                ChartType.LINE, ChartType.AREA, ChartType.SUM, ChartType.POINT
        ));

        chartTypeComboBox.setDisable(isDisabled);
        chartTypeComboBox.setValue(chart.getChartType());
        chartTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldType, newType) -> {
            actions.changeChartType(chart.getId(), newType);
            LegendChartItem.this.requestFocus();
        });

        return chartTypeComboBox;
    }
}
