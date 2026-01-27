package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.HostGroup;
import javafx.util.StringConverter;

public class HostGroupStringConverter extends StringConverter<HostGroup> {

    @Override
    public String toString(HostGroup entity) {
        if (entity != null) {
            return entity.getValueName();
        }
        return "*";
    }

    @Override
    public HostGroup fromString(String s) {
        if (s != null) {
            return new HostGroup(s);
        }
        return new HostGroup("*");
    }
}
