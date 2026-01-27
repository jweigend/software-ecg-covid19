package de.qaware.ekg.awb.explorer.ui;

import de.qaware.ekg.awb.common.ui.explorer.ExplorerController;
import de.qaware.ekg.awb.common.ui.explorer.RootItem;
import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.explorer.ui.items.common.ProjectItem;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.project.api.ProjectViewFlavorChangedEvent;
import de.qaware.ekg.awb.project.api.events.ProjectSelectedEvent;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;

import javax.inject.Singleton;

/**
 * A controller extension that will add some additional functionality to the common tree explorer controller.
 * In contrast to the plain controller of the common explorer tree this implementation will know and implement
 * all the EKG specific domain logic like domain specific TreeItems, extended event handling and custom context menus.
 */
@Singleton
public class ExplorerExtendedController {

    private static final Logger LOGGER = EkgLogger.get();

    private ExplorerController commonTreeController;

    private TreeView explorerTree;

    private EkgEventBus eventBus;

    /**
     *
     * @param commonController an ExplorerController instance that controls the ExplorerTree view
     */
    public void initWithCommonController(ExplorerController commonController) {
        this.commonTreeController = commonController;
        this.explorerTree = commonTreeController.getExplorerTree();
        this.eventBus = EkgLookup.lookup(EkgEventBus.class);

        initHandlerFunctions();
    }

    /**
     * Init the extended functionality
     */
    public void initHandlerFunctions() {
        //noinspection unchecked
        explorerTree.getSelectionModel().selectedItemProperty().addListener(new ProjectSelectionListener());
    }

    @EkgEventSubscriber(eventClass = ExplorerUpdateEvent.class)
    public void handleExplorerUpdateEvent(ExplorerUpdateEvent event) {

    }


    @SuppressWarnings("unchecked")
    @EkgEventSubscriber(eventClass = ProjectViewFlavorChangedEvent.class)
    public void changeProject(ProjectViewFlavorChangedEvent event) {

        AbstractItem treeItem = commonTreeController.getRootItem();
        Project relatedProject = event.getRelatedProject();
        ProjectItem relatedProjectItem = findProjectItemByProject(relatedProject, treeItem);

        // check if project exits in the tree - this should always be the case
        if (relatedProjectItem == null) {
            LOGGER.error("unable to find project item in explorer tree for project " + relatedProject.getName());
            return;
        }

        // we only continue if the view flavor has really changed
        if (relatedProjectItem.getActiveViewFlavor() == event.getNewViewFlavor()) {
            return;
        }

        int selectionIndexForProject = commonTreeController.getExplorerTree().getRow(relatedProjectItem);
        commonTreeController.getExplorerTree().getSelectionModel().select(selectionIndexForProject);
        relatedProjectItem.changeProjectViewFlavor(event);

        commonTreeController.getExplorerTree().getSelectionModel().getSelectedItem();
    }

    /**
     * An ChangeListener implementation that will bind to tree selection and will
     * resolve the selected project that belong to a node (TreeItem) clicked by the user
     */
    public class ProjectSelectionListener implements ChangeListener {

        @Override
        public void changed(ObservableValue observable, Object previousSelection, Object selectedItem) {

            // we will only get RootItems above the project tree items - in this case there is no project change
            if (selectedItem instanceof RootItem) {
                return;
            }

            RepositoryBaseItem previousItem = null;
            if (!(previousSelection instanceof RootItem)) {
                previousItem = (RepositoryBaseItem) previousSelection;
            }

            ProjectItem previousProjectItem = resolveProjectItem(previousItem);
            Project oldProject = previousProjectItem != null ? previousProjectItem.getProject() : null;

            ProjectItem selectedProjectItem = resolveProjectItem((RepositoryBaseItem) selectedItem);
            Project newProject = selectedProjectItem != null ? selectedProjectItem.getProject() : null;

            if (newProject == null || newProject.equals(oldProject)) {
                return;
            }

            LOGGER.info("new project selected " + newProject.getId());
            eventBus.publish(new ProjectSelectedEvent(selectedItem, oldProject, newProject,
                    selectedProjectItem.getActiveViewFlavor(), ((RepositoryBaseItem) selectedItem).getRepository()));
        }

        /**
         * Traverse the tree path to the root to resolve the ProjectItem
         * node that contains the selected tree-node (or is itself the selected one).
         *
         * @param selectedItem the selected TreeItem of the explorer view
         * @return null if a selected item is null or a types/root node or the resolved ProjectItem
         */
        private ProjectItem resolveProjectItem(RepositoryBaseItem selectedItem) {
            AbstractItem parent = selectedItem;

            while (parent != null && !(parent instanceof ProjectItem)) {
                parent = (AbstractItem) parent.getParent();
            }

            return (ProjectItem) parent;
        }
    }

    private ProjectItem findProjectItemByProject(Project lookupProject, AbstractItem treeItem) {

        if (lookupProject == null) {
            LOGGER.error("no project was given, abort and return null as ProjectItem");
            return null;
        }

        if (treeItem instanceof ProjectItem && lookupProject.equals(((ProjectItem)treeItem).getProject())) {
            return (ProjectItem) treeItem;
        }

        for(Object child : treeItem.getChildren()) {
            if (child instanceof ProjectItem && lookupProject.equals(((ProjectItem)child).getProject())) {
                return (ProjectItem)child;
            }

            ProjectItem projectItem = findProjectItemByProject(lookupProject, (AbstractItem) child);
            if (projectItem != null) {
                return projectItem;
            }
        }

        return null;
    }
}
