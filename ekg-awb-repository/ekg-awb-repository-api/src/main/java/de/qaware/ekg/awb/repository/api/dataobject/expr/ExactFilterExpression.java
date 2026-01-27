package de.qaware.ekg.awb.repository.api.dataobject.expr;

import de.qaware.ekg.awb.repository.api.schema.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@link Expression} to filter {@link Field}s for exact values. See
 * {@link ExprFactory#exactFilter(Field, String...)}.
 */
public final class ExactFilterExpression implements Expression {
    private final Field field;
    private final List<String> values;

    public ExactFilterExpression(Field field, List<String> values) {
        this.field = field;
        this.values = new ArrayList<>(values);
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
     * Returns the values to filter.
     *
     * @return the values
     */
    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExactFilterExpression that = (ExactFilterExpression) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, values);
    }
}
