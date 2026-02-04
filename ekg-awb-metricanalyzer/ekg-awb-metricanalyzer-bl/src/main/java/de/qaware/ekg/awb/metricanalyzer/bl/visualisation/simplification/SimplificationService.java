//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification;

import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;

import java.util.List;

/**
 * Interface that represents service that simplify time series
 * to improve the rendering performance.
 * In the most common cases this will do by some type of vectorization.
 */
@FunctionalInterface
public interface SimplificationService {

    /**
     * Reduce the amount of data points in the given time series to a maximum defined
     * by the seriesValueLimit parameter.
     * The seriesValueLimit will threaded as overall amount limit. If the time series together have
     * less than the limit the given series will keep untouched.
     *
     * @param timeSeries the time series to vectorize
     * @param threshold threshold for the number of points for all series together;
     *                  0 indicates, that there should no simplification started
     * @return vectorized time series
     */
    List<TimeSeries> simplify(List<TimeSeries> timeSeries, int threshold);
}
