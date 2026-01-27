package de.qaware.ekg.awb.da.solr.export;

import de.qaware.ekg.awb.repository.api.schema.FieldReader;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Configuration of a binder for a {@link Class} with Solr {@link org.apache.solr.client.solrj.beans.Field}
 * annotations. It builds a list of all {@link TupleField}s of that class which can be used to inject field values
 * into objects of that class.
 *
 * @param <T> the type of the class for which this Binder was created
 */
/* package-private */ final class BinderConfig<T> {
    private final Class<T> clazz;
    private final List<TupleField> tupleFields;

    private BinderConfig(Class<T> clazz, List<TupleField> tupleFields) {
        this.clazz = clazz;
        this.tupleFields = new ArrayList<>(tupleFields);
    }

    /**
     * Creates a {@link BinderConfig} for the given {@link Class} with Solr
     * {@link org.apache.solr.client.solrj.beans.Field} annotations.
     *
     * @param clazz the class
     * @param <T>   the type of the class modelled by this Class object
     * @return the {@link BinderConfig}
     */
    /* package-private */
    static <T> BinderConfig<T> create(Class<T> clazz) {
        Map<String, TupleField> tupleFieldMap = new HashMap<>();

        Map<String, Field> solrFields = FieldReader.getSchemaFields(clazz);
        for (Map.Entry<String, Field> solrField : solrFields.entrySet()) {
            String name = solrField.getKey();
            Field field = solrField.getValue();
            tupleFieldMap.computeIfAbsent(name, x -> TupleField.create(name, field, field.getType()));
        }

        return new BinderConfig<>(clazz, new ArrayList<>(tupleFieldMap.values()));
    }

    /**
     * Returns the class for which this {@link BinderConfig} holds the {@link TupleField}s.
     *
     * @return the class
     */
    /* package-private */ Class<T> getClazz() {
        return clazz;
    }

    /**
     * Returns the list of all {@link TupleField}s in the class for which the {@link BinderConfig} was created.
     * It can be used to inject field values into objects of the class.
     *
     * @return the {@link TupleField}s
     */
    /* package-private */ List<TupleField> getTupleFields() {
        return Collections.unmodifiableList(tupleFields);
    }
}
