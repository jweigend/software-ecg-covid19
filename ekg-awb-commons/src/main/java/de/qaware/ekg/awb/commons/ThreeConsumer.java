package de.qaware.ekg.awb.commons;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts three input arguments and returns no
 * result.  This is the three-arity specialization of {@link Consumer}.
 * Unlike most other functional interfaces, {@code ThreeConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <R> the type of the second argument to the operation
 * @param <U> the type of the third argument to the operation
 *
 * @see Consumer
 */
@FunctionalInterface
public interface ThreeConsumer<T, R, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param firstArg the first input argument
     * @param secArg the second input argument
     * @param thirdArg the third input argument
     */
    void accept(T firstArg, R secArg, U thirdArg);

    /**
     * Returns a composed {@code ThreeConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ThreeConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default ThreeConsumer<T, R, U> andThen(ThreeConsumer<? super T, ? super R, ? super U> after) {
        Objects.requireNonNull(after);

        return (l, r, u) -> {
            accept(l, r, u);
            after.accept(l, r, u);
        };
    }
}
