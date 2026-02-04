//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.repository.api.model;

import de.qaware.ekg.awb.repository.api.schema.EkgSchemaField;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * This class contains two fields required by all ET classes to be stored in persistence layer.
 */
public abstract class AbstractEt implements Serializable {

    private static final long serialVersionUID = -6751734116154672022L;

    /**
     * The row id.
     */
    @PersistedField(EkgSchemaField.ID)
    private String id;

    /**
     * represents the type of the record to identify it
     * in single table data models
     */
    @PersistedField(EkgSchemaField.DOC_TYPE)
    private String type;

    /**
     * Default constructor for internal purposes.
     */
    protected AbstractEt() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractEt)) {
            return false;
        }
        AbstractEt that = (AbstractEt) o;
        return new EqualsBuilder()
                .append(getId(), that.getId())
                .append(getType(), that.getType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getId())
                .append(getType())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .toString();
    }
}
