package de.qaware.ekg.awb.repository.api.schema;

import static de.qaware.ekg.awb.repository.api.schema.Field.Multiplicity.SINGLE_VALUE;

/**
 * The EkgSchemaField class defines constants for each attribute field that can
 * occur in a document stored in an EKG repository. Each field is described with
 * it's name and the Multiplicity (single value or multi value field).
 */
public enum EkgSchemaField implements Field {


    //================================================================================================================
    //  common fields
    //================================================================================================================

    /**
     * The unique id of the document in the database specified
     */
    ID("id", SINGLE_VALUE),

    /**
     * fixed document type for multi-datatype tables that specifies that
     * this bean(document)
     */
    DOC_TYPE("type", SINGLE_VALUE),


    //================================================================================================================
    //  project fields
    //================================================================================================================

    /**
     * The name of the project (should be unique)
     */
    PROJECT_NAME("project_name", SINGLE_VALUE),

    /**
     * A description text that provides more details to the project
     */
    PROJECT_DESCRIPTION("project_description", SINGLE_VALUE),

    /**
     * A boolean flag that indicates if the project queries time series data
     * in real time from foreign databases or fetch it from the EKG repository itself
     */
    PROJECT_USE_SPLIT_SOURCE("project_useSplitSource", SINGLE_VALUE),

    /**
     * representation of the ProjectType that can be null or
     * one of the following strings:
     * <ul>
     *     <li>CLASSIC</li>
     *     <li>HYBRID</li>
     *     <li>CLOUD_NATIVE</li>
     * </ul>
     */
    PROJECT_FLAVOR("project_flavor", SINGLE_VALUE),

    /**
     * A string representation of the CloudPlatformType that can be null or
     * one of the following strings:
     * <ul>
     *     <li>NONE</li>
     *     <li>OPEN_SHIFT</li>
     *     <li>KUBERNETES</li>
     *     <li>OTHER</li>
     * </ul>
     */
    PROJECT_CN_TYPE("project_cn_type", SINGLE_VALUE),


    /**
     * The id of the importer that has initial created this
     * project (creation in import dialog)
     *
     * The id can used to filter projects in import dialogs if a specific importer
     * enforces to import new only in projects with it's own alias mapping.
     */
    PROJECT_IMPORTER_ID("project_importerId", SINGLE_VALUE),

    /**
     * An enum value that indicated ith the time series that belongs to the project
     * are from EKG default type (domain of IT/Software systems), generic (can used by any) or importer specific
     * DEFAULT; GENERIC; SPECIALIZED (see ProjectTimeSeriesType enum)
     */
    PROJECT_TIMESERIES_TYPE("project_timeseries_type", SINGLE_VALUE),

    /**
     * the optional alias names if project specific mapping is in use
     */
    PROJECT_DIM_ALIAS_HOSTGROUP("project_dimension_alias_hostGroup", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_HOST("project_dimension_alias_host", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_NAMESPACE("project_dimension_alias_namespace", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_SERVICE("project_dimension_alias_service", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_POD("project_dimension_alias_pod", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_CONTAINER("project_dimension_alias_container", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_MEASUREMENT("project_dimension_alias_measurement", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_PROCESS("project_dimension_alias_process", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_METRICGROUP("project_dimension_alias_metricGroup", SINGLE_VALUE),
    PROJECT_DIM_ALIAS_METRICNAME("project_dimension_alias_metricName", SINGLE_VALUE),


    //================================================================================================================
    //  time series fields
    //================================================================================================================

    TS_HOST_GROUP_NAME("ts_hostGroup", SINGLE_VALUE),

    TS_HOST_NAME("ts_host", SINGLE_VALUE),

    TS_NAMESPACE_NAME("ts_namespace", SINGLE_VALUE),

    TS_SERVICE_NAME("ts_service", SINGLE_VALUE),

    TS_POD_NAME("ts_pod", SINGLE_VALUE),

    TS_CONTAINER_NAME("ts_container", SINGLE_VALUE),

    TS_MEASUREMENT("ts_measurement", SINGLE_VALUE),

    TS_PROCESS_NAME("ts_process", SINGLE_VALUE),

    TS_METRIC_GROUP("ts_metricGroup", SINGLE_VALUE),

    TS_METRIC_NAME("ts_metricName", SINGLE_VALUE),

    /**
     * The time series data itself as byte array. Each measured value is stored as tuple of
     * timestamp (long, 8 byte) and the value (double, 8 byte). So each tuple has 16 byte in
     * total and the array has an multiple of 16 as length.
     */
    TS_DATA("ts_data", SINGLE_VALUE),

    /**
     * Integer value that stores the amount of tuples (timestamp/value) in the time series.
     * This value = the amount of points/values in the time series.
     */
    TS_DATA_AMOUNT_VALUES("ts_data_amountValues", SINGLE_VALUE),

    /**
     * The series key that can be a simple string or complex serialized object (for e.g. JSON) that will used
     * to identify the series data in databases of other applications than the Split-Source feature is in use.
     * This is necessary because Software-EKG and other software systems doesn't use similar attributes to
     * identify records in the database or make than unique.
     */
    TS_REMOTE_SERIES_KEY("ts_remoteSeriesKey", SINGLE_VALUE),

    /**
     * Field that stores an aggregated view on the values in the time series. This can
     * used than the aggression is often used and expensive in calculation.
     */
    TS_AGGREGATION_LEVEL("ts_ag", SINGLE_VALUE),

    /**
     * The data-time of the first value of the time series.
     * This is the exact moment than the series begins (with precession on milliseconds).
     */
    TS_START("ts_start", SINGLE_VALUE),

    /**
     * The data-time of the last value of the time series.
     * This is the exact moment than the series ends (with precession on milliseconds).
     */
    TS_STOP("ts_end", SINGLE_VALUE),

    /**
     * The data-time of the time series was imported/written to
     * the EKG repository (with precession on milliseconds).
     */
    TS_IMPORT_DATE("ts_importDate", SINGLE_VALUE),

    /**
     * A control flag used by the EKG Collector mark which document are already merged
     * in a batch job that collect many documents and merge it to a single one.
     */
    TS_IS_MERGED("ts_isMerged", SINGLE_VALUE),

    /**
     * A hash value over all filter dimensions that can use to identify series records
     * that belongs together by comparing/grouping only one attribute instead of multiple ones.
     * (primary used by EKG Collector)
     */
    TS_GROUP_KEY_HASH("ts_groupKeyHash", SINGLE_VALUE),


    //================================================================================================================
    //  types fields
    //================================================================================================================

    /**
     * Persisted field with unique id of the types
     */
    REPOSITORY_ID("id", SINGLE_VALUE),

    /**
     * Persisted field with the name of the project this types belongs to
     */
    REPOSITORY_ACCORDING_PROJECT("repository_accordingProject", SINGLE_VALUE),

    /**
     * Persisted field with the name of the types
     */
    REPOSITORY_NAME("repository_name", SINGLE_VALUE),

    /**
     * Persisted field with the types type (remote Solr classic, remote Solr cloud, remote ElasticSearch, local Solr)
     */
    REPOSITORY_DB_TYPE("repository_dbType", SINGLE_VALUE),

    /**
     * Persisted field with the types url used to fetch the data
     */
    REPOSITORY_URL("repository_url", SINGLE_VALUE),

    /**
     * Persisted field with the types url used to fetch the data
     */
    REPOSITORY_INDEX_NAME("repository_dbIndexName", SINGLE_VALUE),

    /**
     * Persisted field with a flag if the types is an import source
     */
    REPOSITORY_IS_IMPORTSOURCE("repository_isSource", SINGLE_VALUE),

    /**
     * Persisted field that stores the authentication type of the types (NONE, API_KEY, USER_PASSWORD)
     */
    REPOSITORY_AUTH_TYPE("repository_authType", SINGLE_VALUE),

    /**
     * Persisted field that stores the user name if credentials are required
     */
    REPOSITORY_USER("repository_user", SINGLE_VALUE),

    /**
     * Persisted field stores the user password or API key (depending of auth type that is in use)
     */
    REPOSITORY_PASSWORD("repository_pass", SINGLE_VALUE),

    /**
     * Persisted field to store temporary valid SSO Tokens insert manually or fetched using username/password
     */
    REPOSITORY_SSO_TOKEN("repository_ssoToken", SINGLE_VALUE),

    /**
     * Persisted field to store the absolute URL of the OAuth server that issued the access and refresh tokens.
     */
    REPOSITORY_OAUTH_SERVER_URL("repository_oauthServerUrl", SINGLE_VALUE),


    //================================================================================================================
    //  bookmark fields
    //================================================================================================================

    /**
     * The name of the bookmark that will displayed in the UI
     */
    BOOKMARK_NAME("bookmarkName", SINGLE_VALUE),

    /**
     * The name of the bookmark group the bookmark belongs to. If not defined 'GLOBAL'
     * will used as default.
     */
    BOOKMARK_GROUP_NAME("bookmarkGroupName", SINGLE_VALUE),

    /**
     * The id of the bookmark group this concrete bookmark belongs to.
     * If the bookmark didn't belong to a specific bookmark this value id will be _BOOKMARK_GLOBAL_
     */
    BOOKMARK_GROUP_ID("bookmarkGroupId", SINGLE_VALUE),

    /**
     * The description of the bookmark that provides further information's about the bookmark
     */
    BOOKMARK_DESCRIPTION("bookmarkDescription", SINGLE_VALUE),

    /**
     * The JSON serialized protocol of each chart action that were handled by the ProtocolManager
     */
    BOOKMARK_PROTOCOL("bookmarkProtocol", SINGLE_VALUE),

    /**
     * The timezone setting the user currently uses
     */
    BOOKMARK_TIME_ZONE("bookmarkTimeZone", SINGLE_VALUE);

    //================================================================================================================
    //  business logic
    //================================================================================================================


    private String name;

    private Multiplicity multiplicity;



    /**
     * Constructs a constant.
     *
     * @param name         Name of the solr field
     * @param multiplicity Multiplicity of the field
     */
    EkgSchemaField(String name, Multiplicity multiplicity) {
        this.name = name;
        this.multiplicity = multiplicity;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

}