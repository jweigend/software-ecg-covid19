package de.qaware.ekg.awb.common.ui.chartng.zoom;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/**
 * Decorator for charts to prepare chart for zooming, panning and sizing
 *
 * @param <X> x axis type
 * @param <Y> y axis type
 */
public class Zoomable<X extends Long, Y extends Number> implements IZoomable<X, Y> {

    private static final MouseMode DEFAULT_MOUSE_MODE = null;

    private final SimpleObjectProperty<MouseMode> mouseModeProperty = new SimpleObjectProperty<>();

    private final SimpleBooleanProperty isZoomingEnabled = new SimpleBooleanProperty(true);

    private final ObjectProperty<EventHandler<MouseEvent>> moveEnteredHandlerProperties = new SimpleObjectProperty<>();

    // Handler for Moving enter and exit
    private final ObjectProperty<EventHandler<MouseEvent>> moveExitedHandlerProperties = new SimpleObjectProperty<>();

    // Handler for Moving enter and exit
    private final ObjectProperty<EventHandler<IZoomable.ZoomEvent>> zoomChangedHandlerProperties = new SimpleObjectProperty<>();

    private final ValueAxis<Long> xAxis;
    private final ValueAxis<Y> yAxis;

    private Group chartGroup;
    private Rectangle zoomRectangle;

    private final XYChart<Long, Y> chart;

    private boolean isAxisValuesVisible;
    private int tempMinorTickCount;

    // temporary variables used with zooming
    private double initialMouseSceneX;
    private double initialMouseSceneY;

    private double lastMouseDragX;
    private double lastMouseDragY;

    // temporary variables used for scaling
    private double initialYLowerBounds;
    private double initialYUpperBounds;
    private double scrollUpperBound = Double.NaN;
    private double scrollLowerBound = Double.NaN;

    // starting bound before first zooming
    private double startingXLowerBounds;
    private double startingXUpperBounds;
    private double startingYLowerBounds;
    private double startingYUpperBounds;

    private Cursor previousMouseCursor;

    private boolean isShowingOnlyYPositiveValues;
    private boolean isVerticalPanningAllowed;
    private boolean isHorizontalPanningAllowed;
    private boolean isVerticalScalingAllowed;

    //=================================================================================================================
    // constructors
    //=================================================================================================================

    /**
     * Constructs a decorator for the given chart
     *
     * @param chart chart to decorate
     */
    public Zoomable(XYChart<Long, Y> chart) {
        this.chart = chart;
        isAxisValuesVisible = true;
        isShowingOnlyYPositiveValues = false;
        isVerticalPanningAllowed = true;
        isHorizontalPanningAllowed = true;
        isVerticalScalingAllowed = true;

        xAxis = (ValueAxis<Long>) chart.getXAxis();
        yAxis = (ValueAxis<Y>) chart.getYAxis();

        setupZoom();

        mouseModeProperty.set(DEFAULT_MOUSE_MODE);
    }

    /**
     * Constructs a decorator for the given chart and copy the zoom bounds from the old zoomable
     *
     * @param chart       chart to decorate
     * @param oldZoomable the old zoomable
     */
    public Zoomable(XYChart<Long, Y> chart, IZoomable oldZoomable) {
        this(chart);

        if (oldZoomable == null) {
            return;
        }

        double[] bounds = oldZoomable.getStartingBounds();
        startingXLowerBounds = bounds[0];
        startingXUpperBounds = bounds[1];
        startingYLowerBounds = bounds[2];
        startingYUpperBounds = bounds[3];
    }


    //=================================================================================================================
    // IZoomable API
    //=================================================================================================================

    @Override
    public final void setOnZoomChanged(EventHandler<IZoomable.ZoomEvent> value) {
        zoomChangedHandlerProperties.set(value);
    }

    @Override
    public final void setOnMoveEntered(EventHandler<MouseEvent> value) {
        moveEnteredHandlerProperties.set(value);
    }

    @Override
    public final void setOnMoveExited(EventHandler<MouseEvent> value) {
        moveExitedHandlerProperties.set(value);
    }

    @Override
    public void setPrefWidth(double size) {
        chart.setPrefWidth(size);
    }

    @Override
    public void setPrefHeight(double size) {
        chart.setPrefHeight(size);
    }

    @Override
    public void setMaxWidth(double size) {
        chart.setMaxWidth(size);
    }

    @Override
    public void setMaxHeight(double size) {
        chart.setMaxHeight(size);
    }

    @Override
    public ObservableList<Series<Long, Y>> getData() {
        return chart.getData();
    }

    @Override
    public void setData(ObservableList<Series<Long, Y>> data) {
        setAutoRanging(true);
        chart.setData(data);
    }

    @Override
    public void setTitle(String title) {
        chart.setTitle(title);
    }

    //=================================================================================================================
    // API of the Zoomable class itself
    //=================================================================================================================

    /**
     * Returns all series of the chart
     *
     * @return all series of the chart
     */
    public ObservableList<Node> getNodeRepresentation() {
        zoomRectangle.setManaged(false);
        return chartGroup.getChildren();
    }

    /**
     * Returns the decorated (JavaFX) chart wrapped
     * by this Zoomable instance
     *
     * @return the decorated chart
     */
    public XYChart<Long, Y> getChart() {
        return chart;
    }

    /**
     * Resets the chart to the previously set starting bounds
     */
    public void zoomToFit() {

        setAutoRanging(false);

        if (!(startingXLowerBounds == 0 && startingYLowerBounds == 0 && startingXUpperBounds == 0
                && startingYUpperBounds == 0)) {

            if (!xAxis.lowerBoundProperty().isBound()) {
                xAxis.setLowerBound(startingXLowerBounds);
            }

            if (!xAxis.upperBoundProperty().isBound()) {
                xAxis.setUpperBound(startingXUpperBounds);
            }


            yAxis.setUpperBound(startingYUpperBounds);
            yAxis.setLowerBound(startingYLowerBounds);

            yAxis.layout();
            xAxis.layout();

            fireZoomChanged(new ZoomEvent(xAxis.getLowerBound(), xAxis.getUpperBound(), yAxis.getLowerBound(), yAxis.getUpperBound(), false));
        }
    }

    /**
     * Returns a mouse mode depending on the key pressed
     *
     * @param mouseEvent event with key pressed information
     * @return mouse mode (SCALE, PAN, ZOOM)
     */
    public MouseMode getMouseMode(MouseEvent mouseEvent) {

        if (mouseEvent.isAltDown()) {
            return mouseEvent.isControlDown() ? MouseMode.SCALE : MouseMode.PAN;
        } else {
            return MouseMode.ZOOM;
        }
    }

    /**
     * Returns the mouse mode property
     *
     * @return mouse mode property
     */
    public SimpleObjectProperty<MouseMode> mouseModeProperty() {
        return mouseModeProperty;
    }

    /**
     * Sets whether the axis is visible
     *
     * @param isShowing the axis is visible
     */
    public void setAxisValuesShowing(boolean isShowing) {
        isAxisValuesVisible = isShowing;

        xAxis.setTickMarkVisible(isAxisValuesVisible);
        yAxis.setTickMarkVisible(isAxisValuesVisible);

        xAxis.setTickLabelsVisible(isAxisValuesVisible);
        yAxis.setTickLabelsVisible(isAxisValuesVisible);

        if (!isAxisValuesVisible) {
            if (xAxis.getMinorTickCount() != 0) {
                tempMinorTickCount = xAxis.getMinorTickCount();
            }

            yAxis.setMinorTickCount(0);
            xAxis.setMinorTickCount(0);

        } else {
            if (xAxis.getMinorTickCount() == 0 && tempMinorTickCount != 0) {
                xAxis.setMinorTickCount(tempMinorTickCount);
                yAxis.setMinorTickCount(tempMinorTickCount);
            }
        }
    }

    /**
     * Returns whether axis is visible
     *
     * @return true if axis is visible else false
     */
    public boolean isAxisValuesShowing() {
        return isAxisValuesVisible;
    }

    private void setAutoRanging(boolean isAutoRanging) {
        xAxis.setAutoRanging(isAutoRanging);
        yAxis.setAutoRanging(isAutoRanging);
    }


    /**
     * Sets a cursor
     *
     * @param cursor new cursor
     */
    public void setChartCursor(Cursor cursor) {
        // Do nothing
    }

    /**
     * Sets only y axis positive values are visible
     *
     * @param onlyYPositive only y axis positive values are visible
     */
    public void setIsShowingOnlyYPositiveValues(boolean onlyYPositive) {
        isShowingOnlyYPositiveValues = onlyYPositive;
    }

    /**
     * Sets whether vertical panning is allowed
     *
     * @param isAllowed vertical panning is allowed
     */
    public void setIsVerticalPanningAllowed(boolean isAllowed) {
        isVerticalPanningAllowed = isAllowed;
    }

    /**
     * Sets whether horizontal panning is allowed
     *
     * @param isAllowed horizontal panning is allowed
     */
    public void setIsHorizontalPanningAllowed(boolean isAllowed) {
        isHorizontalPanningAllowed = isAllowed;
    }

    /**
     * Sets whether vertical scaling is allowed
     *
     * @param isAllowed vertical scaling is allowed
     */
    public void setIsVerticalScalingAllowed(boolean isAllowed) {
        isVerticalScalingAllowed = isAllowed;
    }

    /**
     * Returns the zoom rectangle
     *
     * @return the zoom rectangle
     */
    public Rectangle getZoomRectangle() {
        return zoomRectangle;
    }

    /**
     * Returns the initial bounds of the chart
     *
     * @return the initial bounds of the chart
     */
    @Override
    public double[] getStartingBounds() {
        return new double[]{startingXLowerBounds, startingXUpperBounds, startingYLowerBounds, startingYUpperBounds};
    }

    /**
     * Sets the starting Bound for the zoom
     * @param bounds bounds
     */
    @Override
    public void setStartingBounds(double[] bounds) {
        startingXLowerBounds = bounds[0];
        startingXUpperBounds = bounds[1];
        startingYLowerBounds = bounds[2];
        startingYUpperBounds = bounds[3];
    }


    /**
     * Enables/Disables the zooming
     *
     * @param isEnabled true/false
     */
    public void setZoomingEnabled(boolean isEnabled) {
        isZoomingEnabled.set(isEnabled);
    }

    /**
     * Returns whether zooming is enabled
     *
     * @return true, if zooming is enabled else false
     */
    public boolean isZoomingEnabled() {
        return isZoomingEnabled.get();
    }

    /**
     * Returns the zooming enabled property
     *
     * @return the zooming enabled property
     */
    public SimpleBooleanProperty isZoomingEnabledProperty() {
        return isZoomingEnabled;
    }

    /**
     * Returns the mouse mode
     *
     * @return PAN, ZOOM or SCALE
     */
    public MouseMode getMouseMode() {
        return mouseModeProperty.get();
    }

    public void reset() {
        setStartingBounds(new double[]{0, 0, 0, 0});
    }

    //=================================================================================================================
    // internal logic
    //=================================================================================================================

    /**
     * Creates a new styled rectangle that can used
     * to visualize the zoom area spanned via mouse.
     *
     * @return a styled {@link Rectangle} instance
     */
    private Rectangle createZoomRectangle() {

        final Rectangle zoomArea = new Rectangle();

        // border of the mouse spanned rectangle
        zoomArea.setStroke(Color.BLUE);
        zoomArea.setStrokeWidth(1);

        // styling of the mouse spanned rectangle
        Color color = Color.LIGHTBLUE;
        Color zoomAreaFill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5);
        zoomArea.setFill(zoomAreaFill);

        return zoomArea;
    }

    private void setupZoom() {
        zoomRectangle = createZoomRectangle();
        zoomRectangle.setVisible(false);

        chartGroup = new Group();
        chartGroup.getChildren().addAll(zoomRectangle, chart);

        chart.setOnMousePressed(this::mousePressed);
        chart.setOnMouseDragged(this::mouseDragged);
        chart.setOnMouseReleased(this::mouseReleased);

        chart.getData().addListener((InvalidationListener) c -> {
            scrollLowerBound = yAxis.getLowerBound();
            scrollUpperBound = yAxis.getUpperBound();
        });

        chart.setOnScroll(e -> {
            setAutoRanging(false);

            if (isVerticalScalingAllowed) {

                if (Double.isNaN(scrollUpperBound)) {
                    scrollUpperBound = yAxis.getUpperBound();
                }

                if (Double.isNaN(scrollLowerBound)) {
                    scrollLowerBound = yAxis.getLowerBound();
                }

                double range = Math.abs(yAxis.getUpperBound() - scrollLowerBound);
                double delta = range / 250 * -1;

                double newYUpperBound = yAxis.getUpperBound() + (e.getDeltaY() * delta);

                setYAxisBounds(initialYLowerBounds, newYUpperBound);

                yAxis.requestAxisLayout();
                yAxis.layout();
            }
        });
    }

    private void mouseReleased(MouseEvent mouseEvent) {
        if (mouseModeProperty.get() != MouseMode.ZOOM) {
            setMouseMode(null);
            fireMoveExited(mouseEvent);
            return;
        }

        setMouseMode(null);

        zoomRectangle.setVisible(false);
        setChartCursor(previousMouseCursor);

        double newMouseX = mouseEvent.getSceneX();
        double newMouseY = mouseEvent.getSceneY();

        if (newMouseX < initialMouseSceneX && newMouseY < initialMouseSceneY) {
            // zoom out
            zoomOut();

            fireZoomChanged(new ZoomEvent(xAxis.getLowerBound(), xAxis.getUpperBound(), yAxis.getLowerBound(), yAxis.getUpperBound(), false));

        } else if (newMouseX > initialMouseSceneX && newMouseY > initialMouseSceneY) {
            // zoom in
            zoomIn(newMouseX, newMouseY);

            fireZoomChanged(new ZoomEvent(xAxis.getLowerBound(), xAxis.getUpperBound(), yAxis.getLowerBound(), yAxis.getUpperBound(), true));

        }

        fireMoveExited(mouseEvent);
    }

    private void mouseDragged(MouseEvent mouseEvent) {
        double mouseSceneX = mouseEvent.getSceneX();
        double mouseSceneY = mouseEvent.getSceneY();
        double dragX = mouseSceneX - lastMouseDragX;
        double dragY = mouseSceneY - lastMouseDragY;

        lastMouseDragX = mouseSceneX;
        lastMouseDragY = mouseSceneY;

        if (mouseModeProperty.get() == MouseMode.ZOOM) {
            zoomRectangle.toFront();
            zoomRectangle.setWidth(mouseSceneX - initialMouseSceneX);
            zoomRectangle.setHeight(mouseSceneY - initialMouseSceneY);
            zoomRectangle.setVisible(true);

        } else if (mouseModeProperty.get() == MouseMode.PAN) {
            setAutoRanging(false);

            Dimension2D chartDrag = sceneToChartDistance(dragX, dragY);

            if (isHorizontalPanningAllowed) {
                xAxis.setLowerBound(xAxis.getLowerBound() - chartDrag.getWidth());
                xAxis.setUpperBound(xAxis.getUpperBound() - chartDrag.getWidth());
            }
            if (isVerticalPanningAllowed) {
                double newYLowerBound = yAxis.getLowerBound() + chartDrag.getHeight();
                double newYUpperBound = yAxis.getUpperBound() + chartDrag.getHeight();

                setYAxisBounds(newYLowerBound, newYUpperBound);
            }
        } else if (mouseModeProperty.get() == MouseMode.SCALE) {
            setAutoRanging(false);

            dragX = mouseSceneX - initialMouseSceneX;
            dragY = mouseSceneY - initialMouseSceneY;

            Dimension2D chartDrag = sceneToChartDistance(dragX, dragY);

            if (isVerticalScalingAllowed) {
                double lowerFactor = Math.abs(initialYLowerBounds / (initialYUpperBounds - initialYLowerBounds));
                double upperFactor = 1.0 - lowerFactor;

                double newYLowerBound = initialYLowerBounds - chartDrag.getHeight() * lowerFactor;
                double newYUpperBound = initialYUpperBounds + chartDrag.getHeight() * upperFactor;

                setYAxisBounds(newYLowerBound, newYUpperBound);

                yAxis.requestAxisLayout();
                yAxis.layout();
            }
        }
    }


    public void keyPressed(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.DOWN && keyEvent.isControlDown()) {
            System.out.println("test");
        }
    }

    /**
     * Set all members that hold the coordinates and cursor type lock in the position
     * where a mouse even occur in the chart. It also change the cursor typ based on
     * the action type.
     *
     * @param mouseEvent the occurred mouse event
     */
    private void mousePressed(MouseEvent mouseEvent) {
        previousMouseCursor = chart.getCursor();

        double initialMouseX = mouseEvent.getX();
        double initialMouseY = mouseEvent.getY();

        initialMouseSceneX = mouseEvent.getSceneX();
        initialMouseSceneY = mouseEvent.getSceneY();

        lastMouseDragX = initialMouseSceneX;
        lastMouseDragY = initialMouseSceneY;

        setMouseMode(getMouseMode(mouseEvent));

        // safe the starting bounds before zooming, panning and scaling
        setStartingBounds();

        fireMoveEntered(mouseEvent);

        // mouse was used to zoom in or out
        if (mouseModeProperty.get() == MouseMode.ZOOM) {
            setChartCursor(Cursor.CROSSHAIR);
            zoomRectangle.setX(initialMouseX);
            zoomRectangle.setY(initialMouseY);

        // mouse was used to pan the whole chart inside it's container
        } else if (mouseModeProperty.get() == MouseMode.PAN) {
            setChartCursor(Cursor.CLOSED_HAND);

        // mouse was used scale the metric axis (y-axis)
        } else if (mouseModeProperty.get() == MouseMode.SCALE) {
            initialYLowerBounds = yAxis.getLowerBound();
            initialYUpperBounds = yAxis.getUpperBound();
            setChartCursor(Cursor.MOVE);
        }
    }

    private void zoomIn(double newMouseX, double newMouseY) {
        setAutoRanging(false);

        double[] newLower = sceneToChartValues(initialMouseSceneX, newMouseY);
        double[] newUpper = sceneToChartValues(newMouseX, initialMouseSceneY);

        xAxis.setLowerBound(newLower[0]);
        xAxis.setUpperBound(newUpper[0]);

        setYAxisBounds(newLower[1], newUpper[1]);

        yAxis.layout();
    }

    private void setStartingBounds() {
        if (startingXLowerBounds == 0
                && startingYLowerBounds == 0
                && startingXUpperBounds == 0
                && startingYUpperBounds == 0) {

            startingXLowerBounds = xAxis.getLowerBound();
            startingXUpperBounds = xAxis.getUpperBound();

            startingYLowerBounds = yAxis.getLowerBound();
            startingYUpperBounds = yAxis.getUpperBound();
        }
    }

    private void zoomOut() {
        setAutoRanging(false);

        if (!(startingXLowerBounds == 0
                && startingYLowerBounds == 0
                && startingXUpperBounds == 0
                && startingYUpperBounds == 0)) {

            xAxis.setLowerBound(startingXLowerBounds);
            xAxis.setUpperBound(startingXUpperBounds);

            yAxis.setLowerBound(startingYLowerBounds);
            yAxis.setUpperBound(startingYUpperBounds);

            yAxis.layout();
        }
    }

    private void setYAxisBounds(double newYLowerBound, double newYUpperBound) {
        if (!isShowingOnlyYPositiveValues) {
            if (!yAxis.lowerBoundProperty().isBound()) {
                yAxis.setLowerBound(newYLowerBound);
            }

            if (!yAxis.upperBoundProperty().isBound()) {
                yAxis.setUpperBound(newYUpperBound);
            }
        } else {
            if (!yAxis.lowerBoundProperty().isBound()) {
                yAxis.setLowerBound(newYLowerBound < 0 ? 0 : newYLowerBound);
            }

            if (!yAxis.upperBoundProperty().isBound()) {
                yAxis.setUpperBound(newYUpperBound < 0 ? 0 : newYUpperBound);
            }
        }
    }


    private void fireMoveExited(MouseEvent event) {
        MouseEvent newMouseEvent = event.copyFor(event.getTarget(), event.getTarget());
        if (moveExitedHandlerProperties.get() != null) {
            moveExitedHandlerProperties.get().handle(newMouseEvent);
        }
    }

    private void fireMoveEntered(MouseEvent event) {
        MouseEvent newMouseEvent = event.copyFor(event.getTarget(), event.getTarget());
        if (moveEnteredHandlerProperties.get() != null) {
            moveEnteredHandlerProperties.get().handle(newMouseEvent);
        }
    }

    private void fireZoomChanged(IZoomable.ZoomEvent event) {
        if (zoomChangedHandlerProperties.get() != null) {
            zoomChangedHandlerProperties.get().handle(event);
        }
    }

    private double[] sceneToChartValues(double sceneX, double sceneY) {
        double xDataLength = xAxis.getUpperBound() - xAxis.getLowerBound();
        double yDataLength = yAxis.getUpperBound() - yAxis.getLowerBound();
        double xPixelLength = xAxis.getWidth();
        double yPixelLength = yAxis.getHeight();

        Point2D leftBottomChartPos = xAxis.localToScene(0, 0);
        double xMinPixelCoord = leftBottomChartPos.getX();
        double yMinPixelCoord = leftBottomChartPos.getY();

        double chartXCoord = xAxis.getLowerBound() + ((sceneX - xMinPixelCoord) * xDataLength / xPixelLength);
        double chartYcoord = yAxis.getLowerBound() + ((yMinPixelCoord - sceneY) * yDataLength / yPixelLength);
        return new double[]{chartXCoord, chartYcoord};
    }

    private Dimension2D sceneToChartDistance(double sceneX, double sceneY) {
        double xDataLength = xAxis.getUpperBound() - xAxis.getLowerBound();
        double yDataLength = yAxis.getUpperBound() - yAxis.getLowerBound();
        double xPixelLength = xAxis.getWidth();
        double yPixelLength = yAxis.getHeight();

        double chartXDistance = sceneX * xDataLength / xPixelLength;
        double chartYDistance = sceneY * yDataLength / yPixelLength;
        return new Dimension2D(chartXDistance, chartYDistance);
    }

    /**
     * Sets the mouse mode
     *
     * @param mode new mouse mode
     */
    private void setMouseMode(MouseMode mode) {
        if (mode == mouseModeProperty.get()) {
            return;
        }

        this.mouseModeProperty.set(mode);
        if (mouseModeProperty.get() == MouseMode.ZOOM) {
            setZoomingEnabled(true);
            setChartCursor(Cursor.DEFAULT);
        } else {
            setZoomingEnabled(false);
            if (mouseModeProperty.get() == MouseMode.PAN) {
                setChartCursor(Cursor.OPEN_HAND);
            }
        }
    }



}