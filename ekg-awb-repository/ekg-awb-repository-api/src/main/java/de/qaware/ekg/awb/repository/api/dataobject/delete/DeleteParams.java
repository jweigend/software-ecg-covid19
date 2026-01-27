package de.qaware.ekg.awb.repository.api.dataobject.delete;

import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;

import java.util.HashMap;
import java.util.Map;

/**
 * The DeleteParams class is a data-type used to collect and
 * transfer filter parameters used to delete records from the repository.
 * The bean also provides the functionality to return all collected items as
 * Lucene compatible filter string.
 */
public class DeleteParams {

    private Map<EkgSchemaField, String> filterExpMap = new HashMap<>();

    /**
     * Add an exact or wildcard filter that has to match to one of the values
     * of the specified schema field.
     *
     * @param field the EKG repository schema field that defines the record attribute that has to match the filter
     * @param filterValue the value that will used as matching reference
     */
    public DeleteParams addFilter(EkgSchemaField field, String filterValue) {
        filterExpMap.put(field, filterValue);
        return this;
    }

    /**
     * Returns the map with all filters
     * @return the map of filters
     */
    public Map<EkgSchemaField, String> getFilterExpMap() {
        return filterExpMap;
    }

    /**
     * Returns the AND concated filter added before as Lucene
     * compatible filter string.
     *
     * @return all filters concated as Lucene query string.
     */
    public String toDeleteFilterQueryString() {

        StringBuilder expression = new StringBuilder();

        for (Map.Entry<EkgSchemaField, String> entry : filterExpMap.entrySet()) {
            expression.append(" AND ");

            expression.append(entry.getKey().getName());
            expression.append(":");

            if (entry.getValue().contains(" ")) {
                expression.append("\"");
            }

            expression.append(entry.getValue());

            if (entry.getValue().contains(" ")) {
                expression.append("\"");
            }

        }

        return expression.substring(5);
    }
}