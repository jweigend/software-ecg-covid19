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
package de.qaware.ekg.awb.common.ui.explorer;

import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.util.Objects;

/**
 * A factory to create {@link TreeCell TreeCells} from {@link AbstractItem AbstractItems}.
 */
class TreeViewTreeCellFactory<V> implements Callback<TreeView<V>, TreeCell<V>> {

    /**
     * Indicates a single mouse-click.
     */
    private static final int SINGLE_CLICK = 1;

    /**
     * Used for indicating a double-click.
     */
    private static final int DOUBLE_CLICK = 2;

    private KeyNavigationHandler<V> keyNavigationHandler;

    /**
     * Init the factory.
     *
     * @param keyNavigationHandler The navigation handler for key strokes.
     */
    public TreeViewTreeCellFactory(KeyNavigationHandler<V> keyNavigationHandler) {
        this.keyNavigationHandler = keyNavigationHandler;
    }

    /**
     * The actual {@link TreeCell} factory.
     *
     * @param treeView The target view for the created cells.
     * @return The new {@link TreeCell}.
     */
    @Override
    public TreeCell<V> call(final TreeView<V> treeView) {
        treeView.setOnKeyReleased(keyNavigationHandler); //Get selected item on tree at key pressed

        final TreeCell<V> treeCell = makeTreeCell();
        treeCell.setOnMouseClicked(e -> onTreeCellClickedHandler(e, treeCell));

        return treeCell;
    }

    private TextFieldTreeCell<V> makeTreeCell() {
        return new TextFieldTreeCell<>() {
            @Override
            public void updateItem(V item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(Objects.toString(getTreeItem().getValue(), "NO TEXT"));
                    setGraphic(getTreeItem().getGraphic());
                }
            }
        };
    }

    /**
     * MouseEvent-Handler reacting to clicks on the TreeCells.
     *
     * @param mouseEvent The caught mouse event.
     * @param treeCell   the TreeCell which was clicked.
     */
    @SuppressWarnings("unchecked")
    protected void onTreeCellClickedHandler(final MouseEvent mouseEvent, final TreeCell treeCell) {
        if (!(treeCell.getTreeItem() instanceof AbstractItem)) {
            return;
        }

        AbstractItem<V> item = (AbstractItem<V>) treeCell.getTreeItem();

        if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
            item.getDoubleClickHandler().handle(mouseEvent);

        } else if (mouseEvent.getClickCount() == SINGLE_CLICK) {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                item.getSingleClickHandler().handle(mouseEvent);

            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                treeCell.setContextMenu(item.getContextMenu());
            }
        }
    }
}
