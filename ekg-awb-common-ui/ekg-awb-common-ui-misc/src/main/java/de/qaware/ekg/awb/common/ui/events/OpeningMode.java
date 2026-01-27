package de.qaware.ekg.awb.common.ui.events;

/**
 * The opening mode describes which views should be used when opening some items.
 */
public enum OpeningMode {

    /**
     * Clears the data of the existing view and load
     * the new one into it. If no view already exists
     * a new one will be open.
     */
    CLEAR_VIEW,

    /**
     * Opens the data in the existing panel a keep all (background chart) data
     * as is. The bookmark button function will locked until
     * the a clear view is open because bookmarks doesn't support multiple
     * contexts in one view.
     *
     * If no view exists a new one will open.
     */
    MERGE_VIEW,

    /**
     * Enforces to load the data into a new window that exists besides of an
     * existing view that shows series data.
     * (this feature is currently not supported)
     */
    NEW_VIEW,

    /**
     * Update the existing view with new query data but keep the
     * context (project/repository) as before.
     */
    UPDATE
}
