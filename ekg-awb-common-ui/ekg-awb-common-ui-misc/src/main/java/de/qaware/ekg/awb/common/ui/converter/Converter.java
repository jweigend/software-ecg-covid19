package de.qaware.ekg.awb.common.ui.converter;

/**
 * This is a generic converter that allows to convert values from one type into another and back.
 *
 * @param <T> The first type of the value.
 * @param <U> The second type of the value.
 */
public interface Converter<T, U> {
    /**
     * Convert the value from the first type into the second type.
     *
     * @param first The value represented as the first type.
     * @return The value represented in the second type.
     */
    U fromFirst(T first);

    /**
     * Convert back the value from the second type into the first type.
     *
     * @param second The value represented as the second type.
     * @return The value converted back into the first type.
     */
    T fromSecond(U second);
}
