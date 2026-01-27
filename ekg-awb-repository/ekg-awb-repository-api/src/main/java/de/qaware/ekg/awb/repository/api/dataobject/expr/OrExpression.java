package de.qaware.ekg.awb.repository.api.dataobject.expr;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A boolean OR {@link Expression} (disjunction).
 */
public final class OrExpression implements Expression {
    private final List<Expression> expressions;

    /* package-private */ OrExpression(List<Expression> expressions) {
        this.expressions = new ArrayList<>(expressions);
    }

    /**
     * Returns the {@link Expression}s that will be OR-connected.
     *
     * @return the {@link Expression}s
     */
    public List<Expression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrExpression that = (OrExpression) o;
        return Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }
}
