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
 * Represents a Measurement.
 * <p>
 * A Measurement contains at least one host.
 */
public class Measurement extends NamedValueEntity implements Comparable<Measurement>, Serializable {
    public static final Measurement DEFAULT = new Measurement("*", -1, -1);
    private static final long serialVersionUID = -7692982449097633579L;



    /**
     * The start.
     */
    private final long start;

    /**
     * The end.
     */
    private final long end;

    public Measurement() {
        this("");
    }

    public Measurement(String name) {
        this(name, -1, -1);
    }

    /**
     * Instantiates a new series.
     *
     * @param name  the name
     * @param start the start
     * @param end   the end
     */
    public Measurement(String name, long start, long end) {
        super(name);
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return getValueName();
    }

    /**
     * Gets the start.
     *
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * Gets the end.
     *
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Measurement)) {
            return false;
        }

        Measurement that = (Measurement) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
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
        return Measurement.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }

    @Override
    public int compareTo(Measurement m) {
        return getValueName().compareTo(m.getName());
    }
}
