//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.bindings;

import de.qaware.ekg.awb.common.ui.converter.Converter;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Function;

/**
 * A bidirectional binding (or "bind with inverse") between two properties of different types
 * using a {@link Converter} for conversion between the properties.
 * <p>
 * A bidirectional binding is a binding that works in both directions. If two properties a and
 * b are linked with a bidirectional binding and the value of a changes, b is set to the same
 * value automatically. And vice versa, if b changes, a is set to the same value.
 * <p>
 * Note: this implementation of a bidirectional binding behaves differently from all other bindings
 * here in two important aspects. A property that is linked to another property with a bidirectional
 * binding can still be set (usually bindings would throw an exception). Secondly bidirectional
 * bindings are calculated eagerly, i.e. a bound property is updated immediately.
 * <p>
 * Some javafx controls have different ways to read or write the entered or selected value. In some
 * cases it may required to use different properties to read or write the values. In this cases the
 * binding will be constructed using three properties. The first is read and written as in any other
 * cases. The second property is only read. The third propery is only written.
 *
 * @param <T> The type of the first value.
 * @param <U> The type of the second value
 */
public class ConversionBidirectionalBinding<T, U> implements ChangeListener<Object>, WeakListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionBidirectionalBinding.class);

    private final WeakReference<Property<T>> propertyRef1;
    private final WeakReference<ReadOnlyProperty<U>> propertyRef2R;
    private final WeakReference<Property<U>> propertyRef2W;
    private final Converter<T, U> converter;

    private boolean updating = false;

    /**
     * Init a new bidirectional binding between the given properties using the specified
     * {@link Converter} for conversion.
     *
     * @param property1  The first property of the binding.
     * @param property2r The readable part of the second property of the binding.
     * @param property2w The writable part of the second property of the binding.
     * @param converter  The {@link Converter} used to convert between the properties.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected ConversionBidirectionalBinding(Property<T> property1, ReadOnlyProperty<U> property2r, Property<U> property2w,
                                             Converter<T, U> converter) {
        Objects.requireNonNull(property1, "'property1' must not be null");
        Objects.requireNonNull(property2r, "'property2r' must not be null");
        Objects.requireNonNull(property2w, "'property2w' must not be null");
        Objects.requireNonNull(converter, "'converter' must not be null");
        if (property1 == property2r || property1 == property2w) {
            throw new IllegalArgumentException("Cannot bind property to itself");
        }
        this.converter = converter;
        this.propertyRef1 = new WeakReference<>(property1);
        this.propertyRef2R = new WeakReference<>(property2r);
        this.propertyRef2W = new WeakReference<>(property2w);
    }

    /**
     * Init a new bidirectional binding between the given properties using the specified
     * {@link Converter} for conversion.
     *
     * @param property1 The first property of the binding.
     * @param property2 The second property of the binding.
     * @param converter The {@link Converter} used to convert between the properties.
     */
    protected ConversionBidirectionalBinding(Property<T> property1, Property<U> property2, Converter<T, U> converter) {
        this(property1, property2, property2, converter);
    }

    /**
     * Init a new bidirectional binding between the given properties using the specified
     * {@link Function Functions} for conversion.
     *
     * @param property1  The first property of the binding.
     * @param property2  The second property of the binding.
     * @param fromFirst  The function to convert from {@code property1} to {@code property2}.
     * @param fromSecond The function to convert from {@code property2} to {@code property1}.
     */
    protected ConversionBidirectionalBinding(Property<T> property1, Property<U> property2, Function<T, U> fromFirst,
                                             Function<U, T> fromSecond) {
        this(property1, property2, property2, new FunctionalConverter<>(fromFirst, fromSecond));
    }

    /**
     * Init a new bidirectional binding between the given properties using the specified
     * {@link Function Functions} for conversion.
     *
     * @param property1  The first property of the binding.
     * @param property2r The readable part of the second property of the binding.
     * @param property2w The wirtable part of the second property of the binding.
     * @param fromFirst  The function to convert from {@code property1} to {@code property2}.
     * @param fromSecond The function to convert from {@code property2} to {@code property1}.
     */
    protected ConversionBidirectionalBinding(Property<T> property1, ReadOnlyProperty<U> property2r, Property<U> property2w,
                                             Function<T, U> fromFirst, Function<U, T> fromSecond) {
        this(property1, property2r, property2w, new FunctionalConverter<>(fromFirst, fromSecond));
    }

    /**
     * Get the first property.
     *
     * @return The property.
     */
    protected Property<T> getProperty1() {
        return propertyRef1.get();
    }

    /**
     * Get the readable part of the second property.
     *
     * @return The property.
     */
    protected ReadOnlyProperty<U> getProperty2R() {
        return propertyRef2R.get();
    }

    /**
     * Get the writable part of the second property.
     *
     * @return The property.
     */
    public Property<U> getProperty2W() {
        return propertyRef2W.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversionBidirectionalBinding)) {
            return false;
        }
        ConversionBidirectionalBinding<?, ?> that = (ConversionBidirectionalBinding<?, ?>) o;
        return new EqualsBuilder()
                .append(getProperty1(), that.getProperty1())
                .append(getProperty2R(), that.getProperty2R())
                .append(getProperty2W(), that.getProperty2W())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getProperty1())
                .append(getProperty2R())
                .append(getProperty2W())
                .toHashCode();
    }

    /**
     * This method needs to be provided by an implementation of
     * {@code ChangeListener}. It is called if the value of an
     * {@link ObservableValue} changes.
     * <p>
     * In general is is considered bad practice to modify the observed value in
     * this method.
     *
     * @param observable The {@code ObservableValue} which value changed
     * @param oldValue   The old value
     * @param newValue   The new value
     */
    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
        if (updating) {
            return;
        }
        final Property<T> property1 = getProperty1();
        final ReadOnlyProperty<U> property2r = getProperty2R();
        final Property<U> property2w = getProperty2W();
        if (wasGarbageCollected()) {
            if (property1 != null) {
                property1.removeListener(this);
            }
            if (property2r != null) {
                property2r.removeListener(this);
            }
            if (property2w != null) {
                property2w.removeListener(this);
            }
        } else {
            try {
                updating = true;
                if (property1 == observable) {
                    setAndConvertValue(property1, property2w, converter::fromFirst);
                } else {
                    setAndConvertValue(property2r, property1, converter::fromSecond);
                }
            } finally {
                updating = false;
            }
        }
    }

    /**
     * Converts the source value and try to set the target property.
     * <p>
     * In case of any exceptions while converting the source value into the target type the target will set to null.
     *
     * @param source    The source property.
     * @param target    The target property.
     * @param converter The converter function to convert the source value into the target type.
     * @param <S>       The source value type.
     * @param <T>       The target value type.
     */
    private static <S, T> void setAndConvertValue(ReadOnlyProperty<S> source, Property<T> target, Function<S, T> converter) {
        try {
            target.setValue(converter.apply(source.getValue()));
        } catch (Exception e) {
            LOGGER.info("Exception while converting '" + getPropertyName(source) +
                    "' to '" + getPropertyName(target) + "' in bidirectional binding", e);
            target.setValue(null);
        }
    }

    /**
     * Get the property name.
     *
     * @param property the property
     * @return the name for the property.
     */
    protected static String getPropertyName(ReadOnlyProperty<?> property) {
        if (property == null) {
            return "";
        }
        if (property.getBean() == null) {
            return "";
        }
        if (StringUtils.isBlank(property.getName())) {
            return "";
        }
        return property.getBean().getClass().getName() + ":" + property.getName();
    }

    /**
     * Returns {@code true} if the linked listener was garbage-collected.
     * In this case, the listener can be removed from the observable.
     *
     * @return {@code true} if the linked listener was garbage-collected.
     */
    @Override
    public boolean wasGarbageCollected() {
        return getProperty1() == null || getProperty2R() == null || getProperty2W() == null;
    }

    /**
     * Helper {@link Converter} to use two lambda functions as converter.
     *
     * @param <T> The type of the first value.
     * @param <U> The type of the second value.
     */
    private static class FunctionalConverter<T, U> implements Converter<T, U> {
        private final Function<T, U> fromFirst;
        private final Function<U, T> fromSecond;

        /**
         * Init the converter.
         *
         * @param fromFirst  Converts the first to the second type.
         * @param fromSecond Converts the second type to the first type.
         */
        public FunctionalConverter(Function<T, U> fromFirst, Function<U, T> fromSecond) {
            Objects.requireNonNull(fromFirst, "Converter method 'fromFirst()' must not be null");
            Objects.requireNonNull(fromSecond, "Converter method 'fromSecond()' must not be null");
            this.fromFirst = fromFirst;
            this.fromSecond = fromSecond;
        }

        @Override
        public U fromFirst(T first) {
            return fromFirst.apply(first);
        }

        @Override
        public T fromSecond(U second) {
            return fromSecond.apply(second);
        }
    }
}
