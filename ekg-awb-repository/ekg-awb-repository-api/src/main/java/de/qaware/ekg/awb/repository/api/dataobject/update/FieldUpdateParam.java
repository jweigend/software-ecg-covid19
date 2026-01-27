package de.qaware.ekg.awb.repository.api.dataobject.update;

import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;

/**
 * Update parameter that used to specify an atomic update
 * of a single field. If multiple fields should update
 * a collection of these params are necessary.
 */
public class FieldUpdateParam {

    private EkgSchemaField field;

    private FieldUpdateAction action;

    private Object value;

    public FieldUpdateParam(EkgSchemaField field, FieldUpdateAction action, Object value) {
        this.field = field;
        this.action = action;
        this.value = value;
    }

    public EkgSchemaField getField() {
        return field;
    }

    public FieldUpdateAction getAction() {
        return action;
    }

    public Object getValue() {
        return value;
    }
}
