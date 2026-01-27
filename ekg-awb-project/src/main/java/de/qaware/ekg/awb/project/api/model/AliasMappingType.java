package de.qaware.ekg.awb.project.api.model;

import de.qaware.ekg.awb.sdk.core.NamedEnum;

/**
 * Enumeration of supported filter dimension mapping types.
 * The mapping feature supports to define individual label for
 * the fixed set of filter dimensions.
 */
public enum AliasMappingType implements NamedEnum {

    /**
     * EKG default filter dimensions (like host, metric, pod or namespace)
     */
    EKG_STANDARD ("EKG Standard"),

    /**
     * Importer specific mapping that are optional provided by each importer module
     */
    IMPORTER_SPECIFIC ("Importer specific filter dimensions"),

    /**
     * User specific mappings defined per project
     */
    CUSTOM ("User specific filter dimensions");

    private String name;


    AliasMappingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
