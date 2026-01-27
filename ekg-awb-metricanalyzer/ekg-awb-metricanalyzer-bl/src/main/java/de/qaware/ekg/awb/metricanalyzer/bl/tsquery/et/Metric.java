//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * The Class Metric.
 */
public class Metric extends NamedValueEntity implements Comparable<Metric>, Serializable {

    private static final long serialVersionUID = -4091118172242720577L;


    public Metric() {
        this("");
    }

    /**
     * Instantiates a new metric.
     *
     * @param name the name
     */
    public Metric(String name) {
        super(name);
    }

    /**
     * Value of.
     *
     * @param aMetric the a metric
     * @return the metric
     */
    public static Metric valueOf(String aMetric) {
        return new Metric(aMetric);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.getValueName();
    }

    @Override
    public int compareTo(Metric metric) {
        return getValueName().compareTo(metric.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Metric)) {
            return false;
        }

        Metric metric = (Metric) o;

        return new EqualsBuilder()
                .append(getName(), metric.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ " + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
