package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing;

import com.carrotsearch.hppc.DoubleArrayList;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implements a series smoothing by use the average of all values inside
 * a bucket (time interval) of the given time series.
 */
public class AvgTimeSeriesSmoother extends AbstractTimeSeriesSmoother {

    /**
     * Constructs a new instance of AvgTimeSeriesSmoother which will use
     * the granularity of timestamp buckets for computing the series smoothing
     *
     * @param smoothingGranularity an SeriesSmoothingGranularity enum that specifies the granularity
     */
    protected AvgTimeSeriesSmoother(SeriesSmoothingGranularity smoothingGranularity) {
        super(smoothingGranularity);
    }

    /* (non-Javadoc)
     * @see de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing.AbstractTimeSeriesSmoother#computeValues(...)
     */
    @Override
    protected List<Value> computeSeriesValues(Map<Long, List<Value>> bucketValueMap) {

        List<Value> resultList = new ArrayList<>(bucketValueMap.size());

        for (Map.Entry<Long, List<Value>> bucket : bucketValueMap.entrySet()) {

            DoubleArrayList doubleList = DoubleArrayList.from();

            for (Value bucketValue : bucket.getValue()) {
                doubleList.add(bucketValue.getValue());
            }

            double average = StatUtils.mean(doubleList.buffer, 0, doubleList.elementsCount);
            resultList.add(new Value(bucket.getKey(), average));
        }

        Collections.sort(resultList);

        return resultList;
    }
}
