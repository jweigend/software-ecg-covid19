package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.NamedValueEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * Entity that represents an Kubernetes/OpenShift pod as
 * atomic deployment unit inside an Cloud-Native environment.
 */
public class Pod extends NamedValueEntity {

    public static final Pod DEFAULT = new Pod("*");

    public Pod() {
        this("");
    }

    public Pod(String valueName) {
        super(valueName);
    }

    @Override
    public String toString() {
        return Pod.class.getSimpleName() + "[ "
                + (StringUtils.isBlank(getValueName()) ? "-BLANK-" : getValueName()) + " ]";
    }
}
