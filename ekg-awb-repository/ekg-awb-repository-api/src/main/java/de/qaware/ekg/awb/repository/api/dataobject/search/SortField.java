package de.qaware.ekg.awb.repository.api.dataobject.search;

import de.qaware.ekg.awb.repository.api.schema.Field;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * Encapsulate the sort information for a search query.
 */
public class SortField {

    private final Field field;
    private final SortMode mode;

    /**
     * Instantiate a new sort info.
     *
     * @param field the field
     * @param mode  the mode
     */
    public SortField(Field field, SortMode mode) {
        Validate.notNull(field);
        Validate.notNull(mode);
        this.field = field;
        this.mode = mode;
    }

    public Field getField() {
        return field;
    }

    public SortMode getMode() {
        return mode;
    }

    /**
     * The sorting order.
     */
    public enum SortMode {
        /**
         * Ascending order.
         */
        ASC,
        /**
         * Descending order.
         */
        DESC;

        @Override
        public String toString() {
            return StringUtils.lowerCase(name());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SortField sortField = (SortField) o;
        return Objects.equals(field, sortField.field) &&
                mode == sortField.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, mode);
    }
}
