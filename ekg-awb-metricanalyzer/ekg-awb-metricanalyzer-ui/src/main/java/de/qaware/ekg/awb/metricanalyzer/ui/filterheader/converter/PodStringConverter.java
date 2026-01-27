package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Pod;
import javafx.util.StringConverter;

public class PodStringConverter extends StringConverter<Pod> {

    @Override
    public String toString(Pod entity) {
        if (entity != null) {
            return entity.getValueName();
        }
        return "*";
    }

    @Override
    public Pod fromString(String s) {
        if (s != null) {
            return new Pod(s);
        }
        return new Pod("*");
    }
}
