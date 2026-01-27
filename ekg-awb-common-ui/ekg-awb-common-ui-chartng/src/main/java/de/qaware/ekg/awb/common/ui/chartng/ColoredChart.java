package de.qaware.ekg.awb.common.ui.chartng;

import de.qaware.ekg.awb.common.ui.chartng.axis.SpawningAxis;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * The ColorChart is a proxy around the JavaFX XYChart of any type (LineChart, AreaChart, ....).
 * The proxy add additional functionality for color handling and other chart/series states.
 */
public class ColoredChart implements Observable {

    private static final String CHART_STYLESHEET = "ZoomableStackChartStyle.css";

    private static final double AXIS_WIDTH = 60;

    /** -fx-stroke */
    public static final String FX_STROKE = "-fx-stroke: ";

    private Color chartColor;

    private double axisWidth;

    private boolean hideSeries = false;

    private String id;

    private boolean isComputed = false;

    private List<InvalidationListener> chartChangeListener = new ArrayList<>();

    private XYChart<Long, Double> wrappedChart;

    private Map<String, List<ColoredSeries>> coloredSeriesMap = new HashMap<>();

    /**
     * Helps to avoid that the listeners are notified multiple times, even though further changes are applied.
     */
    private final AtomicBoolean changeListenerTriggered = new AtomicBoolean(false);

    //=================================================================================================================

    /**
     * Constructs a ColoredChart instance that will decorate the given JavaFX chart
     * and use a couple of default values for styling and meta data.
     *
     * @param chart the JavaFX XYChart that will decorate by this ColoredChart instance
     */
    public ColoredChart(XYChart<Long, Double> chart) {
        this(chart, AXIS_WIDTH, Color.BLACK, UUID.randomUUID().toString());
    }

    /**
     * Constructs a ColoredChart instance that will decorate the given JavaFX chart
     * by using the specified style data.
     *
     * @param chart the JavaFX XYChart that will decorate by this ColoredChart instance
     * @param axisWidth the axis width
     * @param initialColor the initial color of the chart and it's series
     * @param id the unique id of the chart
     */
    public ColoredChart(XYChart<Long, Double> chart, double axisWidth, Color initialColor, String id) {
        this.wrappedChart = chart;
        this.axisWidth = axisWidth;

        if (this.wrappedChart.getStylesheets().isEmpty()) {
            this.wrappedChart.getStylesheets().add(getClass().getResource(CHART_STYLESHEET).toExternalForm());
        }

        this.wrappedChart.setLegendVisible(false);
        this.wrappedChart.setAnimated(false);

        this.wrappedChart.getXAxis().setPrefWidth(axisWidth);
        this.wrappedChart.getYAxis().setMaxWidth(axisWidth);
        this.wrappedChart.getData().addListener((InvalidationListener) observable -> {
            if (wrappedChart.getData().isEmpty()) {
                coloredSeriesMap.clear();
            }
        });

        setChartColor(initialColor);

        this.id = id;
    }

    //=================================================================================================================
    // decorator API
    //=================================================================================================================


    public Map<String, List<ColoredSeries>> getColoredSeriesMap() {
        return coloredSeriesMap;
    }

    public void setColoredSeriesMap(Map<String, List<ColoredSeries>> coloredSeriesMap) {
        this.coloredSeriesMap = coloredSeriesMap;
    }

    public boolean isComputed() {
        return isComputed;
    }

    public boolean isNotComputed() {
        return !isComputed;
    }

    public void setComputed(boolean proportionalComputed) {
        isComputed = proportionalComputed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEmpty() {
        return hideSeries || getAllSeries().isEmpty();
    }

    public double getAxisWidth() {
        return axisWidth;
    }

    /**
     * Returns the Visibility of the whole chart
     *
     * @return the Visibility of the whole chart
     */
    public boolean isVisible() {
        return this.wrappedChart.isVisible();
    }

    /**
     * Sets the Visibility of the chart
     *
     * @param visible new Visibility of the chart
     */
    public void setVisible(boolean visible) {
        this.wrappedChart.setVisible(visible);
        notifyChangeListener();
    }

    /**
     * Returns the Visibility of a single series
     *
     * @param series series
     * @return the Visibility of a single series
     */
    public boolean isVisible(XYChart.Series series) {
        return series.getNode() == null || series.getNode().isVisible();
    }

    public void hideSeries() {
        hideSeries = true;
        wrappedChart.getData().forEach(series -> {
            if (series.getNode() != null) {
                series.getNode().setVisible(false);
            } else {
                series.getData().forEach(d -> d.getNode().setVisible(false));
            }
        });

        coloredSeriesMap.clear();
        notifyChangeListener();
    }

    /**
     * Sets the Visibility of a single series
     *
     * @param series  series
     * @param visible new Visibility
     */
    public void setVisible(XYChart.Series<Long, Double> series, boolean visible) {
        if (series.getNode() != null) {
            series.getNode().setVisible(visible);
        } else {
            series.getData().forEach(d -> d.getNode().setVisible(visible));
        }
        notifyChangeListener();
    }

    /**
     * Adds an additional series to the underlying chart.
     * The series will styled with individual color
     *
     * @param series the series that should add to the chart
     */
    public synchronized void addSeries(XYChart.Series<Long, Double> series) {

        Color seriesColor = ChartColorStyle.getColor(series);

        if (!coloredSeriesMap.containsKey(series.getName())) {
            coloredSeriesMap.put(series.getName(), new ArrayList<>());
        }

        wrappedChart.getData().add(series);
        coloredSeriesMap.get(series.getName()).add(new ColoredSeries(series, seriesColor));
        hideSeries = false;
        notifyChangeListener();
    }


    /**
     * Returns the data of the managed chart as ObservableList
     * of {@link XYChart.Series} instances.
     *
     * @return the list of series instances
     */
    public List<XYChart.Series<Long, Double>> getAllSeries() {

        if (hideSeries) {
            return new ArrayList<>();
        }

        return wrappedChart.getData();
    }

    /**
     * Returns the data points of all series data points (point with X/Y value)
     * that the series of the decorated chart holds as data.
     * Doesn't serve the according series itself.
     *
     * @return the data points of all chart series data
     */
    public List<List<XYChart.Data<Long, Double>>> getAllSeriesData() {
        return wrappedChart.getData().stream()
                .map(XYChart.Series::getData)
                .collect(Collectors.toList());
    }

    /**
     * Returns the x axis of the managed chart
     *
     * @return the x axis of the managed chart
     */
    public SpawningAxis<Long> getXAxis() {
        //noinspection unchecked
        return (SpawningAxis) wrappedChart.getXAxis();
    }

    /**
     * Returns the y axis of the managed chart
     *
     * @return the y axis of the managed chart
     */
    public SpawningAxis<Double> getYAxis() {
        //noinspection unchecked
        return (SpawningAxis) wrappedChart.getYAxis();
    }

    public ObservableList<String> getStylesheets() {
        return wrappedChart.getStylesheets();
    }

    public boolean isLegendVisible() {
        return wrappedChart.isLegendVisible();
    }

    public void setLegendVisible(boolean isVisible) {
        wrappedChart.setLegendVisible(isVisible);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        chartChangeListener.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        chartChangeListener.remove(listener);
    }


    /**
     * Returns the color of the chart
     *
     * @return the color of the chart
     */
    public Color getColor() {
        return chartColor;
    }


    /**
     * style this chart
     *
     * @param chartColor  color
     */
    public void setChartColor(Color chartColor) {
        this.chartColor = chartColor;
        notifyChangeListener();
    }

    /**
     * Set the specified color to the series with the specified name.
     * For this the method lookup for the right series in all that are
     * a part of the chart model.
     *
     * @param seriesName the name of the series the caller wants tto change the color
     * @param color the new color of that should set to the series
     */
    public void setSeriesColor(String seriesName, Color color) {

        List<XYChart.Series<Long, Double>> lookupSeries = new ArrayList<>();

        for (XYChart.Series<Long, Double> series : wrappedChart.getData()) {
            if (seriesName.equals(series.getName())) {
                lookupSeries.add(series);
            }
        }

        if (lookupSeries.isEmpty()) {
            throw new IllegalArgumentException("Unable to find the series '" + seriesName + "' in the chart '"
                    + getId() + "'");
        }

        coloredSeriesMap.get(seriesName).forEach(series -> series.setColor(color));

        notifyChangeListener();
    }

    public ChartType getChartType() {
        return ChartType.valueOf(wrappedChart.getClass());
    }


    /**
     * Resets the data of the chart by deleting it's series,
     * color mappings and if requested the axis data.
     *
     * @param resetAxis boolean flag that controls if the y-axis will reset (blank label and default ranging) or not
     */
    public void clear(boolean resetAxis) {
        coloredSeriesMap.clear();
        isComputed = false;
        wrappedChart.getData().clear();

        if (resetAxis) {
            wrappedChart.getYAxis().setLabel("");
            wrappedChart.getYAxis().setAutoRanging(true);
            wrappedChart.getXAxis().setAutoRanging(true);
        }

        notifyChangeListener();
    }

    public XYChart<Long, Double> toXYChart() {
        return wrappedChart;
    }

    public String getLegendLabel() {
        return wrappedChart.getYAxis().getLabel();
    }

    public void setLegendLabel(String legendLabel) {
        wrappedChart.getYAxis().setLabel(legendLabel);
    }

    public List<ColoredSeries> getAllColoredSeries() {
        return coloredSeriesMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<ColoredSeries> getColoredSeries(String seriesName) {
        return coloredSeriesMap.get(seriesName);
    }


    //=================================================================================================================
    // internal logic
    //=================================================================================================================

    /**
     * Iterates over all InvalidationListener and notify it
     * that this chart has changed (at least on property of it)
     * <p>
     * The listeners are informed in a new run of the FX-Thread, so we can collect changes, and trigger them only once.
     */
    private void notifyChangeListener() {
        if (!changeListenerTriggered.getAndSet(true)) {
            Platform.runLater(() -> {
                changeListenerTriggered.set(false);
                for (InvalidationListener listener : chartChangeListener) {
                    listener.invalidated(this);
                }
            });
        }
    }


    //=================================================================================================================
    // inner classes
    //=================================================================================================================

    /**
     * A thin decorator class for a XYChart.Series class that will add some
     * abilities especially in easy handling of styling.
     */
    public static class ColoredSeries {

        private static PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

        private Color color;

        private boolean isVisible = true;

        private String seriesName;

        private boolean isSelected = false;

        private XYChart.Series<Long, Double> series;

        /**
         * Constructs a new ColoredSeries instance that will display the name
         * and state of the given series.
         *
         * @param series the series that should decorated by this class
         * @param color the color of the series
         */
        public ColoredSeries(XYChart.Series<Long, Double> series, Color color) {
            this.seriesName = series.getName();
            this.series = series;
            setColor(color);
        }


        private Color preInitColor = null;
        private boolean preInitVisibility;
        private boolean preInitSelectionState;

        public ColoredSeries(XYChart.Series<Long, Double> series, Color color, boolean isVisible, boolean isSelected) {
            this.seriesName = series.getName();
            this.series = series;
            this.preInitColor = color;
            this.preInitVisibility = isVisible;
            this.preInitSelectionState = isSelected;
        }


        public void init() {
            if (preInitColor != null) {
                setColor(preInitColor);
                setVisible(preInitVisibility);
                setSelected(preInitSelectionState);
            }
        }

        public XYChart.Series<Long, Double> getFxSeries() {
            return series;
        }

        public boolean isVisible() {
            if (series.getNode() == null) {
                return isVisible;
            }

            return series.getNode().isVisible();
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;

            if (series.getNode() == null) {
                return;
            }

            SeriesLineAreaContainer seriesLineAreaContainer = getStyleableSeriesElements(series);
            seriesLineAreaContainer.seriesMainElements.forEach(node -> {
                node.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, isSelected);
            });

            seriesLineAreaContainer.seriesFillArea.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, isSelected);
        }

        public boolean isSelected() {
            return isSelected;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            SeriesLineAreaContainer seriesLineAreaContainer = getStyleableSeriesElements(series);
            seriesLineAreaContainer.seriesMainElements.forEach(node -> {
                node.setStyle(FX_STROKE + ChartColorStyle.toRGBCode(color));
            });

            seriesLineAreaContainer.seriesFillArea.setStyle("-fx-fill: " + ChartColorStyle.toRGBCode(color, 0.3));
        }

        public String getSeriesName() {
            return seriesName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ColoredSeries that = (ColoredSeries) o;
            return Objects.equals(color, that.color) &&
                    Objects.equals(seriesName, that.seriesName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, seriesName);
        }


        public void setVisible(Boolean isSeriesVisible) {
            if (series.getNode() != null) {
                series.getNode().setVisible(isSeriesVisible);
            } else {
                series.getData().forEach(d -> d.getNode().setVisible(isSeriesVisible));
            }

            isVisible = isSeriesVisible;
        }

        private SeriesLineAreaContainer getStyleableSeriesElements(XYChart.Series<Long, Double> series) {

            SeriesLineAreaContainer seriesLineAreaContainer = new SeriesLineAreaContainer();

            Node curr = series.getNode();

            if (curr == null) {
                // scatter line
                series.getData().forEach(dataPoint -> {
                    seriesLineAreaContainer.seriesMainElements.add(dataPoint.getNode().lookup(".chart-symbol"));
                });

            } else {
                Node areaLine = curr.lookup(".chart-series-area-line");
                if (areaLine != null) {
                    seriesLineAreaContainer.seriesMainElements.add(areaLine);
                    seriesLineAreaContainer.seriesFillArea = curr.lookup(".chart-series-area-fill");
                } else {
                    seriesLineAreaContainer.seriesMainElements.add(curr);
                }
            }

            return seriesLineAreaContainer;
        }

    }

    private static class SeriesLineAreaContainer {

        private List<Node> seriesMainElements = new ArrayList<>();

        private Node seriesFillArea = new Group();
    }
}
