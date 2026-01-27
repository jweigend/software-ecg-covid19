package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.sdk.core.NamedEnum;
import javafx.util.StringConverter;

/**
 * An Enum to String converter that will provide the alias name
 * for the given enum
 */
public class NamedEnumConverter<T extends NamedEnum> extends StringConverter<T> {

    @Override
    public String toString(NamedEnum namedEnum) {
        return namedEnum.getName();
    }

    @Override
    public T fromString(String string) {
        return null;
    }
}
