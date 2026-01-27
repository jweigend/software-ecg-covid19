package de.qaware.ekg.awb.da.elasticsearch.utils;

import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

import static de.qaware.ekg.awb.da.elasticsearch.utils.FieldType.*;
import static de.qaware.ekg.awb.da.elasticsearch.utils.SchemaFieldPropertyType.*;
import static de.qaware.ekg.awb.repository.api.schema.EkgSchemaField.*;

/**
 * The Software EKG schema definition as code.
 * This builder provides the schema as as XContentBuilder that is a Java API to build JSON objects.
 * To use this schema call the builderSchema() method at programmatically index creation:
 * <code>
 * CreateIndexRequest request = new CreateIndexRequest("software-ekg-data");
 * request.mapping(DOC_TYPE_KEY, builderSchema());
 * </code>
 */
public class SchemaBuilder {

    /**
     * The default name of the single-type per index enforced since ES v6
     * Using exact this name all default ElasticSearch indexer and co. work without custom configs/modifications.
     */
    public static final String DOC_TYPE_KEY = "doc";

    /**
     * Builds and provides the Software-EKG repository schema as JSON structure
     * for ElasticSearch represented by a XContentBuilder instance.
     *
     * @return the schema as JSON structure represented by a XContentBuilder instance
     * @throws IOException if something fails during the schema build process
     */
    public static XContentBuilder buildSchema() throws IOException {

        XContentBuilder builder = XContentFactory.jsonBuilder();

        builder.startObject();
        {
            builder.startObject(DOC_TYPE_KEY);
            {
                builder.startObject("properties");
                {
                    //==================================================================================================
                    // common fields
                    //==================================================================================================

                    defineField(builder, ID, KEYWORD, true, true, true);

                    defineField(builder, DOC_TYPE, KEYWORD, true, true, true);

                    //==================================================================================================
                    // project type
                    //==================================================================================================

                    // --- the unique name of the project for a single ElasticSearch index ---
                    defineField(builder, PROJECT_NAME, KEYWORD, true, true, true);

                    // --- the description that details the content of the project ---
                    defineField(builder, PROJECT_DESCRIPTION, TEXT, false, true, false);

                    // --- true = SPLIT_SOURCE_PROJECT, false = LOCAL_PROJECT (see ProjectType enum) ---
                    defineField(builder, PROJECT_USE_SPLIT_SOURCE, BOOLEAN, false, true, false);

                    // --- CLASSIC; HYBRID; CLOUD_NATIVE (see ProjectFlavor enum) ---
                    defineField(builder, PROJECT_FLAVOR, KEYWORD, true, true, true);

                    // --- DEFAULT; GENERIC; SPECIALIZED (see ProjectTimeSeriesType enum) ---
                    defineField(builder, PROJECT_TIMESERIES_TYPE, KEYWORD, true, true, true);

                    // --- NONE; OPEN_SHIFT; KUBERNETES; OTHER (see CloudPlatformType enum) ---
                    defineField(builder, PROJECT_CN_TYPE, KEYWORD, true, true, true);

                    // --- The id of the importer the that is allowed to import data to this project ---
                    defineField(builder, PROJECT_IMPORTER_ID, KEYWORD, true, true, true);

                    // --- project specific dimension alias (optional filled) ---
                    defineField(builder, PROJECT_DIM_ALIAS_HOSTGROUP, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_HOST, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_NAMESPACE, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_SERVICE, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_POD, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_CONTAINER, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_MEASUREMENT, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_PROCESS, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_METRICGROUP, KEYWORD, false, true, false);
                    defineField(builder, PROJECT_DIM_ALIAS_METRICNAME, KEYWORD, false, true, false);

                    //==================================================================================================
                    //  types config used either for EKG repositories and also for importers remote data sources
                    //==================================================================================================

                    defineField(builder, REPOSITORY_IS_IMPORTSOURCE, BOOLEAN, true, true, false);

                    defineField(builder, REPOSITORY_AUTH_TYPE, KEYWORD, false, true, false);

                    defineField(builder, REPOSITORY_NAME, KEYWORD, true, true, true);

                    defineField(builder, REPOSITORY_DB_TYPE, KEYWORD, true, true, true);

                    defineField(builder, REPOSITORY_URL, KEYWORD, false, true, false);

                    defineField(builder, REPOSITORY_INDEX_NAME, KEYWORD, false, true, false);

                    defineField(builder, REPOSITORY_USER, KEYWORD, false, true, false);

                    defineField(builder, REPOSITORY_PASSWORD, KEYWORD, false, true, false);

                    defineField(builder, REPOSITORY_ACCORDING_PROJECT, KEYWORD, true, true, false);

                    defineField(builder, REPOSITORY_SSO_TOKEN, KEYWORD, false, true, false);

                    //==================================================================================================
                    //  types config used either for EKG repositories and also for importers remote data sources
                    //==================================================================================================

                    // classic only
                    defineField(builder, TS_HOST_GROUP_NAME, KEYWORD, true, true, true);

                    defineField(builder, TS_HOST_NAME, KEYWORD, true, true, true);

                    // cloud native only
                    defineField(builder, TS_NAMESPACE_NAME, KEYWORD, true, true, true);

                    defineField(builder, TS_SERVICE_NAME, KEYWORD, true, true, true);

                    defineField(builder, TS_POD_NAME, KEYWORD, true, true, true);

                    defineField(builder, TS_CONTAINER_NAME, KEYWORD, true, true, true);

                    // common dimensions
                    defineField(builder, TS_MEASUREMENT, KEYWORD, true, true, true);

                    defineField(builder, TS_PROCESS_NAME, KEYWORD, true, true, true);

                    defineField(builder, TS_METRIC_GROUP, KEYWORD, true, true, true);

                    defineField(builder, TS_METRIC_NAME, KEYWORD, true, true, true);

                    // metadata of the time-series
                    defineField(builder, TS_REMOTE_SERIES_KEY, TEXT, false, true, false);

                    defineField(builder, TS_IMPORT_DATE, LONG, true, true, false);

                    defineField(builder, TS_DATA, BINARY, false, true, false);

                    defineField(builder, TS_DATA_AMOUNT_VALUES, INT, false, true, false);

                    defineField(builder, TS_START, LONG, true, true, true);

                    defineField(builder, TS_STOP, LONG, true, true, true);

                    defineField(builder, TS_AGGREGATION_LEVEL, KEYWORD, true, true, true);

                    defineField(builder, TS_IS_MERGED, KEYWORD, true, true, true);

                    defineField(builder, TS_GROUP_KEY_HASH, INT, true, true, true);


                    //==================================================================================================
                    // bookmark data types
                    //==================================================================================================

                    defineField(builder, BOOKMARK_NAME, KEYWORD, true, true, false);

                    defineField(builder, BOOKMARK_GROUP_NAME, KEYWORD, true, true, false);

                    defineField(builder, BOOKMARK_DESCRIPTION, TEXT, false, true, false);

                    defineField(builder, BOOKMARK_PROTOCOL, KEYWORD, false, true, false);

                    defineField(builder, BOOKMARK_TIME_ZONE, KEYWORD, false, true, false);

                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();

        return builder;
    }

    /**
     * Defines a single doc-type field definition compatible to the ElasticSearch mapping syntax.
     * This method is designed to be called multiple times per schema creation to specify schema field per field.
     * In contrast to the direct way it saves a lot of clue code and provides a type safe API do define the fields.
     *
     * @param builder the builder used for schema creation
     * @param field the EKG repository schema field that should defined
     * @param type the type of the repository field. Only field types used in EKG are defined in the enum
     * @param indexed flag that specifies if the field is indexed or not
     * @param stored flag that specifies if the field is stored or not
     * @param isDocValue flag that specifies if the field is a doc-value or not
     * @throws IOException thrown if something fails during the appending the file definition to the builder
     */
    public static void defineField(XContentBuilder builder, EkgSchemaField field, FieldType type,
                                   boolean indexed, boolean stored, boolean isDocValue) throws IOException {

        builder.startObject(field.getName());

        builder.field(FIELD_TYPE.getKey(), type.getTypeKey());
        builder.field(INDEXED.getKey(), indexed);
        builder.field(STORED.getKey(), stored);
        builder.field(IS_DOC_VALUE.getKey(), isDocValue);

        builder.endObject();

    }
}
