package de.qaware.ekg.awb.da.solr.utils;

import de.qaware.ekg.awb.repository.api.schema.FieldReader;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An converter that maps bean form EKG to Solr entities.
 */
public class ObjectBinder {

    public static SolrInputDocument mapToSolrInputDocument(Object ekgEntity) {
        Map<String, Field> fieldMap = FieldReader.getSchemaFields(ekgEntity.getClass());

        if (fieldMap.isEmpty()) {
            throw new IllegalStateException("class "+ ekgEntity.getClass().getName() + " does not define any fields.");
        }

        SolrInputDocument inputDocument = new SolrInputDocument();

        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            try {
                Object value = FieldUtils.readField(entry.getValue(), ekgEntity, true);

                if (value != null) {
                    inputDocument.addField(entry.getKey(), value);
                }

            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to read field '" + entry.getValue().getName()
                        + "' of  class"+ ekgEntity.getClass().getName() + "' because access is forbidden.", e);
            }
        }

        return inputDocument;
    }

    public static <T> List<T> mapToBean(Class<T> type, SolrDocumentList results) {
        try {
            Map<String, Field> fieldMap = FieldReader.getSchemaFields(type);

            List<T> resultList = new ArrayList<>();

            for (SolrDocument document : results) {
                T bean = ConstructorUtils.invokeConstructor(type);

                for (Map.Entry<String, Field> beanFieldEntry : fieldMap.entrySet()) {
                    String fieldNameSolr = beanFieldEntry.getKey();
                    Field beanField = beanFieldEntry.getValue();
                    FieldUtils.writeField(beanField, bean, document.getFieldValue(fieldNameSolr), true);
                }

                resultList.add(bean);
            }

            return resultList;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to map Solr result to domain entity of type '" + type.getName(), e);
        }
    }
}
