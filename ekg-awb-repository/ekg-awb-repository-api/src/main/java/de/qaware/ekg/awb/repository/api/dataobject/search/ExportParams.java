package de.qaware.ekg.awb.repository.api.dataobject.search;

import de.qaware.ekg.awb.repository.api.dataobject.expr.Expression;
import de.qaware.ekg.awb.repository.api.schema.Field;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Input parameters for an export.
 */
public class ExportParams {
    private final List<Expression> filterQueries;
    private List<SortField> sortFields;

    /**
     * Constructs a new {@link ExportParams} instance with default parameters.
     */
    public ExportParams() {
        this.filterQueries = new ArrayList<>();
        this.sortFields = new ArrayList<>();
    }

    /**
     * Set the sort field.
     *
     * @param sortField sort field
     * @return this {@code SearchParams} instance
     */
    public ExportParams withSortField(SortField sortField) {
        this.sortFields.add(sortField);
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
    public ExportParams withSortField(Field field, SortField.SortMode mode) {
        this.sortFields.add(new SortField(field, mode));
        return this;
    }

    /**
     * Set a list of sort fields.
     *
     * @param sortFields sort fields
     * @return this {@code ExportParams} instance
     */
    public ExportParams withSortFields(List<SortField> sortFields) {
        this.sortFields = new ArrayList<>(sortFields);
        return this;
    }

    /**
     * Add filter queries.
     *
     * @param filterQueries filter queries
     * @return this {@code SearchParams} instance
     */
    public ExportParams withFilterQueries(List<Expression> filterQueries) {
        this.filterQueries.addAll(filterQueries);
        return this;
    }

    /**
     * Add filter queries.
     *
     * @param filterQueries filter queries
     * @return this {@code SearchParams} instance
     */
    public ExportParams withFilterQueries(Expression... filterQueries) {
        Collections.addAll(this.filterQueries, filterQueries);
        return this;
    }

    /**
     * Returns the sort field.
     *
     * @return the sort field
     */
    public List<SortField> getSortFields() {
        return Collections.unmodifiableList(sortFields);
    }

    /**
     * Returns the filter queries.
     *
     * @return the filter queries
     */
    public List<Expression> getFilterQueries() {
        return Collections.unmodifiableList(filterQueries);
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ExportParams rhs = (ExportParams) obj;
        return new EqualsBuilder()
                .append(this.filterQueries, rhs.filterQueries)
                .append(this.sortFields, rhs.sortFields)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(filterQueries)
                .append(sortFields)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("filterQueries", filterQueries)
                .append("sortFields", sortFields)
                .toString();
    }
}
