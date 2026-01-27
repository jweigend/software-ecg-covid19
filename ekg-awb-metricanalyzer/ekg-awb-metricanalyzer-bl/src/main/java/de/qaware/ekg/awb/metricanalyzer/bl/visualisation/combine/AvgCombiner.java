package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combiner implementation that will calculate the average of the values of multiple
 * time series at each timestamp they are overlapping.
 * To analytics of the overlapping will be done by creating time buckets.
 */
public class AvgCombiner extends Combiner {

    private SeriesCombineMode combineMode;

    public AvgCombiner(SeriesCombineMode combineMode) {
        this.combineMode = combineMode;
    }

    @Override
    public TimeSeries combine(String newSeriesName, List<TimeSeries> timeSeriesList) {

        Map<Long, Value> resultIndex = new HashMap<>();

        // look for oldest timestamp, the granularity (as divisor parameter) and offset
        GranularityResult analyticsResult;

        if (combineMode == SeriesCombineMode.AVG_EXACT) {
            analyticsResult = analyzeSeries(timeSeriesList);
        } else {
            switch (combineMode) {
                case AVG_SEC:
                    analyticsResult = createGranularityForSec(timeSeriesList);
                    break;
                case AVG_MIN:
                    analyticsResult = createGranularityForMin(timeSeriesList);
                    break;
                case AVG_HOUR:
                    analyticsResult = createGranularityForHours(timeSeriesList);
                    break;
                case AVG_DAY:
                    analyticsResult = createGranularityForDays(timeSeriesList);
                    break;
                case AVG_MONTH:
                    analyticsResult = createGranularityForMonths(timeSeriesList);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected combiner mode " + combineMode.getName());
            }
        }

        TimeSeries combinedSeries = new TimeSeries(newSeriesName);

        // proceed indexing and reduce of values
        for (TimeSeries timeSeries : timeSeriesList) {

            // merge meta data (differ filter dimensions will become a '*')
            combinedSeries.mergeMetaData(timeSeries);

            for (Value value : timeSeries.getValues()) {

                // the shorten key used to group values
                long key = value.getTimestamp() - (value.getTimestamp() % analyticsResult.bucketSize);

                // insert new value or merge the current one
                if (!resultIndex.containsKey(key)) {
                    resultIndex.put(key, new Value(key, value.getValue()));
                } else {
                    resultIndex.get(key).addAndAvg(value.getValue());
                }
            }
        }

        // fill time series with sorted values and return it
        return prepareSeries(resultIndex, combinedSeries);
    }
}
