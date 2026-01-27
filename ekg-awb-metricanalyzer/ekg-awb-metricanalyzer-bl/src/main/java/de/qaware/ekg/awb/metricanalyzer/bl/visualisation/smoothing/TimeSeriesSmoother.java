package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing;

import de.qaware.ekg.awb.sdk.datamodel.Value;

import java.util.List;

/**
 * An interface that represents processors that neat the time series by
 * using individual algorithms that modify the amount and/or values of the
 * given Value list.
 */
public interface TimeSeriesSmoother {

    /**
     * Create a new list of Value instances based on the given one.
     * The promise of the implementation is to smooth/neat the series
     * so the values have less divergence to each other.
     *
     * For this the returned list will not be equal in it's size or concrete values.
     *
     * @param originalValueList a list of Value instances that represents time series
     * @return a new or modified list with smoothed values inside.
     */
    List<Value> computeSmoothing(List<Value> originalValueList);
}
