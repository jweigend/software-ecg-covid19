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


import de.qaware.ekg.awb.commons.beans.BeanProvider;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryService;
import de.qaware.ekg.awb.repository.api.events.RepositoryModifyEvent;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.types.Embedded;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * The item that shows types within the explorerView tree.
 */
public class RepositoryItem extends RepositoryBaseItem {

    /**
     * The icon of the node in the explorerView tree
     */
    private static final String NODE_ICON = "/icons/repository-icon.svg";

    private static EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);

    private NumberFormat formatter = NumberFormat.getInstance(Locale.GERMAN);

    private String repositoryName;


    /**
     * Constructs a types
     *
     * @param name         - the name of the types
     * @param canBeRemoved - flag to indicate if the types could be removed
     * @param repository   the containing types.
     */
    public RepositoryItem(String name, boolean canBeRemoved, EkgRepository repository) {
        super(name, repository);
        super.setGraphic(createIcon(NODE_ICON));
        setId(repository.getId());

        this.repositoryName = name;

        if (canBeRemoved) {
            RemoveRepositoryHandler removeRepositoryHandler = EkgLookup.lookup(RemoveRepositoryHandler.class);
            removeRepositoryHandler.repository = repository;
            removeRepositoryHandler.repositoryItem = this;

            addContextMenuEntry("Edit repository", e ->
                    eventBus.publish(new RepositoryModifyEvent(getRepository(), getGraphic().getScene().getWindow())));

            addContextMenuEntry("Remove from explorer", removeRepositoryHandler);
        }

        addContextMenuEntry("Reload content", event -> {
                getService().restart();
                updateValueCount(null);
            }
        );

        BeanProvider.injectFields(this);
        updateValueCount(null);
    }

    @EkgEventSubscriber(eventClass = ExplorerUpdateEvent.class)
    public void updateValueCount(ExplorerUpdateEvent event) {
        try {
            long amountValues = getRepository().getRepositoryClient().sumFieldValue(EkgSchemaField.TS_DATA_AMOUNT_VALUES, List.of());

            setValue(repositoryName + " (" + formatter.format(amountValues) + ")");

        } catch (RepositoryException e) {
            // to nothing
        }
    }

    @Override
    protected int getOrderPriority() {
        return getRepository().isEmbedded() ? super.getOrderPriority() - 1 : super.getOrderPriority();
    }

    /**
     * anonymous class to handle the remove event of a types
     * by deleting it from the EKG workbench
     */
    public static class RemoveRepositoryHandler implements EventHandler<ActionEvent> {

        @Inject
        @Embedded
        @SuppressWarnings("CdiInjectionPointsInspection") // will work than bundled together with other EKG components
        private EkgRepository embeddedEkgRepository;

        @Inject
        private Event<ExplorerUpdateEvent> updateEvent;

        /**
         * The types that should removed from EKG workbench
         */
        private EkgRepository repository;

        /**
         * The node in the explorer that represents the types and
         * should updated than finished
         */
        private RepositoryItem repositoryItem;

        @Override
        public void handle(ActionEvent event) {
            RepositoryService service = embeddedEkgRepository.getBoundedService(RepositoryService.class);
            service.deleteRepository(repository);
            //Update service, we can also call createService()
            updateEvent.fire(new ExplorerUpdateEvent(this, repositoryItem));
        }
    }
}
