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
 * The Class Process.
 */
public class Process extends NamedValueEntity implements Comparable<Process>, Serializable {
    public static final Process DEFAULT = new Process("*", "DEFAULT");
    private static final long serialVersionUID = 8801901289904955589L;


    /**
     * The description.
     */
    private final String description;

    public Process() {
        this("", "");
    }

    public Process(String name) {
        this(name, "");
    }

    /**
     * Instantiates a new process.
     *
     * @param name        the name
     * @param description the description
     */
    public Process(String name, String description) {
        super(name);
        this.description = description;
    }

    /**
     * Value of.
     *
     * @param process the process
     * @return the process
     */
    public static Process valueOf(String process) {
        return new Process(process, "");
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
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(Process process) {
        return getValueName().compareTo(process.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Process)) {
            return false;
        }
        Process procs = (Process) o;
        return new EqualsBuilder()
                .append(getName(), procs.getName())
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
        return Process.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
