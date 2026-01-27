package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common;

import de.qaware.ekg.awb.common.ui.view.AppWindowProvider;
import de.qaware.ekg.awb.commons.beans.BeanProvider;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.BookmarkGroupEvent;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.CreateBookmarkGroupDialog;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.UUID;

/**
 * This is the root item (parent node) of all BookmarkItems / BookmarkGroupItems that belongs
 * to a specific repository. It is a section to group the child elements and doesn't define any
 * bookmark data itself.
 */
@SuppressWarnings({"CdiManagedBeanInconsistencyInspection", "CdiInjectionPointsInspection"})
public class ReproBookmarksItem extends ContextAwareItem {

    @Inject
    private Event<BookmarkGroupEvent> bookmarkEvent;

    @Inject
    private AppWindowProvider ownerWindowProvider;

    @Inject
    private static EkgEventBus eventBus;

    /**
     * Creates a new ReproBookmarksItem item based on the given context and the project
     *
     * @param label      the name of the tree item that will used as displayed label in the view
     * @param context    bean that define filters that relating to series data or the view flavor and control the item rendering
     * @param repository the EKG repository the bookmark section item belongs to.
     */
    public ReproBookmarksItem(String label, FilterContext context, EkgRepository repository) {
        super(label, context, repository);
        super.setGraphic(getIconProvider().getReproBookmarksItemIcon(this));
        BeanProvider.injectFields(this);

        addContextMenuEntry("Create bookmark group", event -> createBookmarkGroup());
    }

    @Override
    protected int getOrderPriority() {
        return 20;
    }

    private void createBookmarkGroup() {

        CreateBookmarkGroupDialog dialog = new CreateBookmarkGroupDialog(getRepository());
        dialog.initOwner(ownerWindowProvider.getAppWindow());

        dialog.showAndWait().ifPresent(bookmarkGroup -> {
            MetricsBookmarkService service = getRepository().getBoundedService(MetricsBookmarkService.class);
            bookmarkGroup.setId(UUID.randomUUID().toString());
            service.persistNewBookmarkGroup(bookmarkGroup);

            eventBus.publish(new ExplorerUpdateEvent(this, getRepository()));
        });
    }
}
