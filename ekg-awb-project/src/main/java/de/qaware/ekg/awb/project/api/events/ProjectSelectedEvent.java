package de.qaware.ekg.awb.project.api.events;

import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

/**
 * Event Object that will use to communicate the switch from one project to another
 */
public class ProjectSelectedEvent extends ProjectPropertyChangeEvent {

    private ProjectViewFlavor activeViewFlavor;

    private EkgRepository repository;

    /**
     * Constructs a new ProjectSelectedEvent instance that will
     * transport the given entities to all event listener.
     *
     * @param source the source component which fires the event
     * @param previousProject the previous selected project if exists. Otherwise null.
     * @param newProject the new selected project
     * @param activeViewFlavor the view flavor that is chosen for the selected project
     * @param repository the EKG repository which stores the selected project
     */
    public ProjectSelectedEvent(Object source, Project previousProject, Project newProject,
                                ProjectViewFlavor activeViewFlavor, EkgRepository repository) {
        super(source, newProject,"Project", previousProject, newProject);
        this.activeViewFlavor = activeViewFlavor;
        this.repository = repository;
    }

    public Project getSelectedProject() {
        return (Project) getNewValue();
    }

    public Project getPreviousProject() {
        return (Project) getOldValue();
    }

    public ProjectViewFlavor getActiveViewFlavor() {
        return activeViewFlavor;
    }

    public EkgRepository getRepository() {
        return repository;
    }
}
