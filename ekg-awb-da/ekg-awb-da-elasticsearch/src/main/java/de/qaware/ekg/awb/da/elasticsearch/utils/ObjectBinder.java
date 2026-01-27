package de.qaware.ekg.awb.da.elasticsearch.utils;

import de.qaware.ekg.awb.repository.api.schema.FieldReader;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectBinder {

    public static Map<String, Object> mapToContentMap(Object ekgEntity) {

        Map<String, Field> fieldMap = FieldReader.getSchemaFields(ekgEntity.getClass());

        if (fieldMap.isEmpty()) {
            throw new IllegalStateException("class "+ ekgEntity.getClass().getName() + " does not define any fields.");
        }

        Map<String, Object> jsonMap = new HashMap<>();

        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            try {
                Object value = FieldUtils.readField(entry.getValue(), ekgEntity, true);

                if (value != null) {

                    if (value instanceof Date) {
                        jsonMap.put(entry.getKey(), ((Date) value).getTime());
                    } else {
                        jsonMap.put(entry.getKey(), value);
                    }
                }

            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to read field '" + entry.getValue().getName()
                        + "' of  class"+ ekgEntity.getClass().getName() + "' because access is forbidden.", e);
            }
        }

        return jsonMap;
    }

    public static <T> List<T> mapToBeans(Class<T> type, SearchHits hits) {
        try {
            Map<String, Field> fieldMap = FieldReader.getSchemaFields(type);

            List<T> resultList = new ArrayList<>();

            for (SearchHit document : hits.getHits()) {
                T bean = ConstructorUtils.invokeConstructor(type);
                Map<String, Object> documentFieldMap = document.getSourceAsMap();

                for (Map.Entry<String, Field> beanFieldEntry : fieldMap.entrySet()) {

                    String fieldNameSolr = beanFieldEntry.getKey();
                    Field beanField = beanFieldEntry.getValue();

                    if (!documentFieldMap.containsKey(fieldNameSolr)) {
                         continue;
                    }

                    Object value = documentFieldMap.get(fieldNameSolr);

                    if (beanField.getType() == Date.class && value instanceof Long) {
                        value = new Date((Long) value);
                    } else {
                        value = documentFieldMap.get(fieldNameSolr);
                    }

                    if (beanField.getType() == byte[].class && value instanceof String) {
                        value = Base64.getDecoder().decode(value.toString());
                    }

                    FieldUtils.writeField(beanField, bean, value, true);
                }

                resultList.add(bean);
            }

            return resultList;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to map Solr result to domain entity of type '" + type.getName(), e);
        }
    }
}
