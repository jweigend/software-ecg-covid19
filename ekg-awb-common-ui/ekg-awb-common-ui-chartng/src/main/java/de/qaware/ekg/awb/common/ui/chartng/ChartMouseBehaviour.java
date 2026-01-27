package de.qaware.ekg.awb.common.ui.chartng;

import de.qaware.ekg.awb.common.ui.chartng.zoom.Zoomable;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.qaware.ekg.awb.common.ui.chartng.zoom.MouseMode.PAN;
import static de.qaware.ekg.awb.common.ui.chartng.zoom.MouseMode.ZOOM;

/**
 * Chart extension that brings various kinds of mouse actions like zoom, pan,
 * or context menu that can use to interact with a ZoomableStackedChart instance.
 */
public class ChartMouseBehaviour {

    private static final double STROKE_WIDTH = 0.4;

    private static final int SPACING_10 = 10;

    //----------------------------------------------------------------------------------------------------------------
    // own properties that extend the chart
    //----------------------------------------------------------------------------------------------------------------

    private CrossHair crossHair = new CrossHair(STROKE_WIDTH);

    private Pane crossHairWindow;

    private DetailPopupPane detailPopup;

    private XYChart<Long, Double> movable = null;


    //----------------------------------------------------------------------------------------------------------------
    // properties that store references to target components
    // this component will extend with mouse handling behaviour
    //----------------------------------------------------------------------------------------------------------------

    private ZoomableStackedChart zoomableStackedChart;

    private ColoredChart baseChart;

    /**
     * The Zoomable decorated representation of the underlying JavaFX chart
     * that add the zoom functionality to the standard chart.
     */
    private Zoomable<Long, Double> zoomableBaseChart = null;

    private EventHandler<Zoomable.ZoomEvent> zoomChangedHandler;


    //================================================================================================================
    // constructor / API of this class
    //================================================================================================================

    /**
     * Constructs a new instance of ChartMouseBehaviour that will extend
     * the given ZoomableStackedChart instance if different mouse actions.
     *
     * @param zoomableStackedChart the chart that should extend with this behaviour extension.
     */
    public ChartMouseBehaviour(ZoomableStackedChart zoomableStackedChart) {
        this.zoomableStackedChart = zoomableStackedChart;
        this.baseChart = zoomableStackedChart.getBaseChart();
        this.detailPopup = new DetailPopupPane(zoomableStackedChart);

        initCrossHairWindow();
    }

    public void reset() {
        zoomableBaseChart.reset();
    }

    public void resetBehaviourSetup() {

        // add cross hair
        zoomableStackedChart.getChildren().add(crossHairWindow);

        // add zoom rectangle
        zoomableStackedChart.getChildren().add(zoomableBaseChart.getZoomRectangle());
    }

    /**
     * Method to initialize the baseChart of the decorated ZoomableStackedChart instance
     * for the first time.
     * This method should only called once and add this behavior implementation to the base chart
     * and the ZoomableStackedChart.
     *
     * @param baseChart the base chart that is the primary chart of the ZoomableStackedChart
     */
    public void setupNewBaseChart(ColoredChart baseChart) {

        this.baseChart = baseChart;

        // zoomable
        if (zoomableBaseChart != null) {
            zoomableStackedChart.getChildren().removeAll(zoomableBaseChart.getNodeRepresentation());
        }

        zoomableBaseChart = new Zoomable<>(baseChart.toXYChart(), zoomableBaseChart);
        zoomableStackedChart.getChildren().addAll(zoomableBaseChart.getNodeRepresentation());

        bindMouseEvents();
    }

    /**
     * Sets a EventHandler<Zoomable.ZoomEvent> handler instance that invoked
     * every time the user zooms into the chart or out of it.
     *
     * @param zoomChangedHandler zoom changed handler
     */
    public final void setZoomChangedHandler(EventHandler<Zoomable.ZoomEvent> zoomChangedHandler) {
        this.zoomChangedHandler = zoomChangedHandler;
        zoomableBaseChart.setOnZoomChanged(zoomChangedHandler);
    }


    public Node getMouseOverlay() {
        return detailPopup;
    }

    //================================================================================================================
    // internal handler methods
    //================================================================================================================


    private void initCrossHairWindow() {
        crossHairWindow = new AnchorPane();
        crossHairWindow.prefHeightProperty().bind(zoomableStackedChart.heightProperty());
        crossHairWindow.prefWidthProperty().bind(zoomableStackedChart.widthProperty());
        crossHairWindow.setMouseTransparent(true);
        zoomableStackedChart.getChildren().add(crossHairWindow);
    }

    /**
     * Binds mouse events and according event handler to the ZoomableStackedChart
     * extends by this class.
     * This binding enables the cross hair function, the zooming, details window and panning
     */
    private void bindMouseEvents() {
        zoomableStackedChart.setOnMouseMoved(null);
        zoomableStackedChart.setMouseTransparent(false);

        // resolve all ui element except the base chart foreground and it's axis and
        // set it is completely transparent to mouse events
        final Axis<Long> xAxis = baseChart.getXAxis().castToAxis();
        final Axis<Double> yAxis = baseChart.getYAxis().castToAxis();
        final Node chartBackground = baseChart.toXYChart().lookup(".chart-plot-background");
        chartBackground.getParent()
                .getChildrenUnmodifiable()
                .stream()
                .filter(n -> n != chartBackground && n != xAxis && n != yAxis)
                .forEach(n -> n.setMouseTransparent(true));

        // change the cursor style to CROSSHAIR than the mouse
        // moves over the chartBackground
        chartBackground.setCursor(Cursor.CROSSHAIR);

        // active mouse handling for the chart
        zoomableBaseChart.setOnMoveExited(event -> chartBackground.getOnMouseEntered().handle(event));
        zoomableBaseChart.setOnMoveEntered(event -> chartBackground.getOnMouseExited().handle(event));
        zoomableBaseChart.setOnZoomChanged(zoomChangedHandler);

        bindCrossHairToChart(chartBackground);

        chartBackground.setOnMousePressed(this::enterYMovingMode);
        chartBackground.setOnMouseReleased(this::leaveYMovingMode);
        chartBackground.setOnMouseDragged(this::mouseDragged);
    }


    private void bindCrossHairToChart(Node chartBackground) {
        // set the own mouse event handler logic that will activate the
        // CrossHair than no PAN/ZOOM is active
        chartBackground.setOnMouseEntered(event -> {
            chartBackground.getOnMouseMoved().handle(event);
            if (zoomableBaseChart.getMouseMode() != ZOOM && zoomableBaseChart.getMouseMode() != PAN) {
                crossHair.addCrosshair();
            }
        });

        // deactivate CrossHair on leave
        chartBackground.setOnMouseExited(event -> crossHair.removeCrosshair());

        // update position of cross hair
        chartBackground.setOnMouseMoved(event -> {
            if (zoomableBaseChart.getMouseMode() == ZOOM || zoomableBaseChart.getMouseMode() == PAN) {
                // No Details in Panning or Zooming mode
                return;
            }

            double y = event.getY() + chartBackground.getLayoutY();
            double x = event.getX() + chartBackground.getLayoutX();

            crossHair.updateCrosshair(x, y);
            detailPopup.showOrHideDetails(event.isShiftDown(), event.getX(), event.getY(), x, y);
        });
    }


    private void mouseDragged(MouseEvent event) {
        if (movable != null) {
            movable.getOnMouseDragged().handle(event);
        }
    }

    /**
     * A lookup method that will resolve all series and the according chart that contain
     * the series that are very close to the mouse cursor.
     * The result will be a map that contains the matching charts as key it's matching series as value.
     * The method only includes charts (and it's series) that are specified via chartsToInclude parameter.
     *
     * @param event           the mouse event that provides cursor position and mouse button state
     * @param chartsToInclude a list of {@link ColoredChart} instances that should included into the lookup process
     * @return a map with chart and series that are close to the mouse cursor.
     */
    private Map<ColoredChart, List<XYChart.Series<Long, Double>>> getChartDataNearToCursor(
            MouseEvent event, List<ColoredChart> chartsToInclude) {

        Object doubleAsObject = baseChart.getXAxis().castToAxis().getValueForDisplay(event.getX());
        //noinspection ConstantConditions - we need to do this workaround to prevent class cast exception on previous step
        Double xValueDouble = (Double) doubleAsObject;

        Map<ColoredChart, List<XYChart.Series<Long, Double>>> resultMap = new HashMap<>();

        for (ColoredChart currentChart : chartsToInclude) {

            if (!currentChart.isVisible()) {
                continue;
            }

            for (XYChart.Series<Long, Double> series : currentChart.toXYChart().getData()) {
                double yValueForChart = getYValueForX(series, xValueDouble.longValue());

                if (Double.isNaN(yValueForChart)) {
                    continue;
                }

                Number yValueLower = Math.round(normalizeYValue(currentChart, event.getY() - SPACING_10));
                Number yValueUpper = Math.round(normalizeYValue(currentChart, event.getY() + SPACING_10));
                Number yValueUnderMouse = Math.round(currentChart.getYAxis().castToAxis().getValueForDisplay(event.getY()));

                // make series name bold when mouse is near given chart's line
                if (isMouseNearLine(yValueForChart, yValueUnderMouse,
                        Math.abs(yValueLower.doubleValue() - yValueUpper.doubleValue()))) {

                    if (!resultMap.containsKey(currentChart)) {
                        resultMap.put(currentChart, new ArrayList<>());
                    }

                    resultMap.get(currentChart).add(series);
                }
            }
        }

        return resultMap;
    }

    /**
     * next 3 methods for finding nearest Chart
     **/
    private double normalizeYValue(ColoredChart chart, double value) {
        Double val = chart.getYAxis().castToAxis().getValueForDisplay(value);
        return val == null ? 0 : val;
    }

    /**
     * Searches for the Y-value (metric value) with the closest timestamp
     * to the specified compared to the given xValued
     *
     * @param series the chart series that should used to find the metric value
     * @param xValue the reference x-value (timestamp) to find the closest matching metric value
     * @return the closest Y-value for the specified timestamp
     */
    private static double getYValueForX(XYChart.Series<Long, Double> series, long xValue) {
        List<XYChart.Data<Long, Double>> dataList = series.getData();

        if (dataList.isEmpty()) {
            return Double.NaN;
        }

        // binary search for the closed timestamp in the list compared to parameter xValue
        int idx = (dataList.size()) / 2;
        int jump = (idx + 1) / 2;
        while (jump > 0) {
            if (dataList.get(idx).getXValue() > xValue) {
                idx = idx - jump;
                idx = idx >= dataList.size() ? dataList.size() - 1 : idx;
            } else {
                idx = idx + jump;
                idx = idx >= dataList.size() ? dataList.size() - 1 : idx;
            }

            // explicitly handle last uneven jump value that can result in a to big step
            // away from an nearly exact matching x-value that couldn't corrected later
            jump = jump == 3 ? 2 : jump / 2;
        }

        // ensure bounds
        idx = idx >= dataList.size() ? dataList.size() - 1 : idx;
        idx = Math.max(idx, 0);

        // explicitly take a look to the neighbors because binary search can step away from closest value at the
        // last left/right jump.
        long xDiff = Math.abs(dataList.get(idx).getXValue() - xValue);
        if (dataList.size() > idx + 1 && Math.abs(dataList.get(idx + 1).getXValue() - xValue) < xDiff) {
            return dataList.get(idx + 1).getYValue();
        }

        if (idx > 0 && Math.abs(dataList.get(idx - 1).getXValue() - xValue) < xDiff) {
            return dataList.get(idx - 1).getYValue();
        }

        return dataList.get(idx).getYValue();
    }

    /**
     * Helper method to check if the mouse cursor is near series
     */
    private boolean isMouseNearLine(Number realYValue, Number yValueUnderMouse, Double tolerance) {
        return Math.abs(yValueUnderMouse.doubleValue() - realYValue.doubleValue()) < tolerance;
    }

    //----------------------------------------------------------------------------------------------------------------
    // methods that handle the movement of one or more charts along the y-axis
    //----------------------------------------------------------------------------------------------------------------

    /**
     * Releases the moving mode that will disable zooming functionality
     * and is used during the panning of charts along the y-axis.
     * The method will only have any effect if the moving mode was active.
     *
     * @param event the mouse event that provides cursor position and mouse button state
     */
    private void leaveYMovingMode(MouseEvent event) {

        if (movable == null) {
            return;
        }

        movable.getOnMouseReleased().handle(event);
        movable = null;
        zoomableBaseChart.setIsVerticalPanningAllowed(true);
        zoomableBaseChart.setIsVerticalScalingAllowed(true);
        zoomableBaseChart.setIsHorizontalPanningAllowed(true);
    }


    /**
     * If the user pressed ALT key the method resolves the background chart with
     * the series that are closed to mouse cursor and defined the according charts
     * to "movables" that can be dragged. All zooming functions on the charts will
     * deactivated.
     *
     * @param event the mouse event the contain the x/y coordinates of the cursor
     */
    private void enterYMovingMode(MouseEvent event) {
        if (!event.isAltDown()) {
            return;
        }

        Map<ColoredChart, List<XYChart.Series<Long, Double>>> chartData =
                getChartDataNearToCursor(event, zoomableStackedChart.getBackgroundCharts());

        if (chartData.size() == 1) {
            movable = chartData.entrySet().iterator().next().getKey().toXYChart();
        }

        if (movable != null) {
            zoomableBaseChart.setIsHorizontalPanningAllowed(false);
            zoomableBaseChart.setIsVerticalPanningAllowed(false);
            zoomableBaseChart.setIsVerticalScalingAllowed(false);
            movable.getOnMousePressed().handle(event);
        }
    }

    //==================================================================================================================
    // anonymous classes that represent parts of the chart
    //==================================================================================================================

    /**
     * Class that represents and manages the CrossHair that displays
     * two lines (X/Y) over the whole chart width/height with crossing point
     * at the mouse cursor.
     */
    private class CrossHair {

        /**
         * Margin of the cross hair
         */
        public static final int CROSSHAIR_MARGIN = 10;

        /**
         * Offset of the cross hair
         */
        public static final int CROSSHAIR_OFFSET = 5;

        private final Line xLine = new Line();
        private final Line yLine = new Line();

        /**
         * Constructs a cross hair with the given stroke width
         *
         * @param strokeWidth stroke width
         */
        public CrossHair(double strokeWidth) {
            yLine.setStroke(Color.STEELBLUE);
            xLine.setStroke(Color.STEELBLUE);
            yLine.setStrokeWidth(strokeWidth);
            xLine.setStrokeWidth(strokeWidth);
            xLine.setVisible(false);
            yLine.setVisible(false);
        }

        private void addCrosshair() {
            xLine.setVisible(true);
            yLine.setVisible(true);
            crossHairWindow.getChildren().removeAll(xLine, yLine);
            crossHairWindow.getChildren().addAll(xLine, yLine);
        }

        private void removeCrosshair() {
            xLine.setVisible(false);
            yLine.setVisible(false);
            crossHairWindow.getChildren().removeAll(xLine, yLine);
        }

        private void updateCrosshair(double x, double y) {
            xLine.setStartX(CROSSHAIR_MARGIN);
            xLine.setEndX(crossHairWindow.getWidth() - CROSSHAIR_MARGIN);
            xLine.setEndY(y + CROSSHAIR_OFFSET);
            xLine.setStartY(y + CROSSHAIR_OFFSET);

            yLine.setStartX(x + CROSSHAIR_OFFSET);
            yLine.setEndX(x + CROSSHAIR_OFFSET);
            yLine.setStartY(CROSSHAIR_MARGIN);
            yLine.setEndY(crossHairWindow.getHeight() - CROSSHAIR_MARGIN);
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    private static class DetailPopupPane extends AnchorPane {

        private DetailsPopup detailsPopup;

        private ZoomableStackedChart zoomableStackedChart;

        public DetailPopupPane(ZoomableStackedChart zoomableStackedChart) {
            this.zoomableStackedChart = zoomableStackedChart;
            this.detailsPopup = new DetailsPopup(zoomableStackedChart);
            this.setMouseTransparent(true);
        }

        /**
         * Helper method that makes the small popup window with a list of series data point values
         * visible or hides it depending on the given boolean flag.
         * If the popup should be shown the it will also layout in term of width/height.
         *
         * @param showDetails a boolean flag that controls if the popup should shown or not
         * @param mouseX      the mouse cursor x coordinate (relative to the plot area of the chart, with left border = 0)
         * @param mouseY      the mouse cursor y coordinate (relative to the plot area of the chart, with left border = 0)
         * @param x           the absolute x coordinate of the mouse cursor
         * @param y           the absolute y coordinate of the mouse cursor
         */
        public void showOrHideDetails(boolean showDetails, double mouseX, double mouseY, double x, double y) {
            AnchorPane.clearConstraints(detailsPopup);

            if (!showDetails) {
                detailsPopup.setVisible(false);
                return;
            }

            getChildren().remove(detailsPopup);
            int rowCount = detailsPopup.showChartDescription(mouseX, mouseY);
            if (rowCount <= 0) {
                return;
            }

            double popupRowsHeight = detailsPopup.getHeight();
            double popupRowsWidth = detailsPopup.getWidth();

            if (y + popupRowsHeight + SPACING_10 < zoomableStackedChart.getHeight()) {
                AnchorPane.setTopAnchor(detailsPopup, y + SPACING_10);
            } else {
                AnchorPane.setTopAnchor(detailsPopup, y - SPACING_10 - popupRowsHeight);
            }

            if (x + zoomableStackedChart.getWidth() / 2.0 + SPACING_10 < zoomableStackedChart.getWidth()) {
                AnchorPane.setLeftAnchor(detailsPopup, x + SPACING_10);
            } else {
                AnchorPane.setLeftAnchor(detailsPopup, x - SPACING_10 - popupRowsWidth);
            }

            getChildren().add(detailsPopup);
            detailsPopup.setVisible(true);

        }
    }

    /**
     * details popup
     */
    private static final class DetailsPopup extends VBox {

        /**
         * maximal width of series name in legend
         */
        public static final int SERIES_NAME_MAX_WIDTH = 140;

        private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.GERMAN);

        private static final String POPUP_STYLESHEET =
                DetailsPopup.class.getResource("ChartDetailOverlayStyle.css").toExternalForm();

        private ZoomableStackedChart zoomableStackedChart;

        public DetailsPopup(ZoomableStackedChart zoomableStackedChart) {
            this.zoomableStackedChart = zoomableStackedChart;
            getStyleClass().add("chartDetailPopup");
            getStylesheets().add(POPUP_STYLESHEET);
            setVisible(false);
        }

        /**
         * Show the detail popup at the given position
         *
         * @param mouseX the x coordinate of the mouse cursor relative to the plot area of the chart (left border = 0)
         * @param mouseY the y coordinate of the mouse cursor relative to the plot area of the chart (top border = 0)
         * @return size of popup entries
         */
        public int showChartDescription(double mouseX, double mouseY) {
            getChildren().clear();
            getChildren().addAll(createPopupRows(mouseX, mouseY));
            layout();
            return getChildren().size();
        }

        private List<Node> createPopupRows(double mouseX, double mouseY) {
            Object xValueAsObject = zoomableStackedChart.getBaseChart().getXAxis().castToAxis().getValueForDisplay(mouseX);

            //noinspection ConstantConditions
            Long xValueLong = ((Double) xValueAsObject).longValue();

            List<Node> popupRows = new ArrayList<>();

            Date date = new Date(xValueLong);
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss,SSS", Locale.GERMANY);
            sdf.setCalendar(cal);
            cal.setTime(date);

            popupRows.add(new HBox(new Label(sdf.format(date) + " Uhr")));

            VBox baseChartPopupRow = buildPopupRows(mouseY, xValueLong, zoomableStackedChart.getBaseChart());
            if (baseChartPopupRow != null) {
                popupRows.add(baseChartPopupRow);
            }

            for (ColoredChart backgroundChart : zoomableStackedChart.getBackgroundCharts()) {
                if (backgroundChart.isVisible()) {
                    VBox popupRow = buildPopupRows(mouseY, xValueLong, backgroundChart);
                    if (popupRow != null) {
                        popupRows.add(popupRow);
                    }
                }
            }

            if (popupRows.size() > 1) {
                popupRows.add(1, new Separator(Orientation.HORIZONTAL));
            }

            return popupRows;
        }

        /**
         * Creates a display row with label and metric value for each series that is close to the
         * cursor coordinates. All charts (base and background charts) will included.
         * The display rows will wrapped and returned in a VBox.
         *
         * @param mouseY     the y coordinate of the mouse cursor relative to the plot area of the chart (top border = 0)
         * @param xValueLong the timestamp of the x coordinate that marks the vertical area the algorithm looks for metric values
         * @param chart      the chart to investigate
         * @return all resolved data rows wrapped in a VBox
         */
        private VBox buildPopupRows(double mouseY, Long xValueLong, ColoredChart chart) {

            if (!chart.isVisible()) {
                return null;
            }

            XYChart<Long, Double> xyChart = chart.toXYChart();

            List<Node> elements = new ArrayList<>();
            for (XYChart.Series<Long, Double> series : xyChart.getData()) {

                if (!chart.isVisible(series)) {
                    continue;
                }

                Label seriesName = new Label(series.getName());
                seriesName.setTextFill(chart.getColoredSeries(series.getName()).get(0).getColor());
                seriesName.setMinWidth(SERIES_NAME_MAX_WIDTH);
                seriesName.setMaxWidth(SERIES_NAME_MAX_WIDTH);

                double yValueForChart = getYValueForX(series, xValueLong);
                if (Double.isNaN(yValueForChart)) {
                    return null;
                }

                double yValueLower = Math.round(normalizeYValue(xyChart, mouseY - SPACING_10));
                double yValueUpper = Math.round(normalizeYValue(xyChart, mouseY + SPACING_10));
                Number yValueUnderMouse = Math.round(xyChart.getYAxis().getValueForDisplay(mouseY));

                // make series name bold when mouse is near given chart's line
                if (isMouseNearLine(yValueForChart, yValueUnderMouse, Math.abs(yValueLower - yValueUpper))) {
                    String formattedValue = NUMBER_FORMAT.format(yValueForChart);
                    HBox hbox = new HBox(SPACING_10, seriesName, new Label(" | " + formattedValue));
                    elements.add(hbox);
                }

            }
            return (!elements.isEmpty()) ? new VBox(elements.toArray(new Node[]{})) : null;
        }

        private double normalizeYValue(XYChart<Long, Double> xyChart, double value) {
            Double val = xyChart.getYAxis().getValueForDisplay(value);
            return val == null ? 0 : val;
        }

        private boolean isMouseNearLine(Number realYValue, Number yValueUnderMouse, Double tolerance) {
            return Math.abs(yValueUnderMouse.doubleValue() - realYValue.doubleValue()) < tolerance;
        }
    }
}
