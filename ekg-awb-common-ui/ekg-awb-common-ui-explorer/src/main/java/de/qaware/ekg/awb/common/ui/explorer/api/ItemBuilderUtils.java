//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.explorer.api;


import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Some utils for common used item builders.
 */
public final class ItemBuilderUtils {

    private ItemBuilderUtils() {
    }

    /**
     * Modify the given item builder that executes the builder when the condition is true. The builder may sends some
     * progress updates while executing.
     * <p>
     * The builder will return an empty list if the condition is false.
     *
     * @param builder   the builder executed when the condition is true.
     * @param condition the condition to check
     * @param <T>       the type of the parent item
     * @return the conditional builder
     */
    public static <T extends AbstractItem> BiFunction<T, ProgressNotifier, List<? extends AbstractItem>>
    conditionalBuilder(BiFunction<T, ProgressNotifier, List<? extends AbstractItem>> builder, Predicate<T> condition) {
        return (parentItem, notifier) -> {
            if (condition.test(parentItem)) {
                return builder.apply(parentItem, notifier);
            } else {
                return Collections.emptyList();
            }
        };
    }

    /**
     * Modify the given item builder that executes the builder when the condition is true. The builder did not send
     * progress updates while executing.
     * <p>
     * The builder will return an empty list if the condition is false.
     *
     * @param builder   the builder executed when the condition is true.
     * @param condition the condition to check
     * @param <T>       the type of the parent item
     * @return the conditional builder
     */
    public static <T extends AbstractItem> BiFunction<T, ProgressNotifier, List<? extends AbstractItem>>
    conditionalBuilder(Function<T, List<? extends AbstractItem>> builder, Predicate<T> condition) {
        return (item, notifier) -> {
            if (condition.test(item)) {
                return builder.apply(item);
            } else {
                return Collections.emptyList();
            }
        };
    }

    /**
     * Create a new item builder from a single item under a condition.
     * <p>
     * The builder returns the built item if the condition is true, otherwise it returns an empty list.
     *
     * @param builder   the builder for the single item, executed when the condition is true.
     * @param condition the condition to check.
     * @param <T>       the type for the parent item.
     * @return the conditional builder.
     */
    public static <T extends AbstractItem> BiFunction<T, ProgressNotifier, List<? extends AbstractItem>>
    singleConditionalBuilder(Function<T, ? extends AbstractItem> builder, Predicate<T> condition) {
        return conditionalBuilder(singleBuilder(builder), condition);
    }

    /**
     * Create a new item builder from a single item.
     *
     * @param builder the builder for the single item
     * @param <T>     the type for the parent item.
     * @return the created builder
     */
    public static <T extends AbstractItem> BiFunction<T, ProgressNotifier, List<? extends AbstractItem>>
    singleBuilder(Function<T, ? extends AbstractItem> builder) {
        return (item, notifier) -> Collections.singletonList(builder.apply(item));
    }
}
