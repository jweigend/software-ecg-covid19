package de.qaware.ekg.awb.common.ui.chartng.legend;

import de.qaware.ekg.awb.common.ui.chartng.ColoredChart;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

/**
 * This class represents a info/control line in the StackedChartLegend that represents
 * 1 of n series that belongs to a chart. So multiple of this items will belong to a
 * LegendBaseChartItem or LegendBackgroundChartItem that represents the chart of this
 * series belongs to.
 *
 * The LegendSeriesItem displays the series name and provides functionality to control
 * the visible state of a single series and it's color.
 */
public class LegendSeriesItem extends GridPane {

    /**
     * The pseudo css class that will used to indicated that this
     * series item is in selected (highlighted) state
     */
    private static PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /**
     * A lock icon that will display in front of the color picker to clarify for the user
     * that the color picker state is locked and can't used to change the series color.
     */
    private static final Image ICON_IMAGE = new Image(
            LegendSeriesItem.class.getResourceAsStream("/de/qaware/ekg/awb/common/ui/chartng/icons/lock-icon.png"),
            14, 14, true, true);

    /**
     * The ColoredSeries that is represented by this legend series item
     */
    private ColoredChart.ColoredSeries coloredSeries;

    /**
     * The color picker that can used to change the color
     * of the the series represented by this item.
     */
    private ColorPicker seriesColorPicker;

    /**
     * The label of the series hold as click area
     * to select the series
     */
    private Label seriesNameLabel;

    /**
     * flag that indicates if the series item is selected or not
     */
    private boolean isSelected = false;

    /**
     * Constructs a new LegendSeriesItem that will show
     * the series name and initialized the color picker with the
     * specified series color.
     *
     * @param coloredSeries the decorated JavaFX series that should represented by this legend item
     * @param colorIsLocked a boolean flag that controls if the color change functionality will be active or not
     */
    public LegendSeriesItem(ColoredChart.ColoredSeries coloredSeries, boolean colorIsLocked) {

        this.getStyleClass().add("legendSeriesItem");

        this.coloredSeries = coloredSeries;

        initGridConstraints();

        CheckBox seriesVisibleCheckBox = new CheckBox();
        seriesVisibleCheckBox.setSelected(coloredSeries.isVisible());

        seriesVisibleCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                coloredSeries.setVisible(newValue));

        this.add(seriesVisibleCheckBox, 1, 0);

        seriesColorPicker = new ColorPicker(coloredSeries.getColor());
        seriesColorPicker.setDisable(colorIsLocked);
        seriesColorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
        this.add(seriesColorPicker, 2, 0);

        if (colorIsLocked) {
            ImageView lockIcon = new ImageView(ICON_IMAGE);
            lockIcon.getStyleClass().add("lockIcon");
            this.add(lockIcon, 2, 0);
        }

        seriesNameLabel = new Label(coloredSeries.getSeriesName());
        seriesNameLabel.getStyleClass().add("seriesLabel");
        seriesNameLabel.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, coloredSeries.isSelected());
        seriesNameLabel.setOnMouseClicked(event -> {
            this.isSelected = !isSelected;
            seriesNameLabel.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, isSelected);
            coloredSeries.setSelected(isSelected);
        });
        this.add(seriesNameLabel, 3, 0);
    }

    /**
     * Set a callback that will invoked than the user changes the series
     * color via color picker. The callback is a {@link BiConsumer} that
     * will get the series name and the color as argument and should use
     * the data to delegate the change to the chart that render the series.
     *
     * @param changeHandler the change handler that proceed the color change actions
     */
    public void setSeriesColorChangeHandler(BiConsumer<String, Color> changeHandler) {
        seriesColorPicker.setOnAction(actionEvent ->
                changeHandler.accept(coloredSeries.getSeriesName(), seriesColorPicker.getValue()));
    }

    /**
     * Set the GridPane Constraints programmatically as only styling part that can't
     * define via CSS stylesheet. All other styling should define using CSS.
     */
    private void initGridConstraints() {
        this.getRowConstraints().add(new RowConstraints(24, 24, 24));

        // (0) indent series line (first column = empty)
        this.getColumnConstraints().add(new ColumnConstraints(30, 30, 30));

        // (1) check box for visibility (second column)
        this.getColumnConstraints().add(new ColumnConstraints(28, 28, 28));

        // (2) check box for color picker (third column)
        this.getColumnConstraints().add(new ColumnConstraints(26, 28, 26));

        // (3) check box for the series label (last column)
        this.getColumnConstraints().add(new ColumnConstraints(50, 50, 9999,
                Priority.ALWAYS, HPos.LEFT, true));
    }
}
