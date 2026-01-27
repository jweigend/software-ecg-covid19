package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.NamedValueEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an service inside a CloudNative environment that will
 * be a facade for 1-n pods used for load-balancing and routing.
 */
public class Service extends NamedValueEntity {

    public static final Service DEFAULT = new Service("*");

    public Service() {
        this("");
    }

    public Service(String valueName) {
        super(valueName);
    }

    @Override
    public String toString() {
        return Service.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
