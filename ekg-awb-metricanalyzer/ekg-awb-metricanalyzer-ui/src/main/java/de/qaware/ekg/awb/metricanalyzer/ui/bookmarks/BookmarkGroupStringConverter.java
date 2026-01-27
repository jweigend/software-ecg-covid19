package de.qaware.ekg.awb.metricanalyzer.ui.bookmarks;

import de.qaware.ekg.awb.common.ui.components.FilterableComboBoxStringConverter;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import org.apache.commons.lang3.StringUtils;

/**
 * A simple converter that converts a String into a BookmarkGroup
 * and vice versa.
 * The conversion form String isn't complete because the BookmarkID is missing
 */
public class BookmarkGroupStringConverter extends FilterableComboBoxStringConverter<BookmarkGroup> {

    @Override
    public String toString(BookmarkGroup bookmarkGroup) {
        if (bookmarkGroup != null) {
            return bookmarkGroup.getName();
        }

        return "NULL";
    }

    @Override
    public BookmarkGroup fromString(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        BookmarkGroup bookmarkGroup = super.selectValue(s);
        if (bookmarkGroup != null) {
            return bookmarkGroup;
        }

        return new BookmarkGroup(s);
    }
}
