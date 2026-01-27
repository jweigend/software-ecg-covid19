//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.explorer.api;

import javafx.concurrent.Service;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Stores all item builder for a defined parent item type to create the tree view within the explorer view.
 * <p>
 * An item builder is a function that returns the children items for a defined parent item type within the explorer
 * tree. For example there are two types of items: AItem and BItem and AItem is the parent item of BItem. Then an item
 * builder for {@code parentItem=AItem.class} must be registered that creates the BItem objects.
 * <p>
 * When executing the item builder for a concrete item, the item builder get at least this item.
 * <p>
 * <pre>
 *     ItemBuilderRegistry registry = // Get the ItemBuilderRegistry i.e. by using Lookup.lookup(...)
 *     registry.registerItemBuilder(AItem.class, parent -> {
 *         // Build a list with the child items of parent i.e.
 *         return Arrays.asList(new BItem(parent.getContext()), new BItem(parent.getContext()));
 *     });
 * </pre>
 */
public interface ItemBuilderRegistry {

    /**
     * Register a new item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    <T extends AbstractItem> void registerItemBuilder(Class<? extends T> parentItem,
                                                      Function<T, List<? extends AbstractItem>> builder);

    /**
     * Register a new item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    <T extends AbstractItem> void registerItemBuilder(Class<? extends T> parentItem,
                                                      BiFunction<T, ProgressNotifier, List<? extends AbstractItem>> builder);

    /**
     * Remove an item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    <T extends AbstractItem> void unRegisterItemBuilder(Class<? extends T> parentItem,
                                                        Function<T, List<? extends AbstractItem>> builder);

    /**
     * Remove an item builder.
     *
     * @param parentItem The type of the parent items.
     * @param builder    The builder to build the child items.
     * @param <T>        The type of the parent item.
     */
    <T extends AbstractItem> void unRegisterItemBuilder(Class<? extends T> parentItem,
                                                        BiFunction<T, ProgressNotifier, List<? extends AbstractItem>> builder);

    /**
     * @return a set with all registered parent item types.
     */
    Set<Class<? extends AbstractItem>> getRegisteredParentItems();

    /**
     * Get a service instance to build the child items for a specific item.
     *
     * @param parentItem The item to get the builder service.
     * @param <T>        The type of the parent item.
     * @return The builder service for the given item.
     */
    <T extends AbstractItem> Service<List<AbstractItem>> getServiceFor(T parentItem);
}
