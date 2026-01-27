package de.qaware.ekg.awb.project.api;

import de.qaware.ekg.awb.project.api.events.ProjectPropertyChangeEvent;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

/**
 * An specialized ProjectPropertyChangeEvent that will used to communicate changes
 * of the view flavor for a specific Project
 */
public class ProjectViewFlavorChangedEvent extends ProjectPropertyChangeEvent {

    /**
     * Constructs a new {@code ProjectViewFlavorChangedEvent}.
     *
     * @param source         the source that as published the event
     * @param relatedProject the project the changed view flavor belongs to
     * @param oldFlavor      the view flavor that was selected before (maybe null)
     * @param newFlavor      the active view flavor for the specified project
     * @throws IllegalArgumentException if {@code source} is {@code null}
     */
    public ProjectViewFlavorChangedEvent(Object source, Project relatedProject, ProjectViewFlavor oldFlavor,
                                         ProjectViewFlavor newFlavor) {

        super(source, relatedProject, ProjectViewFlavor.class.getSimpleName(), oldFlavor, newFlavor);
    }

    public ProjectViewFlavor getNewViewFlavor() {
        return (ProjectViewFlavor) getNewValue();
    }

    public ProjectViewFlavor getPreviousViewFlavor() {
        return (ProjectViewFlavor) getOldValue();
    }
}
