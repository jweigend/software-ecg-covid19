package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common.BookmarkGroupItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common.BookmarkItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common.ReproBookmarksItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * TreeItem builder that produces TreeItem instances of type BookmarkItem.
 * This builder acts with two different contexts:
 * As child of RepoBookmarkGroupItem it serves a list of BookmarkItem's that are assigned to the global bookmark space.
 * As child of BookmarkGroupItem it serves a list of BookmarkItem's that are assigned to this specific bookmark group.
 */
public class BookmarkItemBuilder <T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    private static final String LOADING_MESSAGE = "Loading bookmarks";

    @Override
    protected List<BookmarkItem> getChildItemsFor(T parent, FilterContext context, ProgressNotifier notifier) {

        notifier.updateProgress(LOADING_MESSAGE, -1, 100);

        MetricsBookmarkService service = parent.getRepository().getBoundedService(MetricsBookmarkService.class);

        List<Bookmark> bookmarks;
        if (parent instanceof ReproBookmarksItem) {
            bookmarks = new ArrayList<>(service.getMetricBookmarks(BookmarkGroup.DEFAULT_GLOBAL_GROUP_ID));
        } else {
            bookmarks = new ArrayList<>(service.getMetricBookmarks(((BookmarkGroupItem)parent).getBookmarkGroupId()));
        }

        AtomicInteger i = new AtomicInteger(0);

        ObservableList<BookmarkItem> collect = bookmarks.stream()
                .sorted(Comparator.comparing(Bookmark::getName))
                .map(b -> new BookmarkItem(b, context, parent.getRepository()))
                .peek(b -> notifier.updateProgress(LOADING_MESSAGE, i.getAndIncrement(), bookmarks.size()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        notifier.updateProgress(1, 1);

        return collect;
    }
}


