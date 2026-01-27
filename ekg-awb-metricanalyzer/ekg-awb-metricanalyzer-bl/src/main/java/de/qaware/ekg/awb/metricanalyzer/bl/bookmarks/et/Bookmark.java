//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et;

import de.qaware.ekg.awb.repository.api.model.AbstractEt;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Represents a bookmark. It contains the minimal information that is needed for a bookmark.
 */
public abstract class Bookmark extends AbstractEt implements Serializable {

    private static final long serialVersionUID = -3504999081713331250L;

    /**
     * The name.
     */
    @PersistedField(EkgSchemaField.BOOKMARK_NAME)
    private String name;

    /**
     * The unique id of the bookmark group this bookmark instance belongs to.
     * if it is not explicitly assigned to a group, this value will be '_BOOKMARK_GLOBAL_'
     * per default.
     */
    @PersistedField(EkgSchemaField.BOOKMARK_GROUP_ID)
    private String bookmarkGroupId;

    /**
     * Protected constructor due init of Bookmarks through the {@link Bookmark.Builder}.
     */
    protected Bookmark(DocumentType type) {
        setType(type.name());
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Bookmark)) {
            return false;
        }
        Bookmark bookmark = (Bookmark) o;
        return new EqualsBuilder()
                .append(getId(), bookmark.getId())
                .append(getName(), bookmark.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getId())
                .append(getName())
                .append(getBookmarkGroupId())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("groupId", bookmarkGroupId)
                .toString();
    }

    /**
     * Builder to create a bookmark instance.
     *
     * @param <T> The actual type of the created {@link Bookmark}.
     */
    public abstract static class Builder<T extends Bookmark> {
        protected String id;
        protected String name;
        protected String bookmarkGroupId;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Create a builder from an existing bookmark.
         *
         * @param bookmark the initial values for the builder.
         */
        protected Builder(Bookmark bookmark) {
            this.id = bookmark.getId();
            this.name = bookmark.getName();
        }

        /**
         * Add id to builder.
         *
         * @param id the id name
         * @return this for fluent interface
         */
        public Builder<T> withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Adds the id of the bookmark group to the builder
         * the constructed bookmark will assigned to.
         *
         * @param groupId the id of the bookmark group
         * @return this for fluent interface
         */
        public Builder<T> withBookmarkGroupId(String groupId) {
            this.bookmarkGroupId = groupId;
            return this;
        }

        /**
         * Add name to builder.
         *
         * @param name the name name
         * @return this for fluent interface
         */
        public Builder<T> withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Build the real bookmark instance.
         *
         * @return the built bookmark
         */
        public abstract T build();

        /**
         * Instantiates a new bookmark with the given bookmark values.
         *
         * @param bookmark The new bookmark
         */
        protected void build(final T bookmark) {
            Bookmark b = bookmark;
            b.setId(this.id);
            b.name = this.name;
        }


    }
}
