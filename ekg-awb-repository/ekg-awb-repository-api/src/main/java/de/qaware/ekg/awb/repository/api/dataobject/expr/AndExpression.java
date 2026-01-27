package de.qaware.ekg.awb.repository.api.dataobject.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A boolean AND {@link Expression} (conjunction).
 */
public final class AndExpression implements Expression {
    private final List<Expression> expressions;

    /* package-private */ AndExpression(List<Expression> expressions) {
        this.expressions = new ArrayList<>(expressions);
    }

    /**
     * Returns the {@link Expression}s that will be AND-connected.
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
        AndExpression that = (AndExpression) o;
        return Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }
}
