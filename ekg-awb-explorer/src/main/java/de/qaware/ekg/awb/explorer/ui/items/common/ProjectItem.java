//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.explorer.ui.items.common;

import de.qaware.ekg.awb.explorer.ui.ExplorerTreeIconsProvider;
import de.qaware.ekg.awb.explorer.ui.TreeIconsProviderRegistry;
import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.ProjectViewFlavorChangedEvent;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;

import static de.qaware.ekg.awb.sdk.core.utils.TaskUtils.runAsTask;

/**
 * The explorerView item that shows a specific project.
 */
public class ProjectItem extends RepositoryBaseItem {

    /**
     * The types representation of an workbench project
     */
    private Project project;

    /**
     * The active view flavor with a default state that should be overwritten
     */
    private ProjectViewFlavor activeViewFlavor = ProjectViewFlavor.PHYSICAL_VIEW;


    protected TreeIconsProviderRegistry iconsProviderRegistry = EkgLookup.lookup(TreeIconsProviderRegistry.class);

    /**
     * Creates a new project item based on the given context and the project
     *
     * @param project     - the project
     * @param repository the containing types.
     */
    public ProjectItem(Project project, EkgRepository repository) {
        super(project.getName(), repository);

        ExplorerTreeIconsProvider provider = (ExplorerTreeIconsProvider) iconsProviderRegistry
                .getTreeIconsProvider(project.getImporterId());

        this.project = project;
        super.setGraphic(provider.getProjectItemIcon(this));
        setId(repository.getId() + "_" + project.getName());

        addContextMenuEntry("Delete project and its data",
                event -> runAsTask(
                        "delete-project-" + project.getName() + "-task",
                        () -> {
                            repository.getBoundedService(ProjectDataAccessService.class).deleteProjectByName(project.getName());
                        },
                        finishedEvent -> EkgLookup.lookup(EkgEventBus.class)
                                .publish(new ExplorerUpdateEvent(this.getParent()))
                )
        );
    }

    @EkgEventSubscriber(eventClass = ProjectViewFlavorChangedEvent.class)
    public void changeProjectViewFlavor(ProjectViewFlavorChangedEvent event) {
        if (!project.equals(event.getRelatedProject())) {
            return;
        }

        activeViewFlavor = event.getNewViewFlavor();
        getService().restart();
    }

    @Override
    protected int getOrderPriority() {
        return 10;
    }

    /**
     * Gets the project
     *
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    public ProjectViewFlavor getActiveViewFlavor() {
        return activeViewFlavor;
    }

    public void setActiveViewFlavor(ProjectViewFlavor activeViewFlavor) {
        this.activeViewFlavor = activeViewFlavor;
    }
}