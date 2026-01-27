package de.qaware.ekg.awb.repository.api.dataobject.expr;

import java.util.Objects;

/**
 * A boolean NOT {@link Expression} (negation).
 */
public final class NotExpression implements Expression {
    private final Expression expression;

    /* package-private */ NotExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the {@link Expression} to negate.
     *
     * @return the {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotExpression that = (NotExpression) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }
}
