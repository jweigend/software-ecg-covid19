package de.qaware.ekg.awb.repository.api.events;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryDataEvent;
import javafx.stage.Window;

/**
 * An event class that will used by AWB components to request the creation
 * or update/modification of an EKG types.
 */
public class RepositoryModifyEvent extends RepositoryDataEvent {

    private boolean modificationRequested;

    private Window ownerWindow;

    /**
     * Constructs a new RepositoryModifyEvent with
     * modificationRequested = true as fixed setting.
     *
     * @param repository the types a modification is requested for
     * @param ownerWindow the owner window that will be the parent of an modification dialog/modal
     */
    public RepositoryModifyEvent(EkgRepository repository, Window ownerWindow) {
        this(repository, true, ownerWindow);
    }

    public RepositoryModifyEvent(EkgRepository repository, boolean modificationRequested, Window ownerWindow) {
        super(repository);
        this.modificationRequested = modificationRequested;
        this.ownerWindow = ownerWindow;
    }

    public Window getOwnerWindow() {
        return ownerWindow;
    }

    public boolean isModificationRequested() {
        return modificationRequested;
    }

    public EkgRepository getRepository() {
        return (EkgRepository) getSource();
    }
}
