package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common.ReproBookmarksItem;

import java.util.List;

/**
 * TreeItem builder that produces ReproBookmarksItem instances.
 * This item instance (should a single instance per repository) is just an empty
 * tree node with the visual style of bookmarks, that acts as parent for
 * the bookmark groups or (if assigned to global space) the bookmarks itself.
 */
public class ReproBookmarksItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    private static final String ITEM_DESC = "Bookmarks";

    @Override
    protected List<ReproBookmarksItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {
        return List.of(new ReproBookmarksItem(ITEM_DESC, context, item.getRepository()));
    }
}