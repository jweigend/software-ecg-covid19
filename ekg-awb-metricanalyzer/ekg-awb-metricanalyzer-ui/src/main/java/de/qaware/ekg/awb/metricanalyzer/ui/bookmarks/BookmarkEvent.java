//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.bookmarks;

import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.common.ui.events.WindowOpenEvent;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.EventObject;

/**
 * This event is called if there was a double click to a bookmark item. After that, there will be a reload of the chart
 * and the model will be set to the settings specified in the bookmark.
 */
public class BookmarkEvent extends EventObject implements WindowOpenEvent {

    private final Bookmark bookmark;
    private final EkgRepository sourceRepository;
    private final OpeningMode openingMode;

    /**
     * Creates a new bookmark event object.
     *
     * @param source   The object on which the Event initially occurred.
     * @param bookmark the current bookmark
     */
    public BookmarkEvent(Object source, Bookmark bookmark) {
        this(source, bookmark, null, OpeningMode.CLEAR_VIEW);
    }

    /**
     * Create a new bookmark event object.
     *
     * @param source           The object on which the Event initially occurred.
     * @param bookmark         The bookmark.
     * @param sourceRepository The types where this bookmark is stored.
     */
    public BookmarkEvent(Object source, Bookmark bookmark, EkgRepository sourceRepository) {
        this(source, bookmark, sourceRepository, OpeningMode.CLEAR_VIEW);
    }

    /**
     * Create a new bookmark event object.
     *
     * @param source      The object on which the Event initially occurred.
     * @param bookmark    The bookmark.
     * @param openingMode Should the event result in a new view?
     */
    public BookmarkEvent(Object source, Bookmark bookmark, OpeningMode openingMode) {
        this(source, bookmark, null, openingMode);
    }

    /**
     * Create a new bookmark event object.
     *
     * @param source           The object on which the Event initially occurred.
     * @param bookmark         The bookmark.
     * @param sourceRepository The types where this bookmark is stored.
     * @param openingMode      Should the event result in a new view?
     */
    public BookmarkEvent(Object source, Bookmark bookmark, EkgRepository sourceRepository, OpeningMode openingMode) {
        super(source);
        this.bookmark = bookmark;
        this.sourceRepository = sourceRepository;
        this.openingMode = openingMode;
    }

    public Bookmark getBookmark() {
        return bookmark;
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
        if (!(o instanceof BookmarkEvent)) {
            return false;
        }
        BookmarkEvent that = (BookmarkEvent) o;
        return new EqualsBuilder()
                .append(getOpeningMode(), that.getOpeningMode())
                .append(getBookmark(), that.getBookmark())
                .append(getSourceRepository(), that.getSourceRepository())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getBookmark())
                .append(getSourceRepository())
                .append(getOpeningMode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("bookmark", bookmark)
                .append("sourceRepository", sourceRepository)
                .append("openingMode", openingMode)
                .toString();
    }
}
