package de.qaware.ekg.awb.explorer.ui.items;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.ProjectItem;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

import java.util.List;

/**
 * ItemBuilder implementation that will use a factory for all TreeItems that belong to workbench
 * but are not a direct part of time-series. The type of items are:
 * <ul>
 *   <li>{@link ProjectItem}</li>
 * </ul>
 */
public class CommonItemBuilder extends ItemBuilder {

    /**
     * Private constructor
     */
    private CommonItemBuilder() {
        // this class should only use in a static way
    }

    /**
     * Build the Project items of explorer for a given parent item.
     *
     * @param parent   the parent item.
     * @param notifier a notifier to show the progress
     * @return the built series items.
     */
    public static List<ProjectItem> buildProjectItems(RepositoryBaseItem parent, ProgressNotifier notifier) {

        ProjectDataAccessService projectService = parent.getRepository().getBoundedService(ProjectDataAccessService.class);

        List<Project> projectList = projectService.listProjects();

        return getItems(
                projectList.stream(),
                project -> {
                    ProjectItem projectItem = new ProjectItem(project, parent.getRepository());

                    if (project.getProjectFlavor() == ProjectFlavor.CLASSIC) {
                        projectItem.setActiveViewFlavor(ProjectViewFlavor.PHYSICAL_VIEW);
                    } else {
                        projectItem.setActiveViewFlavor(ProjectViewFlavor.LOGICAL_VIEW);
                    }

                    return projectItem;
                },
                notifier,
                projectList.size(),
                "EKG Worbench ProjectConfiguration");
    }
}
