//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.bindings;

import de.qaware.ekg.awb.common.ui.converter.Converter;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.util.function.Function;

import static java.util.function.Function.identity;

/**
 * The Bindings class is a helper class containing utility functions to create bidirectional bindings for javafx.
 */
public final class Bindings {

    private Bindings() {
    }

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
     *
     * @param property1 The first property of the binding.
     * @param property2 The second property of the binding.
     * @param converter The {@link Converter} used to convert between the properties.
     * @param <T>       The type of the first value.
     * @param <U>       The type of the second value.
     * @return the created binding
     */
    public static <T, U> ConversionBidirectionalBinding bindBidirectional(Property<T> property1, Property<U> property2, Converter<T, U> converter) {
        ConversionBidirectionalBinding<T, U> binding = new ConversionBidirectionalBinding<>(property1, property2, converter);
        property1.setValue(convertIfNotNull(property2, converter::fromSecond));
        property1.addListener(binding);
        property2.addListener(binding);
        return binding;
    }

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
     * This binding uses different properties for reading and writing the second part of the binding.
     *
     * @param property1  The first property of the binding.
     * @param property2r The readable part of the second property of the binding.
     * @param property2w The writable part of the second property of the binding.
     * @param converter  The {@link Converter} used to convert between the properties.
     * @param <T>        The type of the first value.
     * @param <U>        The type of the second value.
     * @return the created binding
     */
    public static <T, U> ConversionBidirectionalBinding bindBidirectional(Property<T> property1, ReadOnlyProperty<U> property2r, Property<U> property2w, Converter<T, U> converter) {
        ConversionBidirectionalBinding<T, U> binding = new ConversionBidirectionalBinding<>(property1, property2r, property2w, converter);
        property1.setValue(convertIfNotNull(property2r, converter::fromSecond));
        property1.addListener(binding);
        property2r.addListener(binding);
        return binding;
    }


    /**
     * A bidirectional binding (or "bind with inverse") between two properties of different types
     * using two lambda {@link Function Functions} for conversion between the properties.
     * <p>
     * A bidirectional binding is a binding that works in both directions. If two properties a and
     * b are linked with a bidirectional binding and the value of a changes, b is set to the same
     * value automatically. And vice versa, if b changes, a is set to the same value.
     * <p>
     * Note: this implementation of a bidirectional binding behaves differently from all other bindings
     * here in two important aspects. A property that is linked to another property with a bidirectional
     * binding can still be set (usually bindings would throw an exception). Secondly bidirectional
     * bindings are calculated eagerly, i.e. a bound property is updated immediately.
     *
     * @param property1  The first property of the binding.
     * @param property2  The second property of the binding.
     * @param fromFirst  The function that converts the first to the second type.
     * @param fromSecond The function that converts the second type to the first type.
     * @param <T>        The type of the first value.
     * @param <U>        The type of the second value.
     * @return the created binding
     */
    public static <T, U> ConversionBidirectionalBinding bindBidirectional(Property<T> property1, Property<U> property2, Function<T, U> fromFirst, Function<U, T> fromSecond) {
        ConversionBidirectionalBinding<T, U> binding = new ConversionBidirectionalBinding<>(property1, property2, fromFirst, fromSecond);
        property1.setValue(convertIfNotNull(property2, fromSecond));
        property1.addListener(binding);
        property2.addListener(binding);
        return binding;
    }

    /**
     * A bidirectional binding (or "bind with inverse") between two properties of different types
     * using two lambda {@link Function Functions} for conversion between the properties.
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
     * This binding uses different properties for reading and writing the second part of the binding.
     *
     * @param property1  The first property of the binding.
     * @param property2r The readable part of the second property of the binding.
     * @param property2w The writable part of the second property of the binding.
     * @param fromFirst  The function that converts the first to the second type.
     * @param fromSecond The function that converts the second type to the first type.
     * @param <T>        The type of the first value.
     * @param <U>        The type of the second value.
     * @return the created binding
     */
    public static <T, U> ConversionBidirectionalBinding bindBidirectional(Property<T> property1, ReadOnlyProperty<U> property2r, Property<U> property2w, Function<T, U> fromFirst, Function<U, T> fromSecond) {
        ConversionBidirectionalBinding<T, U> binding = new ConversionBidirectionalBinding<>(property1, property2r, property2w, fromFirst, fromSecond);
        property1.setValue(convertIfNotNull(property2r, fromSecond));
        property1.addListener(binding);
        property2r.addListener(binding);
        return binding;
    }

    /**
     * A bidirectional binding (or "bind with inverse") between two properties of different types
     * using two lambda {@link Function Functions} for conversion between the properties.
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
     * This binding uses different properties for reading and writing the second part of the binding.
     *
     * @param property1  The first property of the binding.
     * @param property2r The readable part of the second property of the binding.
     * @param property2w The writable part of the second property of the binding.
     * @param <T>        The type of the first value.
     * @return the created binding
     */
    public static <T> ConversionBidirectionalBinding bindBidirectional(Property<T> property1, ReadOnlyProperty<T> property2r, Property<T> property2w) {
        return bindBidirectional(property1, property2r, property2w, identity(), identity());
    }

    /**
     * Remove the binding between the both properties.
     *
     * @param property1 The first property of the binding.
     * @param property2 The second property of the binding.
     * @param <T>       The type of the first value.
     * @param <U>       The type of the second value.
     */
    public static <T, U> void unbind(Property<T> property1, Property<U> property2) {
        ConversionBidirectionalBinding<T, U> binding = new ConversionBidirectionalBinding<>(property1, property2, i -> null, i -> null);
        property1.removeListener(binding);
        property2.removeListener(binding);
    }

    /**
     * Only convert the property value if it is not null.
     *
     * @param property  the property to convert.
     * @param converter the converter function.
     * @param <T>       The source type.
     * @param <U>       The target type.
     * @return the converted value or null if the source is null.
     */
    private static <T, U> U convertIfNotNull(ReadOnlyProperty<T> property, Function<T, U> converter) {
        if (property.getValue() != null) {
            return converter.apply(property.getValue());
        } else {
            return null;
        }
    }


    /**
     * Initializes a given {@link ComboBox}. It sets the items, the value property and may set an event handler when a
     * key was pressed.
     *
     * @param comboBox The {@code ComboBox} to initialize.
     * @param value    the value property
     * @param itemList a observable list with the items.
     * @param <T>      the actual type of items and value
     */
    public static <T> void bindComboBox(ComboBox<T> comboBox, ObservableList<T> itemList, Property<T> value) {
        comboBox.getItems().setAll(itemList);
        itemList.addListener((ListChangeListener<T>) c -> {
            comboBox.getItems().setAll(c.getList());
            comboBox.getSelectionModel().select(value.getValue());
        });
        comboBox.valueProperty().bindBidirectional(value);
    }

    /**
     * Initializes a given {@link DatePicker}. It sets the items, the value property and may set an event handler when a
     * key was pressed.
     *
     * @param datePicker The {@code DatePicker} to initialize.
     * @param value    the value property
     * @param converter converter form T to Local date.
     * @param <T>      the actual type of items and value
     */
    public static <T> void bindDatePicker(DatePicker datePicker, Property<T> value, Converter<T, LocalDate> converter) {
        Bindings.bindBidirectional(value, datePicker.valueProperty(), converter);
    }
}
