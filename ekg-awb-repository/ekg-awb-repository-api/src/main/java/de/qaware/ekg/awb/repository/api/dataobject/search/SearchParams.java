package de.qaware.ekg.awb.repository.api.dataobject.search;

import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.schema.Field;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A parameter set for a regular search.
 */
public class SearchParams {

    /**
     * Limit for maxRows
     */
    public static final int MAX_ROWS_LIMIT = 10_000;

    private int startRow;
    private int maxRows;
    private String cursorMark = null;
    private final List<Expression> filterQueries;
    private List<SortField> sortFields;

    /**
     * Constructs a new {@code SearchParams} instance with default values.
     */
    public SearchParams() {
        this.startRow = 0;
        this.maxRows = MAX_ROWS_LIMIT;
        this.filterQueries = new ArrayList<>();
        this.sortFields = new ArrayList<>();
    }

    /**
     * Copy constructor for the search params.
     *
     * @param searchParams search params to copy
     */
    private SearchParams(SearchParams searchParams) {
        this.startRow = searchParams.startRow;
        this.maxRows = searchParams.maxRows;
        this.cursorMark = searchParams.cursorMark;
        this.filterQueries = new ArrayList<>(searchParams.filterQueries);
        this.sortFields = searchParams.sortFields;
    }

    /**
     * Create a copy of this {@code SearchParams} instance.
     *
     * @return copy of this
     * instance
     */
    public SearchParams copy() {
        return new SearchParams(this);
    }

    /**
     * Set the start row.
     *
     * @param startRow start row
     * @return this {@code SearchParams} instance
     */
    public SearchParams withStartRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    /**
     * Set the max. number of rows to be returned. Can not be greater than the {@link #MAX_ROWS_LIMIT}.
     *
     * @param maxRows max. number of rows to be returned
     * @return this {@code SearchParams} instance
     */
    public SearchParams withMaxRows(int maxRows) {
        if (maxRows > MAX_ROWS_LIMIT) {
            throw new IllegalArgumentException("MaxRows may not be greater than " + MAX_ROWS_LIMIT);
        }
        this.maxRows = maxRows;
        return this;
    }

    /**
     * Adds a sort field.
     *
     * @param sortField sort field
     * @return this {@code SearchParams} instance
     */
    public SearchParams withSortField(SortField sortField) {
        this.sortFields.add(sortField);
        return this;
    }

    /**
     * Set a list of sort fields.
     *
     * @param sortField sort field
     * @return this {@code SearchParams} instance
     */
    public SearchParams withSortFields(List<SortField> sortField) {
        this.sortFields = new ArrayList<>(sortField);
        return this;
    }

    /**
     * Set the sort field.
     * <p/>
     * Shorthand for {@code .withSortField(new SortField(field, mode))}.
     *
     * @param field the field
     * @param mode  this instance
     * @return this instance
     */
    public SearchParams withSortField(Field field, SortField.SortMode mode) {
        this.sortFields.add(new SortField(field, mode));
        return this;
    }

    /**
     * Add filter queries.
     *
     * @param filterQueries filter queries
     * @return this {@code SearchParams} instance
     */
    public SearchParams withFilterQueries(List<Expression> filterQueries) {
        this.filterQueries.addAll(filterQueries);
        return this;
    }

    /**
     * Add filter queries.
     *
     * @param filterQueries filter queries
     * @return this {@code SearchParams} instance
     */
    public SearchParams withFilterQueries(Expression... filterQueries) {
        Collections.addAll(this.filterQueries, filterQueries);
        return this;
    }

    /**
     * Get the row the search result should start with.
     *
     * @return row the search result should start with
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Get the max. number of rows returned
     *
     * @return max. number of rows returned.
     */
    public int getMaxRows() {
        return maxRows;
    }

    /**
     * Gets sort field.
     *
     * @return the sort field
     */
    public List<SortField> getSortFields() {
        return new ArrayList<>(sortFields);
    }

    /**
     * Gets filter queries.
     *
     * @return the filter queries
     */
    public List<Expression> getFilterQueries() {
        return Collections.unmodifiableList(filterQueries);
    }

    public SearchParams withCursor(String cursorId) {
        this.cursorMark = cursorId;
        return this;
    }

    public boolean hasCursor() {
        return this.cursorMark != null;
    }

    public String getCursorMark() {
        return this.cursorMark;
    }

    /**
     * Whether to sort or not to sort.
     *
     * @return true if sorted, otherwise false
     */
    public boolean hasSortField() {
        return !sortFields.isEmpty();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.startRow)
                .append(this.maxRows)
                .append(this.filterQueries)
                .append(this.sortFields)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchParams other = (SearchParams) obj;
        return new EqualsBuilder()
                .append(this.startRow, other.startRow)
                .append(this.maxRows, other.maxRows)
                .append(this.filterQueries, other.filterQueries)
                .append(this.sortFields, other.sortFields)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("startRow", startRow)
                .append("maxRows", maxRows)
                .append("filterQueries", filterQueries)
                .append("sortFields", sortFields.toString())
                .toString();
    }


}
