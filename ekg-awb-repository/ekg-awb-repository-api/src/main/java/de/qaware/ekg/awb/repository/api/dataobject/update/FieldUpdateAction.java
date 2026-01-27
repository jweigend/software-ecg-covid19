package de.qaware.ekg.awb.repository.api.dataobject.update;

/**
 * An enum that specifies the different update actions
 * that are supported on atomic fields of records persisted
 * in the EKG Repository.
 */
public enum FieldUpdateAction {

    /**
     * Set or replace the field value(s) with the specified value(s),
     * or remove the values if 'null' or empty list is specified as the new value.
     *
     * May be specified as a single value, or as a list for multiValued fields
     */
    SET,

    /**
     * Adds the specified values to a multiValued field.
     * May be specified as a single value, or as a list.
     */
    ADD,

    /**
     * Removes (all occurrences of) the specified values from a multiValued field.
     * May be specified as a single value, or as a list.
     */
    REMOVE
}
