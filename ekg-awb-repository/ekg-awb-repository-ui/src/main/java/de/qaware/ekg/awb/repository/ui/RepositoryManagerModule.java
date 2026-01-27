package de.qaware.ekg.awb.repository.ui;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.events.RepositoryModifyEvent;
import de.qaware.ekg.awb.repository.api.types.Embedded;
import de.qaware.ekg.awb.repository.ui.admin.EkgRepositoryAdmin;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;

import javax.inject.Inject;

public class RepositoryManagerModule implements Module {

    @Inject
    @Embedded
    private EkgRepository repository;

    @Override
    public void preload() throws PlatformException {

        EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);
        eventBus.subscribe(RepositoryModifyEvent.class, event
                -> new EkgRepositoryAdmin().handleModifyRepositoryEvent((RepositoryModifyEvent) event));
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        repository.close();
    }
}
