package de.qaware.ekg.awb.repository.api.schema;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Utility class to read the {@link PersistedField} annotations from entities.
 */
public final class FieldReader {

    private FieldReader() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the Schema fields on the pojo class, as indicated by the
     * {@link PersistedField} annotations.
     * <p>
     * The implementation does only check fields, not methods, for annotations.
     *
     * @param clazz the class to inspect
     * @return all Solr fields with name and type
     */
    public static Map<String, Field> getSchemaFields(Class clazz) {
        Map<String, Field> solrFields = new HashMap<>();

        Class superClazz = clazz;

        List<Field> fields = new ArrayList<>();
        while (superClazz != null && superClazz != Object.class) {
            fields.addAll(Arrays.asList(superClazz.getDeclaredFields()));
            superClazz = superClazz.getSuperclass();
        }
        for (Field field : fields) {
            PersistedField annotation =
                    field.getAnnotation(PersistedField.class);
            if (annotation != null) {
                solrFields.put(annotation.value().getName(), field);
            }
        }

        return solrFields;
    }
}
