//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.api;

import de.qaware.ekg.awb.sdk.core.NamedEnum;

/**
 * ValueType defines how the series values are aggregated.
 */
public enum SeriesSmoothingType implements NamedEnum {

    /**
     * The Smoother will returned the untouched value list
     */
    NONE ("Inactive"),

    /**
     * smooth each series by calculating the average value
     * of each series value in a defined time slice
     */
    AVG ("Average"),

    /**
     * The MIN.
     */
    MIN ("Min"),

    /**
     * The MAX.
     */
    MAX ("Max"),

    /**
     * The accumulated value of all values in a bucket of time series
     */
    SUM ("Sum"),

    /**
     * The median value of all values in the granularity range.
     */
    MEDIAN ("Median"),

    /**
     * The amount of all values points (per time interval) independent of the individual values
     */
    VALUE_COUNT ("Number of values"),

    /**
     * The difference of the sum of all values inside the defined time interval to the
     * previous time interval (or to zero if there is no previous interval)
     */
    DIFF ("Difference between values");


    private String name;

    SeriesSmoothingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
