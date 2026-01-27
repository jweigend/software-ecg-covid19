package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.sdk.datamodel.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implements a series smoothing by use the count of values inside
 * a bucket (time interval) of the given time series.
 * This is useful for metrics like requests per second or similar.
 */
public class ValueCountTimeSeriesSmoother extends AbstractTimeSeriesSmoother {

    /**
     * Constructs a new instance of ValueCountTimeSeriesSmoother which will use
     * the granularity of timestamp buckets for computing the series smoothing
     *
     * @param smoothingGranularity an SeriesSmoothingGranularity enum that specifies the granularity
     */
    protected ValueCountTimeSeriesSmoother(SeriesSmoothingGranularity smoothingGranularity) {
        super(smoothingGranularity);
    }

    @Override
    protected List<Value> computeSeriesValues(Map<Long, List<Value>> bucketValueMap) {

        List<Value> resultList = new ArrayList<>(bucketValueMap.size());

        for (Map.Entry<Long, List<Value>> bucket : bucketValueMap.entrySet()) {
            resultList.add(new Value(bucket.getKey(), bucket.getValue().size()));
        }

        Collections.sort(resultList);

        return resultList;
    }
}
