package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common.BookmarkGroupItem;

import java.util.List;

/**
 * TreeItem builder that produces BookmarkGroupItem instances.
 * The builder creates a BookmarkGroup item for each group that exists in the repository
 * but not for the virtual group "_BOOKMARK_GLOBAL_".
 */
public class BookmarkGroupItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    private static final String ITEM_DESC = "BookmarkGroups";

    @Override
    protected List<BookmarkGroupItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        MetricsBookmarkService bookmarkService = item.getRepository().getBoundedService(MetricsBookmarkService.class);

        List<BookmarkGroup> bookmarkGroups = bookmarkService.getBookmarkGroups();
        int amountOfGroups = bookmarkGroups.size();

        return ItemBuilder.getItems(
                bookmarkGroups.stream(),
                bookmarkGroup -> new BookmarkGroupItem(bookmarkGroup, null, item.getRepository()),
                notifier,
                amountOfGroups,
                ITEM_DESC
        );
    }
}