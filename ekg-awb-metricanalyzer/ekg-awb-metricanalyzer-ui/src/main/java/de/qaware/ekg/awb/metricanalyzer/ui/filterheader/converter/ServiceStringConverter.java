package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Service;
import javafx.util.StringConverter;

public class ServiceStringConverter extends StringConverter<Service> {

    @Override
    public String toString(Service service) {
        if (service != null) {
            return service.getValueName();
        }
        return "*";
    }

    @Override
    public Service fromString(String s) {
        if (s != null) {
            return new Service(s);
        }
        return new Service("*");
    }
}