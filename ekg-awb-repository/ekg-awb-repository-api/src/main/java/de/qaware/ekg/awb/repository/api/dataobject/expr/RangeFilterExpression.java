package de.qaware.ekg.awb.repository.api.dataobject.expr;

import de.qaware.ekg.awb.repository.api.schema.Field;

import java.util.Objects;

/**
 * {@link Expression} to filter {@link Field}s for ranges. See
 * {@link ExprFactory#numberRangeFilter(Field, Number, Number)}.
 */
public final class RangeFilterExpression implements Expression {
    private final Field field;
    private final String lowerBound;
    private final String upperBound;

    public RangeFilterExpression(Field field, String lowerBound, String upperBound) {
        this.field = field;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
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
     * Returns the lower bound of the range.
     *
     * @return the lower bound
     */
    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the upper bound of the range.
     *
     * @return the upper bound
     */
    public String getUpperBound() {
        return upperBound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeFilterExpression that = (RangeFilterExpression) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(lowerBound, that.lowerBound) &&
                Objects.equals(upperBound, that.upperBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, lowerBound, upperBound);
    }
}
