package de.qaware.ekg.awb.repository.api.dataobject.expr;

import de.qaware.ekg.awb.repository.api.schema.Field;

import java.util.Objects;

/**
 * {@link Expression} to filter searchable {@link Field}s for text. See
 * {@link ExprFactory#fullTextFilter(Field, String)}.
 */
public final class FullTextFilterExpression implements Expression {
    private final Field field;
    private final String value;

    /* package-private */ FullTextFilterExpression(Field field, String value) {
        this.field = field;
        this.value = value;
    }

    /**
     * Returns the {@link Field} on which to filter.
     *
     * @return the {@link Field}
     */
    public Field getField() {
        return field;
    }

    /**
     * Returns the text value to search.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullTextFilterExpression that = (FullTextFilterExpression) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value);
    }
}
