package de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et;

import de.qaware.ekg.awb.repository.api.model.AbstractEt;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;

import java.util.Objects;

/**
 * A POJO that represents a bookmark group that wraps
 * bookmarks that belongs to a specific domain or topic.
 */
public class BookmarkGroup extends AbstractEt {

    /**
     * The id of the 'virtual' bookmark group if an bookmark
     * isn't assigned to a specific group and exists in the global space.
     */
    public static final String DEFAULT_GLOBAL_GROUP_ID = "_BOOKMARK_GLOBAL_";

    /**
     * The name of the bookmark group (shown in the UI)
     */
    @PersistedField(EkgSchemaField.BOOKMARK_GROUP_NAME)
    private String name;

    /**
     * The unique if of the bookmark group
     */
    @PersistedField(EkgSchemaField.BOOKMARK_GROUP_ID)
    private String bookmarkGroupId;

    /**
     * Creates a new instance of BookmarkGroup
     */
    public BookmarkGroup() {
        setType(DocumentType.BOOKMARK_GROUP.name());
    }

    /**
     * Constructs a new BookmarkGroup that
     * has the given name.
     *
     * @param name the display name of the bookmark group
     */
    public BookmarkGroup(String name) {
        this();
        setName(name);
    }

    /**
     * Constructs a new BookmarkGroup with
     * the given name and id
     *
     * @param name the readable alias name of the group
     * @param bookmarkGroupId the unique id of the group
     */
    public BookmarkGroup(String name, String bookmarkGroupId) {
        this(name);
        this.bookmarkGroupId = bookmarkGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBookmarkGroupId() {
        return bookmarkGroupId;
    }

    public void setBookmarkGroupId(String bookmarkGroupId) {
        this.bookmarkGroupId = bookmarkGroupId;
    }

    /**
     * Returns a boolean value that indicates
     * if this Bookmark is new (never persisted before)
     * or not.
     *
     * @return TRUE if the Bookmark is a new created one without id.
     */
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isEmptyGroup() {
        return DEFAULT_GLOBAL_GROUP_ID.equals(bookmarkGroupId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BookmarkGroup that = (BookmarkGroup) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(bookmarkGroupId, that.bookmarkGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, bookmarkGroupId);
    }
}
