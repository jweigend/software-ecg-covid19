package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.NamedValueEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * An domain entity that represents a host group / cluster
 * and stores the properties that are part of it.
 */
public class HostGroup extends NamedValueEntity {

    public static final HostGroup DEFAULT = new HostGroup("*");

    public HostGroup() {
        this("");
    }

    public HostGroup(String valueName) {
        super(valueName);
    }

    @Override
    public String toString() {
        return HostGroup.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
