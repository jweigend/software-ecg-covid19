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
package de.qaware.ekg.awb.common.ui.explorer.impl;

import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.common.ui.explorer.api.ItemBuilderRegistry;
import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The explorerView item builder registry.
 * <p>
 * It stores all item builder for a defined parent item type.
 */
@Singleton
public class ItemBuilderRegistryImpl implements ItemBuilderRegistry {

    public static final String HANDLER_NOT_NULL = "Handler must not be null!";
    private final Map<Class<? extends AbstractItem>, Set<BiFunction<? extends AbstractItem, ProgressNotifier, List<? extends AbstractItem>>>> childItemBuilder = new ConcurrentHashMap<>();

    /**
     * Register a new item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    @Override
    public <T extends AbstractItem> void registerItemBuilder(Class<? extends T> parentItem,
                                                             Function<T, List<? extends AbstractItem>> builder) {
        Objects.requireNonNull(builder, HANDLER_NOT_NULL);
        registerItemBuilder(parentItem, new NoNotifyBuilder<>(builder));
    }

    /**
     * Register a new item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    @Override
    public <T extends AbstractItem> void registerItemBuilder(Class<? extends T> parentItem,
                                                             BiFunction<T, ProgressNotifier, List<? extends AbstractItem>> builder) {
        Objects.requireNonNull(parentItem, "Parent item must not be null!");
        Objects.requireNonNull(builder, HANDLER_NOT_NULL);
        synchronized (childItemBuilder) {
            if (!childItemBuilder.containsKey(parentItem)) {
                childItemBuilder.put(parentItem, new CopyOnWriteArraySet<>());
            }
        }
        childItemBuilder.get(parentItem).add(builder);
    }

    /**
     * Remove an item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    @Override
    public <T extends AbstractItem> void unRegisterItemBuilder(Class<? extends T> parentItem,
                                                               Function<T, List<? extends AbstractItem>> builder) {
        Objects.requireNonNull(builder, HANDLER_NOT_NULL);
        unRegisterItemBuilder(parentItem, new NoNotifyBuilder<>(builder));
    }

    /**
     * Remove an item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    @Override
    public <T extends AbstractItem> void unRegisterItemBuilder(Class<? extends T> parentItem,
                                                               BiFunction<T, ProgressNotifier, List<? extends AbstractItem>> builder) {
        Objects.requireNonNull(parentItem, "Parent item must not be null!");
        Objects.requireNonNull(builder, HANDLER_NOT_NULL);

        if (!childItemBuilder.containsKey(parentItem)) {
            return;
        }
        Set<BiFunction<? extends AbstractItem, ProgressNotifier, List<? extends AbstractItem>>> builders = childItemBuilder.get(parentItem);
        builders.remove(builder);
        synchronized (childItemBuilder) {
            if (builders.isEmpty()) {
                childItemBuilder.remove(parentItem);
            }
        }
    }

    /**
     * @return a set with all registered parent item types.
     */
    @Override
    public Set<Class<? extends AbstractItem>> getRegisteredParentItems() {
        return childItemBuilder.keySet();
    }

    /**
     * Get a service instance to build the child items for a specific item.
     *
     * @param parentItem The item to get the builder service.
     * @param <T>        The type of the parent item.
     * @return The builder service for the given item.
     */
    @Override
    public <T extends AbstractItem> Service<List<AbstractItem>> getServiceFor(T parentItem) {
        return new Service<>() {
            @Override
            protected Task<List<AbstractItem>> createTask() {
                return new ChildrenTask<>(parentItem, ItemBuilderRegistryImpl.this);
            }
        };
    }

    /**
     * Get the item builders for a specific parent item.
     *
     * @param parentItem the parent item class.
     * @return A set with the builders.
     */
    Set<BiFunction<? extends AbstractItem, ProgressNotifier, List<? extends AbstractItem>>> getHandlerFor(Class<? extends AbstractItem> parentItem) {
        Set<BiFunction<? extends AbstractItem, ProgressNotifier, List<? extends AbstractItem>>> handlers
                = childItemBuilder.get(parentItem);
        if (handlers != null) {
            return Collections.unmodifiableSet(handlers);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Wrapper class to wrap a {@link Function} builder into a {@link BiFunction} builder.
     * <p>
     * This class assures that all instances created from the same {@code builder} are equals.
     *
     * @param <T> The real parent item type.
     */
    private static final class NoNotifyBuilder<T extends AbstractItem> implements BiFunction<T, ProgressNotifier, List<? extends AbstractItem>> {
        private Function<T, List<? extends AbstractItem>> builder;

        /**
         * Init a new {@link BiFunction} builder.
         *
         * @param builder The builder function.
         */
        private NoNotifyBuilder(Function<T, List<? extends AbstractItem>> builder) {
            this.builder = builder;
        }

        @Override
        public List<? extends AbstractItem> apply(T t, ProgressNotifier progressNotifier) {
            return builder.apply(t);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NoNotifyBuilder<?> that = (NoNotifyBuilder<?>) o;
            return new EqualsBuilder()
                    .append(builder, that.builder)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(builder)
                    .toHashCode();
        }
    }
}
