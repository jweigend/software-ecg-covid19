package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;

import java.util.Set;

/**
 * Factory that provides the corresponding implementation of
 * TimeSeriesCombiner service for the specified combining mode.
 */
public class CombinerFactory {

    private static final Set<SeriesCombineMode> SUM_COMBINER = Set.of(
            SeriesCombineMode.SUM_EXACT,
            SeriesCombineMode.SUM_SEC,
            SeriesCombineMode.SUM_MIN,
            SeriesCombineMode.SUM_HOUR,
            SeriesCombineMode.SUM_DAY,
            SeriesCombineMode.SUM_MONTH
    );

    private static final Set<SeriesCombineMode> AVG_COMBINER = Set.of(
            SeriesCombineMode.AVG_EXACT,
            SeriesCombineMode.AVG_SEC,
            SeriesCombineMode.AVG_MIN,
            SeriesCombineMode.AVG_HOUR,
            SeriesCombineMode.AVG_DAY,
            SeriesCombineMode.AVG_MONTH
    );

    /**
     * Returns the corresponding implementation of
     * TimeSeriesCombiner service for the specified combining mode.
     *
     * @param combineMode the SeriesCombineMode the used to find the correct implementation
     * @return the corresponding combiner to the specified mode
     */
    public static TimeSeriesCombiner resolveCombiner(SeriesCombineMode combineMode) {
        if (SUM_COMBINER.contains(combineMode)) {
            return new SumCombiner(combineMode);
        } else if (AVG_COMBINER.contains(combineMode)) {
            return new AvgCombiner(combineMode);
        }

        throw new IllegalArgumentException("Calling CombinerFactory with SeriesCombineMode::NONE doesn't make sense!");
    }
}
