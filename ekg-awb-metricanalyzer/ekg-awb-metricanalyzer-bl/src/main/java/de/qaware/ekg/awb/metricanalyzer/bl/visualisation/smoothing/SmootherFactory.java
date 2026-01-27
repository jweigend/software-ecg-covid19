package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingType;

/**
 * Factory that serves a TimeSeriesSmoother implementation matches
 * to the series smoothing typ.
 */
public class SmootherFactory {

    /**
     * Returns a TimeSeriesSmoother implementation matches
     * to the series smoothing typ.
     *
     * @param smoothingType the type of the smoothing algorithm the caller specifies
     * @param smoothingGranularity an SeriesSmoothingGranularity enum that specifies the granularity
     * @return an instance of TimeSeriesSmoother that implements the requested algorithm and uses the granularity
     */
    public TimeSeriesSmoother resolveSmoother(SeriesSmoothingType smoothingType,
                                              SeriesSmoothingGranularity smoothingGranularity) {
        if (smoothingType == null) {
            throw new IllegalArgumentException("Aggregation Type must not be null");
        }

        AbstractTimeSeriesSmoother seriesSmoother;
        switch (smoothingType) {

            case AVG:
                seriesSmoother = new AvgTimeSeriesSmoother(smoothingGranularity);
                break;
            case SUM:
                seriesSmoother = new SumTimeSeriesSmoother(smoothingGranularity);
                break;
            case MEDIAN:
                seriesSmoother = new MedianTimeSeriesSmoother(smoothingGranularity);
                break;
            case MAX:
                seriesSmoother = new MaxTimeSeriesSmoother(smoothingGranularity);
                break;
            case MIN:
                seriesSmoother = new MinTimeSeriesSmoother(smoothingGranularity);
                break;
            case VALUE_COUNT:
                seriesSmoother = new ValueCountTimeSeriesSmoother(smoothingGranularity);
                break;
            case DIFF:
                seriesSmoother = new DifferenceTimeSeriesSmoother(smoothingGranularity);
                break;
            default:
                throw new AssertionError("Invalid smoothing");
        }

        return seriesSmoother;
    }
}
