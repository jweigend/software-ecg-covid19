package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et;

import java.util.Objects;

/**
 * Base class for all query entities that have a
 * name value in String format.
 */
public abstract class NamedValueEntity {

    private String valueName;

    public NamedValueEntity(String valueName) {
        this.valueName = valueName;
    }

    public String getValueName() {
        return valueName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedValueEntity that = (NamedValueEntity) o;
        return Objects.equals(valueName, that.valueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueName);
    }


}
