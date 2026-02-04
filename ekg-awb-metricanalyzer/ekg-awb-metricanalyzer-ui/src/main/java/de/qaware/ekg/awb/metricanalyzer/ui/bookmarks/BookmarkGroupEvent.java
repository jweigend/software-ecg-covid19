//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.bookmarks;

import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.common.ui.events.WindowOpenEvent;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.EventObject;

/**
 * This event is called if there was a double click to a bookmark item. After that, there will be a reload of the chart
 * and the model will be set to the settings specified in the bookmark.
 */
public class BookmarkGroupEvent extends EventObject implements WindowOpenEvent {

    private final BookmarkGroup bookmarkGroup;
    private final EkgRepository sourceRepository;
    private final OpeningMode openingMode;

    /**
     * Creates a new bookmark event object.
     *
     * @param source   The object on which the Event initially occurred.
     * @param bookmarkGroup the current bookmark group
     */
    public BookmarkGroupEvent(Object source, BookmarkGroup bookmarkGroup) {
        this(source, bookmarkGroup, null, OpeningMode.CLEAR_VIEW);
    }

    /**
     * Create a new bookmark event object.
     *
     * @param source           The object on which the Event initially occurred.
     * @param bookmarkGroup    The bookmark group.
     * @param sourceRepository The types where this bookmark is stored.
     */
    public BookmarkGroupEvent(Object source, BookmarkGroup bookmarkGroup, EkgRepository sourceRepository) {
        this(source, bookmarkGroup, sourceRepository, OpeningMode.CLEAR_VIEW);
    }

    /**
     * Create a new bookmark event object.
     *
     * @param source        The object on which the Event initially occurred.
     * @param bookmarkGroup The bookmark group
     * @param openingMode   Should the event result in a new view?
     */
    public BookmarkGroupEvent(Object source, BookmarkGroup bookmarkGroup, OpeningMode openingMode) {
        this(source, bookmarkGroup, null, openingMode);
    }

    /**
     * Create a new bookmark event object.
     *
     * @param source           The object on which the Event initially occurred.
     * @param bookmarkGroup    The bookmark group
     * @param sourceRepository The types where this bookmark is stored.
     * @param openingMode      Should the event result in a new view?
     */
    public BookmarkGroupEvent(Object source, BookmarkGroup bookmarkGroup, EkgRepository sourceRepository, OpeningMode openingMode) {
        super(source);
        this.bookmarkGroup = bookmarkGroup;
        this.sourceRepository = sourceRepository;
        this.openingMode = openingMode;
    }

    public BookmarkGroup getBookmarkGroup() {
        return bookmarkGroup;
    }

    public EkgRepository getSourceRepository() {
        return sourceRepository;
    }

    @Override
    public boolean enforceNewView() {
        return openingMode == OpeningMode.NEW_VIEW || openingMode == OpeningMode.MERGE_VIEW;
    }

    @Override
    public OpeningMode getOpeningMode() {
        return openingMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookmarkGroupEvent)) {
            return false;
        }
        BookmarkGroupEvent that = (BookmarkGroupEvent) o;
        return new EqualsBuilder()
                .append(getOpeningMode(), that.getOpeningMode())
                .append(getBookmarkGroup(), that.getBookmarkGroup())
                .append(getSourceRepository(), that.getSourceRepository())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getBookmarkGroup())
                .append(getSourceRepository())
                .append(getOpeningMode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("bookmarkGroup", getBookmarkGroup())
                .append("sourceRepository", sourceRepository)
                .append("openingMode", openingMode)
                .toString();
    }
}
