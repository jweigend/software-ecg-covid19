package de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et;

import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;

/**
 * Concrete bookmark type that represents a bookmark for
 * a simple or complex chart setup that should be view in future.
 * A complex setup means a chart that displays multiple background
 * charts and optional use different aggregations or color settings.
 *
 * This is the default bookmark that is currently supported in the AWB.
 */
public class MetricBookmark extends Bookmark {

    @PersistedField(EkgSchemaField.BOOKMARK_PROTOCOL)
    private String commandProtocol;

    @PersistedField(EkgSchemaField.PROJECT_NAME)
    private String projectName;

    /**
     * Protected constructor due init of Bookmarks through the {@link Builder}.
     */
    public MetricBookmark() {
        super(DocumentType.METRIC_BOOKMARK);
    }

    public String getSerializedCommandProtocol() {
        return commandProtocol;
    }

    public String getProjectName() {
        return projectName;
    }

    /**
     * Builder to create a bookmark instance.
     */
    public static class Builder extends Bookmark.Builder<MetricBookmark> {

        private String commandProtocol;

        private String projectName;

        @Override
        public MetricBookmark build() {
            MetricBookmark bookmark = new MetricBookmark();
            bookmark.setName(super.name);
            bookmark.setId(super.id);
            bookmark.commandProtocol = commandProtocol;
            bookmark.projectName = projectName;

            return bookmark;
        }

        public Builder withCommandProtocol(String commandProtocol) {
            this.commandProtocol = commandProtocol;
            return this;
        }

        public Builder withProjectName(String projectName) {
            this.projectName = projectName;
            return this;
        }
    }


}
