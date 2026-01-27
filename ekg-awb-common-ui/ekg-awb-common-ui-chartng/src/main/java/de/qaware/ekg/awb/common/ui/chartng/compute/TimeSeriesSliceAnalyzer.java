package de.qaware.ekg.awb.common.ui.chartng.compute;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Utility class that provided time slicing functionality
 * of JavaFX chart series data to enable the analysis of series
 * to each other.
 */
public class TimeSeriesSliceAnalyzer {

    /**
     * an array of tick units that will represent the sampling
     * rate functions like proportional combine uses for it's work.
     *
     * Have to be the n-th of 10
     */
    private static final int[] TICKS_MILLIS = new int[]{
            10,            // 10ms
            100,           // 100ms
            1000,          // 1sec
            10_000,        // 10sec
            100_000,       // 100sec or 1.5min
            1_000_000,     // 16min
            10_000_000,    // 2,7h
            100_000_000,   // 1,15 days
    };

    /**
     * Assign the values of each data point of given time series list to the time slices stored
     * in the given {@link SliceValueContainer} container.
     * If more than one data points of a single time series belongs to the same time slice the
     * values will sum and append as total result into the container for the specific time series.
     *
     * Note: The time series will separated using the key "series name". If multiple series use the same name,
     * it's data will combined in the container.
     *
     * @param container the {@link SliceValueContainer} that will hold the complete time series data
     * @param series a list of time series it's data points will will time-sliced, sum and persisted
     */
    public static void assignToFilledSlices(SliceValueContainer container, List<XYChart.Series<Long, Double>> series) {

        // iterate over each time series given by the caller
        for (XYChart.Series<Long, Double> timeSeries : series) {

            // construct a container that will used to store and sum data point values for various
            // data points of a single time series that are place in multiple similar time slices
            SliceDataSet dataSet = new SliceDataSet(timeSeries.getName(), timeSeries.getData().size(), 1.0f);

            // iterate over the data point of the time series
            for (XYChart.Data<Long, Double> data : timeSeries.getData()) {

                // calculate the key of the time slice
                long key = (data.getXValue() / container.timeUnitDivisor) * container.timeUnitDivisor;

                // check if the current data point is placed inside of any time slice defined by the caller
                if (container.referenceDataMap.containsKey(key)) {

                    // all data points that are inside a specific slice will sum to calculate the absolute value
                    // of the time series for the specific slice
                    dataSet.put(key, dataSet.getOrDefault(key, 0d) + data.getYValue());
                    continue;
                }

                // if the data point doesn't match we give them one more chance if interpolation is active
                // in this case we use the series data to calculate a new base line reference
                if (container.interpolate) {
                    dataSet.put(key, dataSet.getOrDefault(key, 0d) + data.getYValue());
                    Double interpolatedRefValue = container.interpolatedTimeSlices.getOrDefault(key, 0d);
                    container.interpolatedTimeSlices.put(key, interpolatedRefValue + data.getYValue());
                }
            }

            if (!dataSet.isEmpty()) {
                container.add(dataSet);
            }
        }
    }


    /**
     * Calculates and returns a divisor that represents a time unit in milliseconds that will
     * the n-th of 10ms. The divisor value is smaller or equal than the shortest time interval
     * between to points in the given time series data.
     *
     * @param dataList a list Chart data points that represents the time series points
     * @return the time unit equivalent divisor
     */
    public static <Y extends Number> int analyzeSeries(List<XYChart.Data<Long, Y>> dataList) {

        // base time for granularity calculation
        long previewTimeStamp = 0;

        // the smallest granularity value that marks the smallest interval between two ticks on the X-Axis (time)
        // find in the series data. The value starting the the biggest value set to the next smaller one that is find
        // at iterating all values.
        long smallestGranularity = Long.MAX_VALUE;

        for (XYChart.Data<Long, Y> seriesDataItem : dataList) {
            long currentTimeRange = seriesDataItem.getXValue() - previewTimeStamp;

            if (currentTimeRange < smallestGranularity) {
                smallestGranularity = currentTimeRange;
            }

            previewTimeStamp = seriesDataItem.getXValue();
        }

        // after resolving the smallest interval (in milliseconds), find out which will be a
        // adequate divisor used to divide values into the right sized slices
        int divisor = TICKS_MILLIS[0];
        for (int tickInMilli : TICKS_MILLIS) {
            if (smallestGranularity < tickInMilli) {
                break;
            }

            divisor = tickInMilli;
        }

        return divisor;
    }

    public static class SliceValueContainer extends ArrayList<SliceDataSet> {

        private Map<Long, Double> interpolatedTimeSlices = new HashMap<>();

        private Map<Long, Double> referenceDataMap;

        private int timeUnitDivisor;

        private boolean interpolate;

        public SliceValueContainer(Map<Long, Double> referenceMap, int timeUnitDivisor, boolean interpolate) {
            this.referenceDataMap = referenceMap;
            this.timeUnitDivisor = timeUnitDivisor;
            this.interpolate = interpolate;
        }

        @Override
        public boolean add(SliceDataSet value) {
            super.add(value);
            value.parent = this;
            return true;
        }

        private Double getSliceValue(Long timeSlot) {
            return referenceDataMap.getOrDefault(timeSlot, interpolatedTimeSlices.get(timeSlot));
        }
    }

    public static class SliceDataSet extends HashMap<Long, Double> {

        private SliceValueContainer parent;

        private String name;

        public SliceDataSet(String name, int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public <Y extends Number> ObservableList<XYChart.Data<Long, Y>> dataAsObservableList(
                BiFunction<Double, Double, Double> transformer) {

            ObservableList<XYChart.Data<Long, Y>> resultList = FXCollections.observableArrayList();

            for (Map.Entry<Long, Double> dataPoint : this.entrySet()) {
                Double referenceSliceValue = parent.getSliceValue(dataPoint.getKey());
                Double computedValue = transformer.apply(referenceSliceValue, dataPoint.getValue());
                //noinspection unchecked
                resultList.add(new XYChart.Data(dataPoint.getKey(), computedValue));
            }

            return resultList;
        }
    }
}
