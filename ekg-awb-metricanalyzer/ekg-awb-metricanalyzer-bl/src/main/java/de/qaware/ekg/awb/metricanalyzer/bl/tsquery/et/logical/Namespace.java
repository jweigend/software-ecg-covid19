package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.NamedValueEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * Entity that represents an Kubernetes Namespace or OpenShift project
 */
public class Namespace extends NamedValueEntity {

    public static final Namespace DEFAULT = new Namespace("*");

    public Namespace() {
        this("");
    }

    public Namespace(String valueName) {
        super(valueName);
    }

    @Override
    public String toString() {
        return Namespace.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
