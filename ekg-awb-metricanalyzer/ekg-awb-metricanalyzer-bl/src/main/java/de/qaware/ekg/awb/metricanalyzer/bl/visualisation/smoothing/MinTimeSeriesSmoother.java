package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.sdk.datamodel.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements a series smoothing by use the minimum of all values inside
 * a bucket (time interval) of the given time series.
 */
public class MinTimeSeriesSmoother extends AbstractTimeSeriesSmoother {

    /**
     * Constructs a new instance of MinTimeSeriesSmoother which will use
     * the granularity of timestamp buckets for computing the series smoothing
     *
     * @param smoothingGranularity an SeriesSmoothingGranularity enum that specifies the granularity
     */
    protected MinTimeSeriesSmoother(SeriesSmoothingGranularity smoothingGranularity) {
        super(smoothingGranularity);
    }

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing.AbstractTimeSeriesSmoother#computeValues(...)
     */
    @Override
    protected List<Value> computeSeriesValues(Map<Long, List<Value>> bucketValueMap) {

        Map<Long, Value> resultIndex = new HashMap<>();

        for (Map.Entry<Long, List<Value>> bucket : bucketValueMap.entrySet()) {

            // the shorten key used to group values
            long key = bucket.getKey();

            for (Value bucketValue : bucket.getValue()) {

                Value existingValue = resultIndex.get(key);

                if (existingValue == null) {
                    resultIndex.put(key, new Value(key, bucketValue.getValue()));

                // replace value if the current one is higher
                } else if (existingValue.getValue() > bucketValue.getValue()) {
                    resultIndex.get(key).setValue(bucketValue.getValue());
                }
            }
        }

        return prepareSeries(resultIndex);
    }
}
