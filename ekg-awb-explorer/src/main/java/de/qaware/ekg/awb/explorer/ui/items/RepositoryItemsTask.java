//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.explorer.ui.items;


import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryItem;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryService;
import de.qaware.ekg.awb.repository.api.types.Embedded;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The task to read all available types.
 */
public class RepositoryItemsTask {

    @Inject
    @Embedded
    @SuppressWarnings("CdiInjectionPointsInspection") // will work than bundled together with other EKG components
    private EkgRepository embeddedRepo;

    /**
     * Load the types for the explorerView tree.
     *
     * @return A list with all types items.
     */
    public List<AbstractItem> getRepositoryItems() {

        RepositoryService service = embeddedRepo.getBoundedService(RepositoryService.class);
        List<EkgRepository> repositories = service.listEkgRepositories();
 
        return repositories
                .stream()
                .map(repository -> new RepositoryItem(repository.getRepositoryName(), true, repository))
                .collect(Collectors.toList());
    }
}
