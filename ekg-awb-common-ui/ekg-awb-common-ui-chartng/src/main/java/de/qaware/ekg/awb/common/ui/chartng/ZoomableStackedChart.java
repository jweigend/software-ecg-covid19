package de.qaware.ekg.awb.common.ui.chartng;

import de.qaware.ekg.awb.common.ui.chartng.legend.StackedChartLegend;
import de.qaware.ekg.awb.common.ui.chartng.zoom.Zoomable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps multiple XYCharts to make all zoomable and stackable
 */
public class ZoomableStackedChart extends StackPane {

    /**
     * The stylesheet used for the whole static styling of these
     * complex chart component and all it's child's
     */
    private static final String CHART_STYLESHEET = "ZoomableStackChartStyle.css";

    /**
     * The width of the y-axis legend independent from it's rotation.
     */
    public static final double AXIS_WIDTH = 60;

    /**
     * The gap between the multiple y-axis of the background charts
     * (right side)
     */
    private static final double Y_AXIS_SEPARATION = 15;

    /**
     * The controller that manages the business logic that controls these component.
     * The controller is only known to provided it to foreign components and initialize
     * with given constructor data
     */
    private StackedChartController controller = new StackedChartController(this);

    /**
     * The primary base chart that will change every time the time series data changes.
     * Is is a standard JavaFX chart of one specific type that is wrapped into a styleable
     * {@link ColoredChart} instance.
     */
    private ColoredChart baseChart;

    /**
     * Chart extension that brings various kinds of mouse actions / behaviours
     * that can use to interact with the this chart.
     */
    private ChartMouseBehaviour mouseBehaviour;

    /**
     * An ObservableList list contains all background charts, wrapped as
     * styleable {@link ColoredChart} instances, that are currently pushed on stack.
     */
    private ObservableList<ColoredChart> backgroundCharts = FXCollections.observableArrayList();

    /**
     * A list of the Zoomable decorated background charts that are
     * currently pushed on stack.
     */
    private List<Zoomable<Long, Double>> zoomableBackgroundCharts = new ArrayList<>();


    //==================================================================================================================
    //  Constructor
    //==================================================================================================================

    /**
     * Constructs a new ZoomableStackedChart instance that will use
     * the given ColoredChart as initial base and manages the specified chart legend.
     *
     * @param baseChart the base chart that will be the initial base chart on top of the stack
     * @param chartLegend the chart legend that will interact with this instance
     */
    public ZoomableStackedChart(ColoredChart baseChart, StackedChartLegend chartLegend) {
        this(baseChart);
        getController().setChartLegend(chartLegend);
    }

    /**
     * Constructs a new Zoomable XYChart with multiple axes
     *
     * @param baseChart the base chart that will be the initial base chart on top of the stack
     */
    public ZoomableStackedChart(ColoredChart baseChart) {

        this.mouseBehaviour = new ChartMouseBehaviour(this);

        this.initBaseChart(baseChart, Color.BLACK);

        this.backgroundCharts.addListener((javafx.beans.Observable observable) -> rebuildChart());

        getStyleClass().addAll("zoomableChart");

        getStylesheets().add(getClass().getResource(CHART_STYLESHEET).toExternalForm());
    }

    //==================================================================================================================
    // public API
    //==================================================================================================================


    public Node getMouseOverlays() {
        return mouseBehaviour.getMouseOverlay();
    }

    /**
     * Returns the controller that manages this chart
     *
     * @return a StackedChartController instance
     */
    public StackedChartController getController() {
        return controller;
    }

    /**
     * Sets the given label to the Y-axis legend
     * of the base chart.
     * This method should be used, to set the label describes the series
     * data that add subsequently via addSeriesToBase(XYChart.Series)
     *
     * @param label the label that will add to Y axis and describes the shown data
     */
    public void setBaseYAxisLabel(String label) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setBaseYAxisLabel(label));
            return;
        }

        baseChart.getYAxis().castToAxis().setLabel(label);
    }

    /**
     * Adds a series to the current base chart.
     *
     * @param chartSeries the series that should added to the base chart
     */
    public void addSeriesToBase(XYChart.Series<Long, Double> chartSeries) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> addSeriesToBase(chartSeries));
            return;
        }

        baseChart.addSeries(chartSeries);
    }

    /**
     * Deletes all data hold by the underlying base chart like the series,
     * visible states or labels
     *
     * @param resetAxis boolean flag that controls if the chart axis should reset than new data will loaded or not.
     */
    public void clearBase(boolean resetAxis) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> clearBase(resetAxis));
            return;
        }

        mouseBehaviour.reset();
        baseChart.clear(resetAxis);
    }

    //==================================================================================================================
    // ZoomableStackedChart package private API
    //==================================================================================================================

    /**
     * Returns the list of wrapped (as Zoomable) decorated JavaFX charts
     * that represents all background charts on the stack
     *
     * @return a list of Zoomable decorated background charts
     */
    /* package private */ List<Zoomable<Long, Double>> getZoomableBgChartList() {
        return zoomableBackgroundCharts;
    }

    /**
     * Returns the base chart, that is the primary chart in the foreground
     * that is the only one without immutable series data.
     *
     * @return the base chart as {@link ColoredChart} instance
     */
    public ColoredChart getBaseChart() {
        return baseChart;
    }

    /**
     * Returns a list with all background charts that are currently
     * on the stack.
     *
     * @return a list of all background charts
     */
    public ObservableList<ColoredChart> getBackgroundCharts() {
        return backgroundCharts;
    }

    /**
     * Rebuilds the complete stacked chart including all it's contained
     * charts (base and background charts).
     * This will cause a complete re-rendering of the chart.
     *
     * Rebuilding means to remove all items form the wrapping StackPane and
     * add all chart elements (base chart, background charts, cross hair, ...) again.
     */
    /* package private */ void rebuildChart() {

        // clearBase base an background charts from the stack
        getChildren().clear();

        // add (resized) base chart again
        getChildren().add(resizeBaseChart(baseChart.toXYChart()));

        // add all visible (resized) background charts again
        backgroundCharts.stream()
                .filter(ColoredChart::isVisible)
                .forEach(backgroundChart -> getChildren().add(resizeBackgroundChart(backgroundChart)));

        mouseBehaviour.resetBehaviourSetup();
    }

    /**
     * Initialize the base chart by wrap it with a Zoomable container
     * and set/bind multiple properties like color, size properties and mouse events.
     *
     * @param baseChart the base chart to initialize
     * @param color the color that should assigned to the chart (not it series)
     */
    /* package private */ void initBaseChart(ColoredChart baseChart, Color color) {

        // this assignment happens than the constructor all this method an also than the chart type changes
        this.baseChart = baseChart;

        // style chart
        baseChart.setChartColor(color);
        baseChart.getYAxis().castToAxis().setPrefWidth(AXIS_WIDTH);
        baseChart.getYAxis().castToAxis().setMaxWidth(AXIS_WIDTH);

        setAlignment(Pos.CENTER_LEFT);

        mouseBehaviour.setupNewBaseChart(baseChart);

        rebuildChart();
    }

    //==================================================================================================================
    // internal logic to manage the chart
    //==================================================================================================================

    /**
     * Resize the width the base chart in due consideration
     * to the amount of visible background charts that will
     * consume space on the right hand side with it y-axis legend.
     *
     * @param xyChart the base chart to resize
     * @return the resized chart.
     */
    private Node resizeBaseChart(XYChart<Long, Double> xyChart) {

        long countVisible = backgroundCharts.stream().filter(ColoredChart::isVisible).count();

        xyChart.prefWidthProperty().bind(widthProperty().subtract((AXIS_WIDTH + Y_AXIS_SEPARATION) * countVisible));
        xyChart.maxWidthProperty().bind(widthProperty().subtract((AXIS_WIDTH + Y_AXIS_SEPARATION) * countVisible));

        return xyChart;
    }

    private Node resizeBackgroundChart(ColoredChart backgroundChart) {

        XYChart xyChart = backgroundChart.toXYChart();

        HBox hBox = new HBox(xyChart);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.prefHeightProperty().bind(heightProperty().subtract(0));
        hBox.maxHeightProperty().bind(heightProperty().subtract(0));
        hBox.prefWidthProperty().bind(widthProperty());
        hBox.setMouseTransparent(true);

        long countVisible = backgroundCharts.stream().filter(ColoredChart::isVisible).count();

        xyChart.prefWidthProperty().bind(widthProperty().subtract((AXIS_WIDTH + Y_AXIS_SEPARATION) * countVisible));
        xyChart.maxWidthProperty().bind(widthProperty().subtract((AXIS_WIDTH + Y_AXIS_SEPARATION) * countVisible));

        countVisible = backgroundCharts.subList(0, backgroundCharts.indexOf(backgroundChart))
                .stream().filter(ColoredChart::isVisible).count();

        xyChart.translateXProperty().bind(baseChart.getYAxis().castToAxis().widthProperty());
        xyChart.getYAxis().setTranslateX((AXIS_WIDTH + Y_AXIS_SEPARATION) * countVisible);

        return hBox;
    }

    /**
     * Sets a zoom changed handler
     *
     * @param zoomChangedHandler zoom changed handler
     */
    public final void setOnZoomChanged(EventHandler<Zoomable.ZoomEvent> zoomChangedHandler) {
        mouseBehaviour.setZoomChangedHandler(zoomChangedHandler);
    }
}
