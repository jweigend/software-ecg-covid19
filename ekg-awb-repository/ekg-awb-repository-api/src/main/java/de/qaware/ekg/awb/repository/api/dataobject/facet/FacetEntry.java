package de.qaware.ekg.awb.repository.api.dataobject.facet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A single facet entry for a facet. Holds the entry name and the count.
 */
public class FacetEntry {

    private String name;
    private long count;

    /**
     * Instantiates a new facet entry.
     *
     * @param name  the name
     * @param count the count
     */
    public FacetEntry(String name, long count) {
        this.name = name;
        this.count = count;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        FacetEntry rhs = (FacetEntry) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.name)
                .append(this.count, rhs.count)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(count)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("name", name)
                .append("count", count)
                .toString();
    }

}
