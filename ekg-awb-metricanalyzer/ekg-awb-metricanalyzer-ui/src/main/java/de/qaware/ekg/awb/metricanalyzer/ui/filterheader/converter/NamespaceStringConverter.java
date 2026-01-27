package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import javafx.util.StringConverter;

import java.util.Objects;

public class NamespaceStringConverter extends StringConverter<Namespace> {

    @Override
    public String toString(Namespace entity) {
        if (entity != null) {
            return entity.getValueName();
        }
        return "*";
    }

    @Override
    public Namespace fromString(String s) {
        return new Namespace(Objects.requireNonNullElse(s, "*"));
    }
}

