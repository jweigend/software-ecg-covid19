package de.qaware.ekg.awb.metricanalyzer.bl.api;

import de.qaware.ekg.awb.sdk.core.NamedEnum;

/**
 * Enumeration that represents the different modes of
 * time series combining
 */
public enum SeriesCombineMode implements NamedEnum {

    /**
     * inactive, no combine of any series
     */
    NONE ("Inaktiv"),

    /**
     * Similar series that are identically in each filter dimension except
     * the measurement will concat to a single long series.
     *
     * Result: a set of unique time series with no measurement value because it used as combined
     */
    CONCAT ("Concatenate"),

    /**
     * All series will merge together by adding values in the same slices.
     * If multiple series exists in the same time interval it will sum.
     * Series in different time intervals will just concat.
     *
     * Result: a single time series with the sum of all input series at each covered time slot
     */
    SUM_EXACT("Add up (exact)"),

    SUM_SEC("Add up (per second)"),

    SUM_MIN("Add up (per minute)"),

    SUM_HOUR("Add up (per hour)"),

    SUM_DAY("Add up (per day)"),

    SUM_MONTH("Add up (per month)"),

    /**
     * All series will merge together by calculate the average value of each series point in
     * the same time slices and concat values in different slices.
     *
     * Result: a single time series with the average of all input series at each covered time slot
     */
    AVG_EXACT("Average (exact)"),

    AVG_SEC("Average (per second)"),

    AVG_MIN("Average (per minute)"),

    AVG_HOUR("Average (per hour)"),

    AVG_DAY("Average (per day)"),

    AVG_MONTH("Average (per month)");

    /**
     * The readable name of the enumeration
     */
    private String name;

    /**
     * Internal constructor that creates enum of this type
     * with the readable name that represents the enum value.
     *
     * @param name the readable name that represents the enum value
     */
    SeriesCombineMode(String name) {
        this.name = name;
    }

    /**
     * Returns the readable name that represents the enum value
     *
     * @return the alias name of the enumeration
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
