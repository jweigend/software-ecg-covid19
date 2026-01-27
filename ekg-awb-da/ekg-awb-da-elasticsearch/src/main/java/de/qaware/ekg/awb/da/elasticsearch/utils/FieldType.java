package de.qaware.ekg.awb.da.elasticsearch.utils;

public enum FieldType {

    TEXT("text"),

    KEYWORD("keyword"),

    BOOLEAN("boolean"),

    BINARY("binary"),

    DATE("date"),

    LONG("long"),

    INT("int");

    private String type;

    FieldType(String type) {
        this.type = type;
    }

    public String getTypeKey() {
        return type;
    }
}
