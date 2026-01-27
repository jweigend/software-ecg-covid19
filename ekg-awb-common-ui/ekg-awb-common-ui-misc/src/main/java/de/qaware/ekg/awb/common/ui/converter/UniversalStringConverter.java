package de.qaware.ekg.awb.common.ui.converter;

/**
 * Implementation of the {@link Converter} interface and javafx' {@link javafx.util.StringConverter}
 * to allow the implementing converters be compatible to both converter.
 *
 * @param <T> The type of the first property.
 */
public abstract class UniversalStringConverter<T> extends javafx.util.StringConverter<T> implements Converter<T, String> {
    /**
     * Converting the value into the string representation.
     *
     * @param first The value represented as the first type.
     * @return The string representation of the value.
     */
    @Override
    public final String fromFirst(T first) {
        return toString(first);
    }

    /**
     * Converting the value back from a string.
     *
     * @param second The value represented as string..
     * @return The converted back value.
     */
    @Override
    public final T fromSecond(String second) {
        return fromString(second);
    }
}
