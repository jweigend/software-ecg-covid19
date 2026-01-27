package de.qaware.ekg.awb.metricanalyzer.ui.bookmarks;

import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;

public class BookmarkCreationResult<T extends Bookmark> {

    private T bookmark;

    private BookmarkGroup bookmarkGroup;

    public void setBookmark(T bookmark) {
        this.bookmark = bookmark;
    }

    public void setBookmarkGroup(BookmarkGroup group) {
        this.bookmarkGroup = group;
    }

    public T getBookmark() {
        return bookmark;
    }

    public BookmarkGroup getBookmarkGroup() {
        return bookmarkGroup;
    }
}
