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
 * Represents a group of counter or logs.
 */
public class MetricGroup extends NamedValueEntity implements Comparable<MetricGroup>, Serializable {

    public static final MetricGroup DEFAULT = new MetricGroup("*");
    private static final long serialVersionUID = -945916908658499174L;


    public MetricGroup() {
        this("");
    }


    /**
     * Instantiates a new group.
     *
     * @param name the name
     */
    public MetricGroup(String name) {
        super(name);
    }

    /**
     * Value of.
     *
     * @param group the group
     * @return the group
     */
    public static MetricGroup valueOf(String group) {
        return new MetricGroup(group);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return getValueName();
    }

    @Override
    public int compareTo(MetricGroup metricGroup) {
        return getValueName().compareTo(metricGroup.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetricGroup)) {
            return false;
        }
        MetricGroup metricGroup = (MetricGroup) o;
        return new EqualsBuilder()
                .append(getName(), metricGroup.getName())
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
        return MetricGroup.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
