package de.qaware.ekg.awb.common.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for StringConverter implementations that should convert
 * simple strings to complex entities or choose the right existing entity
 * based on the autocomplete text input of the user.
 */
public abstract class FilterableComboBoxStringConverter<T> extends StringConverter<T> {

    private ObjectProperty<ObservableList<T>> filteredItemsProperty;

    protected T selectValue(String textInput) {

        if (StringUtils.isBlank(textInput)) {
            return null;
        }

        for (T value : filteredItemsProperty.get()) {
            if (textInput.equals(value.toString())) {
                return value;
            }
        }

        return null;
    }

    public void setFilteredListProperty(ObjectProperty<ObservableList<T>> filteredItemsProperty) {
        this.filteredItemsProperty = filteredItemsProperty;
    }
}