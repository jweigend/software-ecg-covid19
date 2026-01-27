package de.qaware.ekg.awb.repository.api.schema;

/**
 * A field of an entity with its attributes.
 */
public interface Field {

    /**
     * The field's multiplicity, i.e. single value or multi value.
     */
    enum Multiplicity {
        /**
         * The field can contain maximum one value.
         */
        SINGLE_VALUE,
        /**
         * The field can contain multiple values.
         */
        MULTI_VALUE
    }

    /**
     * Returns the name of the field.
     *
     * @return the name of the field
     */
    String getName();

    /**
     * Returns the multiplicity of the field.
     *
     * @return the multiplicity
     */
    Multiplicity getMultiplicity();
}
