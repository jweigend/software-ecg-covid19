package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.NamedValueEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * Entity that represents an (Docker) container as part of an pod
 * (atomic deployment unit) inside an Cloud-Native environment.
 */
public class Container extends NamedValueEntity {

    public static final Container DEFAULT = new Container("*");

    public Container() {
        this("");
    }

    public Container(String valueName) {
        super(valueName);
    }

    @Override
    public String toString() {
        return Container.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
