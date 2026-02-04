//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.explorer;


import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for the explorerView view.
 */
@Singleton
public class ExplorerController implements Initializable {

    /**
     * Defines the cell size for the metric tree.
     */
    private static final int DEFAULT_CELL_SIZE = 22;

    /**
     * The metric tree control
     */
    @FXML
    private TreeView explorerTree;


    private AbstractItem<?> rootItem;

    /**
     * Initialize the controller
     *
     * @param location  - the location url
     * @param resources - the resources
     */
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        explorerTree.setCellFactory(new TreeViewTreeCellFactory<>(new KeyNavigationHandler<>(this.explorerTree)));
        explorerTree.setShowRoot(true);
        explorerTree.setFixedCellSize(DEFAULT_CELL_SIZE);
    }

    /**
     * Set the root item.
     *
     * @param rootItem the new root item.
     */
    @SuppressWarnings("unchecked")
    public void setRootItem(AbstractItem<?> rootItem) {
        this.rootItem = rootItem;
        explorerTree.setRoot(this.rootItem);
    }

    public AbstractItem<?> getRootItem() {
        return rootItem;
    }

    public TreeView getExplorerTree() {
        return explorerTree;
    }

    /**
     * Subscribe this class to listen on the explorerView update event, to update the tree items
     *
     * @param updateEvent update event
     * @return true
     */
    @EkgEventSubscriber(eventClass = ExplorerUpdateEvent.class)
    public boolean updateExplorer(@Observes ExplorerUpdateEvent updateEvent) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateExplorer(updateEvent));
            return true;
        }

        if (updateEvent.getSource() instanceof AbstractItem) {
            // concrete types was given
            AbstractItem root = (AbstractItem) updateEvent.getSource();

            root.getService().restart();
            return true;

       } else if (updateEvent.getSource() instanceof String) {

            String nodeId = ((String) updateEvent.getSource()).toLowerCase();

            // something else was given an we will update the project node that was updated
            AbstractItem root = (AbstractItem) explorerTree.getRoot();
            for (Object childItem : root.getFilteredChildren(nodeId)) {
                ((AbstractItem<?>)childItem).getService().restart();
                ((AbstractItem<?>)childItem).setExpanded(true);
            }

            return true;
        }

        // Source is tree item, use the tree item

        // something else was given an we will update the project node that was updated
        TreeItem project = explorerTree.getRoot();
        ((AbstractItem) project).getService().restart();
        project.setExpanded(true);

        return true;
    }
}
