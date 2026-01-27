package de.qaware.ekg.awb.explorer.ui;

import de.qaware.ekg.awb.common.ui.explorer.api.TreeIconsProvider;
import de.qaware.ekg.awb.explorer.ui.items.common.ProjectItem;
import javafx.scene.Node;

/**
 * Interface that represent a specialized variant of TreeIconsProvider
 * that has the context of EKG Explorer tree with it Workspace, Repository and
 * Project tree nodes.
 *
 * @see TreeIconsProvider for more information
 */
public interface ExplorerTreeIconsProvider extends TreeIconsProvider {

    /**
     * Returns the node icon that represents a ProjectItem in
     * the explorer tree.
     *
     * @param item the ProjectItem this icon will assigned to, to enable node specific icons
     * @return the icon as Node instance that can assigned to the specified TreeItem
     */
    Node getProjectItemIcon(ProjectItem item);
}
