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
 * SeriesSmoothingGranularity levels.
 * These are all the possible sampling levels
 * of the Software EKG.
 */
public enum SeriesSmoothingGranularity implements NamedEnum {

    /**
     * each value of the original series will result
     * in it's own bucket. This will only required in
     * combination with DifferenceTimeSeriesSmoother.
     */
    OFF("Minimal/exact"),

    /**
     * group time series values into buckets
     * with time interval of one second
     */
    SECONDS("Seconds"),

    /**
     * group time series values into buckets
     * with time interval of one minute
     */
    MINUTE("Minutes"),

    /**
     * group time series values into buckets
     * with time interval of one hour
     */
    HOUR("Hours"),

    /**
     * group time series values into buckets
     * with time interval of one day
     */
    DAY("Days"),

    /**
     * group time series values into buckets
     * with time interval of one week
     */
    WEEK("Weeks"),

    /**
     * group time series values into buckets
     * with fix time interval of one month (30 days)
     * starting from the first value in the series.
     */
    MONTH("Months"),

    /**
     * group time series values into buckets
     * matches to the calendar months in the range
     * that is covered by the series.
     */
    CALENDAR_MONTH("Calender months"),

    /**
     * group time series values into buckets
     * with time interval of a quarter of year (3 months)
     */
    QUARTER("Quarter"),

    /**
     * group time series values into buckets
     * with time interval of of 6 months
     */
    HALF_YEAR("Half-year"),

    /**
     * let the smoothing algorithm decide
     * which will be the best time interval
     */
    AUTO("Auto");

    private String name;

    SeriesSmoothingGranularity(String name) {
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
