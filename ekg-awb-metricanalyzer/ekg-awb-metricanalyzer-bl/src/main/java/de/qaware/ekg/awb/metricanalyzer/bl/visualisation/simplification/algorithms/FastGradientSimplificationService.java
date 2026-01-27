package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms;

import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.SimplificationService;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.slf4j.Logger;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of SimplificationService that is based on the following algorithm:
 *
 * Step 1: calculating the gradient of each point in the graph (Value objects in the counter series)
 * Step 2: calculating the tolerance threshold that will use to filter the points
 * Step 3: filter als points that have an gradient == 0 or < the tolerance threshold
 *
 * The result will have less or the exact amount of points that is defined by the threshold.
 *
 * The implementation is fast processing and may use code constructs that are less readable.
 * It optimized for multi-core submultipliers systems also prefer primitives and array usage with offset to
 * increase the performance.
 */
@Singleton
public class FastGradientSimplificationService implements SimplificationService {

    /**
     * Logger for error output
     */
    private static final Logger LOGGER = EkgLogger.get();

    /**
     * The amount of CPUs that can be used for optimize the parallel algorithm
     */
    private static final int AMOUNT_CPUs = Runtime.getRuntime().availableProcessors();

    /**
     * The minimum amount of counter values of a chunk that will delegate to a separate thread
     * (to prevent multithreading with to small workload)
     */
    private static final int MIN_CHUNK_SIZE = 20_000;

    /**
     * our own thread pool used for parallel processing of the counter simplification
     */
    private ExecutorService executor = Executors.newFixedThreadPool(AMOUNT_CPUs);

    /**
     * Simplify a list of counters to less or the exact amount of counter values defined by the
     * threshold. If more than one counter is given, the threshold will limit the overall counter
     * values. For that the threshold value will divided in equal chunks for each counter.
     *
     * @param timeSeriesList  the time series with counter values
     * @param threshold threshold for the number of points for all counters together; 0 indicates,
     *                  that there should no simplification started
     * @return simplified counters
     */
    @Override
    public synchronized List<TimeSeries> simplify(List<TimeSeries> timeSeriesList, final int threshold) {

        long beforeSimplifying = timeSeriesList.stream().mapToLong(c -> c.getValues().size()).sum();

        // if zero threshold defined or to less counter we can break early
        if (beforeSimplifying <= threshold || threshold < 0) {
            return timeSeriesList;
        }

        // decision between parallelization on the list of time series vs. on chunks of data points
        // for one specific time series (don't do both, it will result in to many tasks)
        final int amountChunkSplits;
        if (timeSeriesList.size() > 20 * AMOUNT_CPUs) {
            amountChunkSplits = 1;
        } else {
            amountChunkSplits = AMOUNT_CPUs;
        }

        AtomicBoolean thresholdWarMsgIsLogged = new AtomicBoolean(false);

        // spawn a new thread for each counter in the query result
        timeSeriesList.parallelStream().forEach(timeSeries -> {

            try {
                List<Value> values = timeSeries.getValues();

                int amountValues = timeSeries.getValues().size();
                int chunkSize = Math.max(MIN_CHUNK_SIZE, amountValues / amountChunkSplits);
                int amountOfChunks = (int) Math.max(Math.ceil((double) amountValues / (double) chunkSize), 1);
                int segmentThreshold = threshold / timeSeriesList.size() / amountOfChunks;

                if (segmentThreshold == 0) {
                    if (thresholdWarMsgIsLogged.compareAndSet(false, true)) {
                        LOGGER.warn("The defined threshold the limit the total number of data point for all chart series is " +
                                "smaller than the amount of fetched chart series. This will result in unexpected graphs. " +
                                "Use much higher thresholds to handle this.");
                    }

                    return;
                }

                final AtomicInteger activeTasksCount = new AtomicInteger(0);

                Value[][] segmentResult = new Value[amountOfChunks][0];

                // divide the counter values into equal chunks and create a task with will filter the values in the chunk
                runSimplifierTasks(values, chunkSize, amountOfChunks, segmentThreshold, activeTasksCount, segmentResult);

                // wait until all tasks are finished
                while (activeTasksCount.get() > 0) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        new ArrayList<>();
                    }
                }

                // replace the counter values with the simplified ones
                values.clear();
                for (Value[] segmentValues : segmentResult) {
                    values.addAll(Arrays.asList(segmentValues));
                }

            } catch (Exception e) {
                LOGGER.error("Error during calculation and delegation of simplifier tasks", e);
                throw new IllegalStateException(e);
            }
        });

        return timeSeriesList;
    }

    /**
     * Create SimplifierTask for chunks of the value list (min size defined by constant MIN_CHUNK_SIZE)
     * that will reduce that amount of values by eliminating the values with no or to less gradient difference to
     * the compared previous value.
     *
     * The result will written to the segmentResult. The first dimension of the array represents the chunk
     * of the time series in the correct chronological order
     *
     * @param values the complete values of the counter time series
     * @param chunkSize the amount of values proceed in a dedicated task (thread)
     * @param amountOfChunks the expected amount of chunks required to proceed the complete time series
     * @param segmentThreshold the maximum of values that are allowed in each chunk of the time series
     * @param activeTasks an global counter to share the state of #active tasks
     * @param segmentResult a multi-dimension result array that will fulfilled by SimplifierTasks
     */
    private void runSimplifierTasks(List<Value> values, int chunkSize, int amountOfChunks,
                                    int segmentThreshold, AtomicInteger activeTasks, Value[][] segmentResult) {

        for (int chunkIndex = 0; chunkIndex < amountOfChunks; chunkIndex ++) {

            int index = chunkIndex;
            int start = chunkIndex * chunkSize;
            int end = start + chunkSize >= values.size() ? values.size() : start + chunkSize;

            activeTasks.incrementAndGet();
            executor.submit(() -> {
                try {
                    segmentResult[index] = filterValues(values, start, end, segmentThreshold);
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    activeTasks.getAndDecrement();
                }
            });
        }
    }

    /**
     * Filter/simplification function that will work in the three steps:
     *
     * Step 1: calculating the gradient of each point in the graph (Value objects in the counter series)
     * Step 2: calculating the tolerance threshold that will use to filter the points
     * Step 3: filter als points that have an gradient == 0 or < the tolerance threshold
     *
     * @param values the complete values of the counter time series
     * @param start the index of the first value that belongs to the chunk (start offset)
     * @param end the index of the last value that belongs to the chunk
     * @param segmentThreshold the maximum of values that are allowed in each chunk of the time series
     *
     * @return a value array with the filtered/simplified values
     */
    private static Value[] filterValues(List<Value> values, int start, int end, int segmentThreshold) {

        List<ValueGradientHolder> diffGradientValues = new ArrayList<>();

        Value lastElementFiltered = null;

        // Step 1: normalize data. In this step the algorithm reduce multiple data points to a single one if
        //         the multiple data points have the same timestamp. This isn't common but already concurred in real-life.
        //         In this case the algorithm search for the value with the maximum difference to the value of
        //         the previous timestamp.
        double valueAtPreviousTimestamp = 0;
        Value valueWithHighestDiff = null;
        double lastLoopDiff = Double.NaN;
        long lastLoopTimestamp = -1;
        List<Value> normalizedValues = new ArrayList<>();

        for (int index = start + 1; index < end; index++) {

            Value value = values.get(index);

            if (value.getTimestamp() != lastLoopTimestamp) {
                lastLoopTimestamp = value.getTimestamp();
                lastLoopDiff = Double.NaN;

                if (valueWithHighestDiff == null) {
                    valueWithHighestDiff = value;
                } else {
                    normalizedValues.add(valueWithHighestDiff);
                    valueAtPreviousTimestamp = valueWithHighestDiff.getValue();
                }
            }

            double diff = Math.abs(valueAtPreviousTimestamp - value.getValue());
            if (Double.isNaN(lastLoopDiff) || lastLoopDiff < diff || (lastLoopDiff == diff && valueWithHighestDiff.getValue() < value.getValue())) {
                lastLoopDiff = diff;
                valueWithHighestDiff = value;
            }
        }

        if (valueWithHighestDiff != null && !normalizedValues.get(normalizedValues.size() - 1).equals(valueWithHighestDiff)) {
            normalizedValues.add(valueWithHighestDiff);
        }

        // Step 2: calculating the gradient of each value. Values with zero gradient will filtered immediately
        for (int index = 1; index < normalizedValues.size(); index++) {

            double calculatedGradient = Math.abs(calculateLinearGradient(normalizedValues.get(index - 1), normalizedValues.get(index)));

            if (calculatedGradient > 0.0) {
                if (lastElementFiltered != null) {
                    diffGradientValues.add(new ValueGradientHolder(lastElementFiltered, calculatedGradient));
                    lastElementFiltered = null;
                }

                diffGradientValues.add(new ValueGradientHolder(normalizedValues.get(index), calculatedGradient));
            } else {
                lastElementFiltered = normalizedValues.get(index);
            }
        }

        if (diffGradientValues.isEmpty()) {
            return new Value[0];
        }

        List<Value> resultList = new ArrayList<>(segmentThreshold);
        lastElementFiltered = null;

        // if we don't reach the segment threshold after removing zero gradient values, we can skip step 2
        if (diffGradientValues.size() < segmentThreshold) {

            for (ValueGradientHolder holder : diffGradientValues) {
                resultList.add(holder.value);
            }

        } else {
            // Step 3: calculating the tolerance threshold that will use to filter the points
            List<ValueGradientHolder> sortedGradientValues = new ArrayList<>(diffGradientValues);
            //noinspection unchecked
            Collections.sort(sortedGradientValues);

            int nthValueIndex;

            if (segmentThreshold == 0) {
                return new Value[]{sortedGradientValues.get(sortedGradientValues.size() - 1).value};
            } else {
                nthValueIndex = sortedGradientValues.size() - segmentThreshold;
            }

            double toleranceThreshold = sortedGradientValues.get(nthValueIndex).gradient;

            // Step 4: filter all points that have the gradient < tolerance threshold
            for (ValueGradientHolder holder : diffGradientValues) {
                if (holder.gradient > toleranceThreshold) {
                    if (lastElementFiltered != null) {
                        resultList.add(lastElementFiltered);
                        lastElementFiltered = null;
                    }

                    resultList.add(holder.value);
                } else {
                    lastElementFiltered = holder.value;
                }
            }
        }

        return resultList.toArray(new Value[0]);
    }


    /**
     * Calculates the linear gradient of between two values of the counter time series
     *
     * @param previousValue the preview value of the counter time series
     * @param currentValue the current value of the counter time series
     * @return a double that represents the gradient
     */
    private static double calculateLinearGradient(Value previousValue, Value currentValue) {
        double dX = currentValue.getTimestamp() - previousValue.getTimestamp();
        return dX <= 0.0 ? Double.MAX_VALUE : (currentValue.getValue() - previousValue.getValue()) / (dX);
    }


    /**
     * Helper data structure to sort the values
     */
    private static class ValueGradientHolder implements Comparable {

        private double gradient;

        private Value value;

        public ValueGradientHolder(Value value, double gradient) {
            this.gradient = gradient;
            this.value = value;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(gradient, ((ValueGradientHolder) o).gradient);
        }
    }
}
