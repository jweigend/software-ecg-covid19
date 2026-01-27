package de.qaware.ekg.awb.repository.api.dataobject.facet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single facet. Holds the name and all facet entries.
 */
public class Facet {

    private final String name;
    private final List<FacetEntry> entries;

    /**
     * Instantiates a new facet with give name.
     *
     * @param name the name
     */
    public Facet(String name) {
        this(name, Collections.emptyList());
    }

    /**
     * Instantiates a new facet.
     *
     * @param name    the name
     * @param entries the entries
     */
    public Facet(String name, List<FacetEntry> entries) {
        Validate.notNull(entries);
        this.name = name;
        this.entries = new ArrayList<>(entries);
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the facet entries as unmodifiable view.
     *
     * @return the facet entries
     */
    public List<FacetEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Add a FacetEntry to this facet.
     *
     * @param entry the entry
     */
    public void addEntry(final FacetEntry entry) {
        Validate.notNull(entry);
        entries.add(entry);
    }

    /**
     * Gets the entry count.
     *
     * @return the entry count
     */
    public long getEntryCount() {
        return entries.size();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("name", name).
                append("entries", entries).
                toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.name).append(this.entries).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Facet other = (Facet) obj;
        return new EqualsBuilder().append(this.name, other.name).append(this.entries, other.entries).isEquals();
    }

}
