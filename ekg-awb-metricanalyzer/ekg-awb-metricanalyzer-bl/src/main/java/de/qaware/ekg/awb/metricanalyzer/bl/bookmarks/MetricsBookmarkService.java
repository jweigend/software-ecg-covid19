//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.bookmarks;

import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.MetricBookmark;
import de.qaware.ekg.awb.repository.api.RepositoryClientAware;

import java.util.List;

/**
 * An interface that represents a bookmark service that provides
 * various methods to create, update, delete and query bookmarks.
 */
public interface MetricsBookmarkService extends RepositoryClientAware {

    // ------------------------------------------------------------------------------------------------------
    // MetricBookmark actions
    // ------------------------------------------------------------------------------------------------------

    /**
     * Returns a sorted list of (metric) bookmarks that exists in the underlying repository
     * and is related to the specified bookmark group.
     * Depending on the group id the service will have the following behaviour:
     *  - bookmarkGroupId == null or "" : the service will return any MetricBookmark that exists in the repository
     *  - bookmarkGroupId == {@link BookmarkGroup#DEFAULT_GLOBAL_GROUP_ID } : the service will return all bookmark not assigned to a specific group
     *  - bookmarkGroupId == CONCRETE ID : the service will return bookmarks that are assigned to the specified group with the CONCRETE ID
     *
     * @param bookmarkGroupId the unique id of the bookmark group the searched bookmarks belongs to
     * @return a list of MetricBookmark instances that are assigned to the given group id (if specified)
     */
    List<MetricBookmark> getMetricBookmarks(String bookmarkGroupId);

    // ------------------------------------------------------------------------------------------------------
    // generic bookmark actions
    // ------------------------------------------------------------------------------------------------------

    /**
     * Stores a bookmark for the query context.
     *
     * @param bookmark the new bookmark to store.
     */
    void persistNewBookmark(Bookmark bookmark);

    /**
     * Updates a bookmark.
     * <p>
     * The old and new object must have the same id. In some cases the backend do not support
     * a real update than it deletes the stored object with the same id and create a new backend
     * object. In this case the id of the updated element may changed by the backend.
     *
     * @param bookmark the bookmark.
     */
    void updateBookmark(Bookmark bookmark);

    /**
     * Deletes a bookmark.
     *
     * @param bookmark the bookmark.
     */
    void deleteBookmark(Bookmark bookmark);

    // ------------------------------------------------------------------------------------------------------
    // BookmarkGroup actions
    // ------------------------------------------------------------------------------------------------------

    /**
     * Returns a list of all BookmarkGroup's that currently exists in the repository.
     * The result items in the list are sorted alphabetical in ascending order.
     * The implicit existing bookmark "GLOBAL" isn't part of the result.
     *
     * @return a sorted list of BookmarkGroup items
     */
    List<BookmarkGroup> getBookmarkGroups();

    /**
     * Creates a new BookmarkGroup record in the repository.
     * The id of the given instance will used for consistency checks.
     * If not available the procedure will fail. The same if already an bookmark exists with that id.
     *
     * @param bookmarkGroup the bookmark group item to persist
     */
    void persistNewBookmarkGroup(BookmarkGroup bookmarkGroup);

    /**
     * Update mutable attributes of an existing bookmark group in the repository.
     * This method couldn't use to persist a new BookmarkGroup instance or to change it's id.
     *
     * @param bookmarkGroup the bookmark group to update
     */
    void updateBookmark(BookmarkGroup bookmarkGroup);

    /**
     * Deletes a BookmarkGroup and all corresponding (Metric)Bookmarks.
     * The method will return true if all objects deleted successfully.
     * False if no bookmark group with the specified id exists.
     *
     * @param bookmarkId the unique id of the bookmark group to delete
     */
    void deleteBookmarkGroup(String bookmarkId);

}
