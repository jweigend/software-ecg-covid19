package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.sdk.datamodel.Value;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/**
 * Base class of all TimeSeriesSmoother implementations that will do the
 * common stuff like input validation and resolving the divisor for grouping.
 */
public abstract class AbstractTimeSeriesSmoother implements TimeSeriesSmoother {

    /**
     * This static structure will used to calculate the year and month based on a given timestamp.
     * Using an array structure with search access is more than twice faster than using the Calendar API
     * to resolve the information.
     */
    private static final int YEAR_OFFSET = 1970;
    private static final long[] CALENDAR_YEAR_BUCKETS = new long[100];
    private static final long[][] CALENDAR_MONTH_BUCKETS = new long[CALENDAR_YEAR_BUCKETS.length][12];
    private static final long[][] MID_MONTH_TIMESTAMP_INDEX = new long[CALENDAR_YEAR_BUCKETS.length][12];

    /**
     * The amount of value points that make sense to display
     * in the chart and will used as base to calculate the timestamp step-width.
     */
    private static final long REASONABLE_TICKS_IN_CHART = 300;

    /**
     * The granularity of timestamp buckets used to
     * visualisation the smoothing of time series
     */
    private final SeriesSmoothingGranularity smoothingGranularity;

    /*
     * Build up the time search structure
     */
    static {
        initializeCalendarBuckets();
    }


    //================================================================================================================
    //  public API of the (Abstract)TimeSeriesSmoother
    //================================================================================================================

    /**
     * Constructs a new instance of AbstractTimeSeriesSmoother which will use
     * the granularity of timestamp buckets for computing the series smoothing
     *
     * @param smoothingGranularity an SeriesSmoothingGranularity enum that specifies the granularity
     */
    protected AbstractTimeSeriesSmoother(SeriesSmoothingGranularity smoothingGranularity) {
        this.smoothingGranularity = smoothingGranularity;
    }

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing.TimeSeriesSmoother#computeSmoothing(java.util.List)
     */
    @Override
    public List<Value> computeSmoothing(List<Value> originalValueList) {

        if (originalValueList == null || originalValueList.isEmpty()) {
            return originalValueList;
        }

        Map<Long, List<Value>> bucketValueList;

        if (smoothingGranularity == SeriesSmoothingGranularity.CALENDAR_MONTH) {
            bucketValueList = transformToCalendarAwareBuckets(originalValueList);

        } else if (smoothingGranularity == SeriesSmoothingGranularity.OFF) {
            bucketValueList = new HashMap<>(originalValueList.size(), 1);

            for (Value value : originalValueList) {
                bucketValueList.put(value.getTimestamp(), List.of(value));
            }

        } else {
            bucketValueList = transformToGenericTimeBuckets(originalValueList);
        }

        return computeSeriesValues(bucketValueList);
    }



    //================================================================================================================
    //  internal API used or implemented by sub-classes to realize the smoothing functionality
    //================================================================================================================

    /**
     * Proceed the smoothing values stored in the given time buckets
     *
     * The implementation expects an unordered or ordered map with pre-assigned time series
     * to the values that are defined per map entry. The returned list will contain the smoothed
     * result represented as on Value per time bucked sorted in chronological order.
     *
     * @param bucketValueMap a map which contains an entry for each time bucket and a list of values that are assigned to it
     * @return the smoothed value list of the time series
     */
    protected abstract List<Value> computeSeriesValues(Map<Long, List<Value>> bucketValueMap);

    /**
     * Transform the Map with time series values back
     * to a sorted list of {@link Value} instances.
     *
     * @param resultIndex an map with time group keys as keys and Value instances as value
     * @return the transformed map
     */
    protected static List<Value> prepareSeries(Map<Long, Value> resultIndex) {
        List<Long> valueKeys = new ArrayList<>(resultIndex.keySet());
        Collections.sort(valueKeys);

        List<Value> newValueSeries = new ArrayList<>();
        for (Long key : valueKeys) {
            newValueSeries.add(resultIndex.get(key));
        }

        return newValueSeries;
    }

    //================================================================================================================
    //  private helpers to resolve the correct divisor for deriving classes
    //================================================================================================================

    /**
     * Partitions the given list of Value instances to various time buckets
     * returned as map.
     *
     * The underlying algorithm uses fix bucket length's with a static divisor
     * that is resolved for the bind granularity to divide the data into the buckets.
     * The returned map use timestamps that define the half time for each bucket interval.
     *
     * @param valueList the value list of the time series to smooth
     * @return a map which contains an entry for each time bucket and a list of values that are assigned to it
     */
    private Map<Long, List<Value>> transformToGenericTimeBuckets(List<Value> valueList) {

        long divisor = resolveDivisor(smoothingGranularity, valueList);

        long smallestTimestamp = valueList.get(0).getTimestamp();
        long largestTimestamp = valueList.get(valueList.size() - 1).getTimestamp();
        long sliceSizeHalf = divisor / 2;

        int amountOfBuckets = (int)((largestTimestamp - smallestTimestamp) / divisor) + 1;
        Map<Long, List<Value>> bucketValueList = new HashMap<>(amountOfBuckets, 1.0f);

        for (Value value : valueList) {

            int sliceIndex = (int)((value.getTimestamp() - smallestTimestamp) / divisor);
            long sliceTime = smallestTimestamp + sliceSizeHalf + (sliceIndex * divisor);

            if (!bucketValueList.containsKey(sliceTime)) {
                bucketValueList.put(sliceTime, new ArrayList<>());
            }

            bucketValueList.get(sliceTime).add(value);
        }

        return bucketValueList;
    }


    /**
     * Partitions the given list of Value instances to various time buckets
     * returned as map.
     *
     * Each bucket is a calendar month represented by a timestamp points to the middle of the month.
     * But all values that belongs to the relating month (also after the mid of month) are assigned
     * to the same bucket.
     *
     * @param valueList the value list of the time series to smooth
     * @return a map which contains an entry for each time bucket and a list of values that are assigned to it
     */
    private Map<Long, List<Value>> transformToCalendarAwareBuckets(List<Value> valueList) {

        Map<Long, List<Value>> bucketValueMap = new HashMap<>(10, 1.0f);

        int lastYear = -1;
        int lastMonth = -1;

        for (Value value : valueList) {

            long timestamp = value.getTimestamp();

            int indexYear = -1;
            int indexMonth = -1;

            if (lastYear >= 0) {
                indexYear = predictYear(CALENDAR_YEAR_BUCKETS, lastYear, timestamp);
            }

            if (indexYear >= 0) {
                indexMonth = predictMonth(CALENDAR_MONTH_BUCKETS, lastYear, lastMonth, timestamp);
            }

            if (indexMonth < 0) {
                indexYear = binarySearch(CALENDAR_YEAR_BUCKETS, timestamp);
                indexMonth = binarySearch(CALENDAR_MONTH_BUCKETS[indexYear], timestamp);
            }

            lastYear = indexYear;
            lastMonth = indexMonth;

            long midMonthTimestamp = MID_MONTH_TIMESTAMP_INDEX[indexYear][indexMonth];

            List<Value> bucketValueList = bucketValueMap.computeIfAbsent(midMonthTimestamp, k -> new ArrayList<>());
            bucketValueList.add(value);
        }

        return bucketValueMap;
    }

    /**
     * Tries to predict the correct year index of 2-dimension array 'MID_MONTH_TIMESTAMP_INDEX'
     * that stores the bucket the timestamp belongs to based on lastYear index.
     *
     * The method is used as performance optimisation because in the most cases the preview used
     * bucket is equal to the current needed one.
     *
     * @param calendarYearBuckets the array with yearly calender buckets
     * @param lastYear the index of the last year that was proceed in previous calls
     * @param timestamp the timestamp search the bucket for
     * @return the index of the bucket or -1 if prediction fails
     */
    @SuppressWarnings("SameParameterValue")
    private int predictYear(long[] calendarYearBuckets, int lastYear, long timestamp) {

        if (calendarYearBuckets[lastYear] <= timestamp) {

            if (calendarYearBuckets.length > lastYear + 1) {
                return calendarYearBuckets[lastYear +1] > timestamp ? lastYear : -1;

            } else {
                return lastYear;
            }
        }

        return -1;
    }

    /**
     * Tries to predict the correct month index of 2-dimension array 'MID_MONTH_TIMESTAMP_INDEX'
     * that stores the bucket the timestamp belongs to based on lastMonth index.
     *
     * The method is used as performance optimisation because in the most cases the preview used
     * bucket is equal to the current needed one.
     *
     * @param calendarMonthBuckets the array with yearly calender buckets
     * @param lastYear the index of the last year that was proceed in previous calls
     * @param lastMonth the index of the last month that was proceed in previous calls
     * @param timestamp the timestamp search the bucket for
     * @return the index of the bucket or -1 if prediction fails
     */
    @SuppressWarnings("SameParameterValue")
    private int predictMonth(long[][] calendarMonthBuckets, int lastYear, int lastMonth, long timestamp) {

        if (calendarMonthBuckets[lastYear][lastMonth] < timestamp) {

            if (calendarMonthBuckets[lastYear].length > lastMonth + 1) {
                return calendarMonthBuckets[lastYear][lastMonth +1] > timestamp ? lastMonth : -1;

            } else if (calendarMonthBuckets.length > lastYear + 1) {
                return  calendarMonthBuckets[lastYear + 1][0] > timestamp ? lastMonth : -1;
            }

            return lastMonth;
        }

        return -1;
    }


    /**
     * Calculates the divisor that matches to the specified smoothing granularity and will
     * used to segment the timestamp into buckets.
     *
     * @param smoothingGranularity the granularity used to chose / calculate the best divisor
     * @param originalValueList the whole value list of the time series to investigate if granularity=AUTO chosen
     * @return the divisor that matches to the granularity
     */
    private long resolveDivisor(SeriesSmoothingGranularity smoothingGranularity, List<Value> originalValueList) {

        long div;
        switch (smoothingGranularity) {
            case AUTO:
                div = calculateDivisor(originalValueList);
                break;
            case HALF_YEAR:
                div = 15_778_462_998L;
                break;
            case QUARTER:
                div = 7_889_231_499L;
                break;
            case MONTH:
                div = 2_628_000_000L;
                break;
            case WEEK:
                div = 604_800_000L;
                break;
            case DAY:
                div = 86_400_000L;
                break;
            case HOUR:
                div = 3600_000L;
                break;
            case MINUTE:
                div = 60_000L;
                break;
            case SECONDS:
                div = 1000L;
                break;
            default:
                throw new IllegalArgumentException("Unsupported smoothing granularity specified!");
        }

        return div;
    }

    /**
     * Auto calculates the best matching divisor for the given value list
     * by make assumptions about the granularity that fits into a chart
     *
     * @param originalValueList the values list to visualisation the divisor for
     * @return the calculated divisor
     */
    private long calculateDivisor(List<Value> originalValueList) {

        long start = originalValueList.get(0).getTimestamp();
        long end = originalValueList.get(originalValueList.size() - 1).getTimestamp();

        if (start > end) {
            throw new IllegalArgumentException("the value list of the series has an illegal time range.");
        }

        // 100ms will be our smallest bucket range
        return Math.max(100, ((end - start) / REASONABLE_TICKS_IN_CHART));
    }

    private static int binarySearch(long[] a, long key) {
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid];

            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }

        return high;
    }

    private static void initializeCalendarBuckets() {

        Calendar calendar = GregorianCalendar.from(Instant.ofEpochMilli(0).atZone(ZoneId.of("UTC")));

        // fill two arrays (year/year + month) the the timestamps 1970 - 2070 (at 01.01 00:00:00) and the same for each month
        for (int yearWithoutOffset = 0; yearWithoutOffset < CALENDAR_MONTH_BUCKETS.length; yearWithoutOffset++) {

            calendar.set(Calendar.YEAR, yearWithoutOffset + YEAR_OFFSET);

            CALENDAR_YEAR_BUCKETS[yearWithoutOffset] = calendar.getTimeInMillis();

            for (int month = 0; month < 12; month++) {
                calendar.set(Calendar.MONTH, month);

                CALENDAR_MONTH_BUCKETS[yearWithoutOffset][month] = calendar.getTimeInMillis();
            }

            calendar.set(Calendar.MONTH, 0);
        }

        // index the timestamp of the mid of each month we have in our search structure
        for (int yearWithoutOffset = 0; yearWithoutOffset < CALENDAR_MONTH_BUCKETS.length; yearWithoutOffset++) {

            for (int month = 0; month < 12; month++) {

                long mid;

                if (month == 11) {

                    if (yearWithoutOffset + 1 == CALENDAR_YEAR_BUCKETS.length) {
                        mid = CALENDAR_MONTH_BUCKETS[yearWithoutOffset][month] + (15 * 86_400_000L);
                    } else {
                        mid = (CALENDAR_MONTH_BUCKETS[yearWithoutOffset][month] +
                                CALENDAR_MONTH_BUCKETS[yearWithoutOffset + 1][0]) / 2;
                    }

                } else {
                    mid = (CALENDAR_MONTH_BUCKETS[yearWithoutOffset][month] +
                            CALENDAR_MONTH_BUCKETS[yearWithoutOffset][month + 1]) / 2;
                }

                calendar.setTimeInMillis(mid);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                MID_MONTH_TIMESTAMP_INDEX[yearWithoutOffset][month] = calendar.getTimeInMillis();
            }
        }
    }
}
