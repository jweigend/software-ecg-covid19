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
package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common;

import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.commons.beans.BeanProvider;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.BookmarkEvent;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import static de.qaware.ekg.awb.sdk.core.utils.TaskUtils.runAsTask;

/**
 * Show a  single bookmark within the explorerView tree.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class BookmarkItem extends ContextAwareItem {

    private final Bookmark bookmark;

    @Inject
    private Event<BookmarkEvent> bookmarkEvent;

    /**
     * Creates a BookmarkItem with the given bookmark's name and the context.
     *
     * @param bookmark   the bookmark to show
     * @param context    the context
     * @param repository the containing types.
     */
    public BookmarkItem(Bookmark bookmark, FilterContext context, EkgRepository repository) {
        super(bookmark.getName(), context, repository);
        super.setValue(bookmark.getName());
        super.setGraphic(getIconProvider().getBookmarkItemIcon(this));
        BeanProvider.injectFields(this);
        this.bookmark = bookmark;

        addContextMenuEntry("Load into chart",
                e -> bookmarkEvent.fire(new BookmarkEvent(this, this.bookmark, repository, OpeningMode.CLEAR_VIEW)));

        addContextMenuEntry("Delete bookmark", e -> runAsTask("delete-bookmark", this::deleteBookmark));
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public EventHandler<MouseEvent> getDoubleClickHandler() {
        return e -> bookmarkEvent.fire(new BookmarkEvent(this, bookmark, this.getRepository()));
    }

    @Override
    public EventHandler<KeyEvent> getEnterPressedHandler() {
        return e -> bookmarkEvent.fire(new BookmarkEvent(this, bookmark, this.getRepository()));
    }

    private void deleteBookmark() {
        getRepository().getBoundedService(MetricsBookmarkService.class).deleteBookmark(bookmark);
        Platform.runLater(() -> ((AbstractItem) getParent()).getService().restart());
    }
}
