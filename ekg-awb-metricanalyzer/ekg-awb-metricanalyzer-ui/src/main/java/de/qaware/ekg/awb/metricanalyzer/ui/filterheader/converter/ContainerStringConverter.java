package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Container;
import javafx.util.StringConverter;

public class ContainerStringConverter extends StringConverter<Container> {

    @Override
    public String toString(Container container) {
        if (container != null) {
            return container.getValueName();
        }
        return "*";
    }

    @Override
    public Container fromString(String s) {
        if (s != null) {
            return new Container(s);
        }
        return new Container("*");
    }
}