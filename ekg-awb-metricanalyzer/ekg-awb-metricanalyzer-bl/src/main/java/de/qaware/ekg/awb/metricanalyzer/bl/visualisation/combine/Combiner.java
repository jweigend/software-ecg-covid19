package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine;

import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Combiner implements TimeSeriesCombiner {

    private static final Logger LOGGER = EkgLogger.get();

    private static final long MIN_ACCEPTED_TIMESTAMP = 0;

    private static final long MAX_ACCEPTED_TIMESTAMP = 4102441200000L;

    protected static GranularityResult createGranularityForSec(List<TimeSeries> timeSeriesList) {
        return new GranularityResult(analyzeSeries(timeSeriesList).smallestTimestamp, 1000);
    }

    protected static GranularityResult createGranularityForMin(List<TimeSeries> timeSeriesList) {
        return new GranularityResult(analyzeSeries(timeSeriesList).smallestTimestamp, 60 * 1000);
    }

    protected static GranularityResult createGranularityForHours(List<TimeSeries> timeSeriesList) {
        return new GranularityResult(analyzeSeries(timeSeriesList).smallestTimestamp, 60 * 60 * 1000);
    }

    protected static GranularityResult createGranularityForDays(List<TimeSeries> timeSeriesList) {
        return new GranularityResult(analyzeSeries(timeSeriesList).smallestTimestamp, 24 * 60 * 60 * 1000);
    }

    protected static GranularityResult createGranularityForMonths(List<TimeSeries> timeSeriesList) {
        return new GranularityResult(analyzeSeries(timeSeriesList).smallestTimestamp, 2628000000L);
    }

    /**
     * Resolve the maximum sampling step (ms/s/minute/...) that is smaller than the closest
     * step between two values in each of the given time series.
     * The returned tick will be the multiple of 2.
     *
     * @param timeSeriesList the list of time series that should combined together to a single one
     * @return a container object that stores the analytic results about
     */
    protected static GranularityResult analyzeSeries(List<TimeSeries> timeSeriesList) {

        final Object lock = new Object();
        final AtomicLong smallestTimeInterval = new AtomicLong(Long.MAX_VALUE);
        final AtomicLong startTimestamp = new AtomicLong(Long.MAX_VALUE);

        timeSeriesList.parallelStream().forEach(series -> {

            if (series.getValues().isEmpty()) {
                return;
            }

            long start = series.getStartDate();
            long end = series.getEndDate();

            if (start <= MIN_ACCEPTED_TIMESTAMP || end < start || MAX_ACCEPTED_TIMESTAMP < end) {
                LOGGER.error("the time series isn't computable as result of illegal time range.");
                throw new IllegalArgumentException("the time series isn't computable as result of illegal time range.");
            }

            if (end == start) {

                if (series.getValues().size() == 1) {
                    return;
                } else {
                    String errorMsg = "the time series isn't computable because start of the time " +
                            "series == end but it has more than one value";

                    LOGGER.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            }

            long loopSmallestTimeInterval = Long.MAX_VALUE;
            long lastValue = 0;

            for (Value value : series.getValues()) {
                long stepWidth = value.getTimestamp() - lastValue;

                if (stepWidth < loopSmallestTimeInterval) {
                    loopSmallestTimeInterval = stepWidth;
                }

                lastValue = value.getTimestamp();
            }

            synchronized (lock) {

                if (start < startTimestamp.get()) {
                    startTimestamp.set(start);
                }

                if (loopSmallestTimeInterval < smallestTimeInterval.get()) {
                    smallestTimeInterval.set(loopSmallestTimeInterval);
                }
            }
        });

        if (smallestTimeInterval.get() == Long.MAX_VALUE) {
            return new GranularityResult(0, 1);
        }

        return new GranularityResult(startTimestamp.get(), smallestTimeInterval.get());
    }

    protected static class GranularityResult {

        long smallestTimestamp;
        long bucketSize;

        public GranularityResult(long smallestTimestamp, long bucketSize) {
            this.smallestTimestamp = smallestTimestamp;

            // bucket size of zero isn't allowed but it can occur if in the previous step
            // values of the same time merged to a series without using time slices.
            this.bucketSize = bucketSize == 0 ? 1 : bucketSize;
        }
    }

    protected static TimeSeries prepareSeries(Map<Long, Value> resultIndex, TimeSeries combinedSeries) {
        List<Long> seriesTimeKeys = new ArrayList<>(resultIndex.keySet());
        Collections.sort(seriesTimeKeys);

        List<Value> newTimeSeriesValues = new ArrayList<>();
        for (Long key : seriesTimeKeys) {
            newTimeSeriesValues.add(resultIndex.get(key));
        }

        combinedSeries.setSortedValues(newTimeSeriesValues);

        return combinedSeries;
    }
}
