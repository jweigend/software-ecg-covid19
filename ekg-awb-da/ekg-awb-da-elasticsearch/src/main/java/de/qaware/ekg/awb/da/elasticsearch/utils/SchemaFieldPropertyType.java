package de.qaware.ekg.awb.da.elasticsearch.utils;

public enum SchemaFieldPropertyType {

    FIELD_TYPE("type"),

    INDEXED("index"),

    STORED("store"),

    IS_DOC_VALUE("doc_values");

    private String type;

    SchemaFieldPropertyType(String type) {
        this.type = type;
    }

    public String getKey() {
       return type;
    }
}
