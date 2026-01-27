package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;

import java.util.HashMap;
import java.util.List;

/**
 * Combiner implementation that will sum the values of multiple
 * time series at each time they are overlapping.
 */
public class SumCombiner extends Combiner {

    private SeriesCombineMode combineMode;

    public SumCombiner(SeriesCombineMode combineMode) {
        this.combineMode = combineMode;
    }

    @Override
    public TimeSeries combine(String metricName, List<TimeSeries> timeSeriesList) {

        HashMap<Long, Value> resultIndex = new HashMap<>();

        // look for oldest timestamp, the granularity (as divisor parameter) and offset
        GranularityResult analyticsResult;

        if (combineMode == SeriesCombineMode.SUM_EXACT) {
            analyticsResult = analyzeSeries(timeSeriesList);
        } else {
            switch (combineMode) {
                case SUM_SEC:
                    analyticsResult = createGranularityForSec(timeSeriesList);
                    break;
                case SUM_MIN:
                    analyticsResult = createGranularityForMin(timeSeriesList);
                    break;
                case SUM_HOUR:
                    analyticsResult = createGranularityForHours(timeSeriesList);
                    break;
                case SUM_DAY:
                    analyticsResult = createGranularityForDays(timeSeriesList);
                    break;
                case SUM_MONTH:
                    analyticsResult = createGranularityForMonths(timeSeriesList);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected combine mode " + combineMode.getName());

            }
        }

        TimeSeries combinedSeries = new TimeSeries(metricName);

        // proceed indexing and reduce of values
        for (TimeSeries timeSeries : timeSeriesList) {

            // merge meta data (differ filter dimensions will become a '*')
            combinedSeries.mergeMetaData(timeSeries);

            for (Value value : timeSeries.getValues()) {

                // the shorten key used to group values
                long key = analyticsResult.smallestTimestamp + (analyticsResult.bucketSize * ((value.getTimestamp() - analyticsResult.smallestTimestamp) / analyticsResult.bucketSize));

                // insert new value or merge the current one
                if (!resultIndex.containsKey(key)) {
                    resultIndex.put(key, new Value(key, value.getValue()));
                } else {
                    resultIndex.get(key).addAndSum(value.getValue());
                }
            }
        }

        // fill time series with sorted values and return it
        return prepareSeries(resultIndex, combinedSeries);
    }
}
