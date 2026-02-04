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

import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.core.utils.ObjectUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The abstract basic item for explorer view items.
 *
 * @param <T> The type of the {@link #getValue() value} property within TreeItem.
 */
public abstract class AbstractItem<T> extends TreeItem<T> implements Comparable<AbstractItem<T>> {

    // an ObservableList list of TreeItem elements that will populate the tree view.
    // This member is redundant to the "children" member of super class that couldn't accessed by clients via API
    final private ObservableList<TreeItem<T>> sourceList = FXCollections.observableArrayList();

    private ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<>();

    private ContextMenu contextMenu;

    private Service<ObservableList<AbstractItem>> service;

    private String id = super.toString();

    private boolean alreadyLoad = false;

    /**
     * Creates a AbstractItem with the value property set to the provided object and it initializes the context menu for
     * the current item.
     *
     * @param value The object to be stored as the value of this AbstractItem.
     */
    public AbstractItem(T value) {
        super(value);

        //Ensure we are the ui thread.
        Platform.runLater(() -> contextMenu = new ContextMenu());
    }

    /**
     * Returns the list of children that is backing the filtered list.
     * This accessor is redundant to the getChildren of the super class. In contrast
     * to that one this is one doesn't perform additional (expensive logic)
     *
     * @return underlying list of children
     */
    public ObservableList<TreeItem<T>> getInternalChildren() {

        try {
            //noinspection JavaReflectionMemberAccess
            Field field = TreeItem.class.getDeclaredField("children");
            field.setAccessible(true);
            Object value = field.get(this);
            //noinspection unchecked
            return (ObservableList) value;
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Returns a filtered set of {@link AbstractItem} child's of this node that have the same
     * item id like the given one. Normally only one item should returned.
     *
     * @param itemId the item id to look for
     * @return a set of AbstractItem instances
     */
    public Set<AbstractItem<T>> getFilteredChildren(String itemId) {

        Set<AbstractItem<T>> resultSet = new HashSet<>();

        Collection<TreeItem<T>> childs = this.getInternalChildren();

        if (childs == null) {
            return resultSet;
        }

        for (TreeItem<T> child : this.getInternalChildren()) {
            AbstractItem<T> abstractChildItem = (AbstractItem<T>) child;
            if (itemId.equals(abstractChildItem.getId())) {
                resultSet.add(abstractChildItem);
            }

            resultSet.addAll(abstractChildItem.getFilteredChildren(itemId));
        }

        return resultSet;
    }

    /**
     * @return the predicate
     */
    public final TreeItemPredicate<T> getPredicate() {
        return this.predicate.get();
    }

    public ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
        return predicate;
    }

    /**
     * Set the predicate
     *
     * @param predicate the predicate
     */
    public final void setPredicate(TreeItemPredicate<T> predicate) {
        this.predicate.set(predicate);
    }

    /**
     * The children of this TreeItem. This method is called frequently, and
     * it is therefore recommended that the returned list be cached by
     * any TreeItem implementations.
     *
     * @return a list that contains the child TreeItems belonging to the TreeItem.
     */
    @Override
    public ObservableList<TreeItem<T>> getChildren() {
        if (!alreadyLoad) {
            alreadyLoad = true;
            getService().restart();
        }
        return super.getChildren();
    }

    /**
     * Returns the unique id of the tree item (node)
     *
     * @return the unique id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id of the tree item
     *
     * @param id the unique id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Flag to check if the current item is a leaf
     *
     * @return false
     */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /**
     * Get the enter pressed handler.
     *
     * @return the enter pressed handler.
     */
    public EventHandler<KeyEvent> getEnterPressedHandler() {
        return e -> {
            // Do nothing
        };
    }

    /**
     * Get the delete pressed handler.
     *
     * @return the delete pressed handler.
     */
    public EventHandler<KeyEvent> getDeletePressedHandler() {
        return e -> {
            // Do nothing
        };
    }

    /**
     * Get the single click handler.
     *
     * @return the single click handler.
     */
    public EventHandler<MouseEvent> getSingleClickHandler() {
        return e -> {
            // Do nothing
        };
    }

    /**
     * Get the double click handler.
     *
     * @return the double click handler.
     */
    public EventHandler<MouseEvent> getDoubleClickHandler() {
        return e -> {
            // Do nothing
        };
    }

    /**
     * Adds a context menu entry
     *
     * @param command - the name of the command
     * @param action  - the action for the command
     */
    public void addContextMenuEntry(final String command, final EventHandler<ActionEvent> action) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> addContextMenuEntry(command, action));
            return;
        }
        MenuItem item = new MenuItem(command);
        item.setOnAction(action);
        contextMenu.getItems().add(item);
    }

    /**
     * Gets the context menu of the item
     *
     * @return the context menu
     */
    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    /**
     * Get the service to resolve the children.
     *
     * @return the service to resolve the children.
     */
    @SuppressWarnings("unchecked")
    public Service getService() {
        if (service == null) {
            service = createService();
            service.progressProperty().addListener((o, ov, nv) -> EkgLookup.lookup(EkgEventBus.class)
                    .publish(new ProgressEvent(service.getMessage(), service.getProgress(), this)));
            service.setOnSucceeded(e -> {
                getChildren().clear();
                getChildren().addAll((Collection<? extends TreeItem<T>>) e.getSource().getValue());
            });
        }
        return service;
    }

    /**
     * Get the priority how to order different types of items.
     * <p>
     * The default value for the order priority is 100.
     *
     * @return the defined order priority.
     */
    protected int getOrderPriority() {
        return 100;
    }

    @Override
    public int compareTo(AbstractItem<T> o) {
        if (this.getOrderPriority() != o.getOrderPriority()) {
            return Integer.compare(this.getOrderPriority(), o.getOrderPriority());
        } else {
            return ObjectUtils.mapOrDefaultIfNull(getValue(), "", Object::toString)
                    .compareToIgnoreCase(ObjectUtils.mapOrDefaultIfNull(o.getValue(), "", Object::toString));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractItem)) {
            return false;
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        AbstractItem<?> that = (AbstractItem<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getOrderPriority())
                .toHashCode();
    }

    private Service createService() {
        return EkgLookup.lookup(ItemBuilderRegistry.class).getServiceFor(this);
    }
}
