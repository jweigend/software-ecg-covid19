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
package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical;


import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.NamedValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Represents a host.
 * <p>
 * A host contains groups.
 */
public class Host  extends NamedValueEntity implements Comparable<Host>, Serializable {

    private static final long serialVersionUID = -1895081683699349104L;

    public static final Host DEFAULT = new Host("*", "DEFAULT");

    /**
     * The address.
     */
    private final String address;

    /**
     * Instantiates a new host.
     *
     * @param name    the name
     * @param address the address
     */
    public Host(String name, String address) {
        super(name);
        this.address = address;
    }

    public Host() {
        this("");
    }

    /**
     * Instantiates a new host.
     *
     * @param string the string
     */
    public Host(String string) {
        this(string, "");
    }

    /**
     * Value of.
     *
     * @param aHost the a host
     * @return the host
     */
    public static Host valueOf(String aHost) {
        return new Host(aHost, "");
    }


    /**
     * Gets the address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    public String getName() {
        return getValueName();
    }

    @Override
    public int compareTo(Host host) {
        return getValueName().compareTo(host.getValueName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Host)) {
            return false;
        }

        Host host = (Host) o;

        return new EqualsBuilder()
                .append(getName(), host.getName())
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
        return Host.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }

}
