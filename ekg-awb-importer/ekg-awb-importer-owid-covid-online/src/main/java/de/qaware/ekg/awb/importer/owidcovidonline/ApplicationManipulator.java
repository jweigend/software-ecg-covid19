package de.qaware.ekg.awb.importer.owidcovidonline;

import de.qaware.ekg.awb.common.ui.explorer.ExplorerController;
import de.qaware.ekg.awb.common.ui.explorer.RootItem;
import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.explorer.ui.items.common.ProjectItem;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryItem;
import de.qaware.ekg.awb.project.ui.projectbar.ProjectBar;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.windows.DefaultApplicationWindow;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods, which removes/hide UI-Elements from the application window.
 * For the Corona-Edition, we want a simple UI, therefore, we disable some configurations, menu entries or managing of projects.
 */
public class ApplicationManipulator {
    // Elements can be found in awb -> ProjectBarView.fxml and Window.fxml
    private static final List<String> ALLOWED_MENU_ITEMS = List.of("Quit", "Restore Default Layout", "About Software-EKG", "Import CSV File...");
    private static final List<String> ALLOWED_PROJECT_BAR_NODE_ITEM_LABELS = List.of("Selected project:");


    /**
     * Remove all menu entries, which should not used by the corona edition.
     */
    public void removeMenuEntries() {
        ApplicationWindow windows = EkgLookup.lookup(DefaultApplicationWindow.class);
        MenuBar menuBar = (MenuBar) windows.getStage().getScene().lookup("#menuBar");

        menuBar.getMenus().stream()
                .flatMap(m -> m.getItems().stream())
                // filter out all allowed menu items
                .filter(m -> !(m.getText() != null && ALLOWED_MENU_ITEMS.contains(m.getText())))
                // disable the view for the not allowed items.
                .forEach(m -> m.setVisible(false));
    }

    /**
     * Disables the context menue on some items in the explorer. So it is not possible to delete or change settings of a project.
     */
    public void disableRightClickInExplorer() {
        ExplorerController explorer = EkgLookup.lookup(ExplorerController.class);

        RemoveContextMenuOnItemsInExplorer contextMenuRemover = new RemoveContextMenuOnItemsInExplorer();

        AbstractItem<?> root = explorer.getRootItem();
        // remove the context menu from root
        contextMenuRemover.removeContextMenu(root);
    }

    /**
     * The project bar is simplified, to only show the selected project.
     * The information about platform, cloud is removed.
     */
    public void manipulateProjectBar() {
        ApplicationWindow windows = EkgLookup.lookup(DefaultApplicationWindow.class);
        ProjectBar projectBar = (ProjectBar) windows.getStage().getScene().lookup("#projectBar");

        List<Node> toRemoveNodes = new ArrayList<>();
        for (Node node : projectBar.getChildren()) {
            if (node instanceof Label) {
                String labelText = ((Label) node).getText();
                if (!(labelText != null && ALLOWED_PROJECT_BAR_NODE_ITEM_LABELS.contains(labelText))) {
                    // removed nodes, which are not allowed
                    toRemoveNodes.add(node);
                }
            } else if (!(node instanceof HBox)) {
                // removes all nodes, which are not Label or HBox
                toRemoveNodes.add(node);
            }
        }
        // do the removing
        projectBar.getChildren().removeAll(toRemoveNodes);

        // remove the Button to administrate the project, which is in the HBox
        projectBar.getChildren().stream()
                .filter(HBox.class::isInstance)
                .findFirst()
                .ifPresent(hBoxNode -> ((HBox) hBoxNode).getChildren().removeIf(Button.class::isInstance));

    }

    /**
     * Removes the context menu for RootItem, RepositoryItem and ProjectItem.
     * Sometimes the childItems are not set, so we must listen, that the items are added.
     */
    private static class RemoveContextMenuOnItemsInExplorer implements ListChangeListener<TreeItem> {

        @Override
        public void onChanged(Change<? extends TreeItem> change) {
            for (TreeItem item : change.getList()) {
                removeContextMenu(item);
            }
        }

        private void removeContextMenu(TreeItem item) {
            if (item instanceof RootItem || item instanceof RepositoryItem) {
                clearContextMenu((AbstractItem) item);

                // remove contextMenu also for their childrens (is RepositoryItem)
                item.getChildren().forEach(i -> removeContextMenu((TreeItem) i));
                item.getChildren().addListener(this);
            } else if (item instanceof ProjectItem) {
                clearContextMenu((AbstractItem) item);
            }
        }

        private void clearContextMenu(AbstractItem item) {
            item.getContextMenu().getItems().clear();
        }
    }

}
