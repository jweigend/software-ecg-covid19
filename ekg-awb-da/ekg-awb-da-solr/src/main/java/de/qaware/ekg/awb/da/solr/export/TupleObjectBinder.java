package de.qaware.ekg.awb.da.solr.export;

import org.apache.solr.client.solrj.beans.BindingException;
import org.apache.solr.client.solrj.io.Tuple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A binder that converts {@link Tuple}s as returned by the Solr /export handler into Objects of classes with
 * Solr {@link org.apache.solr.client.solrj.beans.Field} annotations.
 */
/* package-private */ class TupleObjectBinder {

    private final Map<Class, BinderConfig> fieldCache = new ConcurrentHashMap<>();

    /**
     * Creates a new bean (POJO) filled with the values from the given {@link Tuple}.
     *
     * @param binderConfig Configuration for the binder providing the bean classes' annotated fields
     * @param tuple        the Solr {@link Tuple} containing raw key/value pairs
     * @param <T>          the class type for the beans
     * @return the created bean
     */
    /* package-private */ <T> T getBean(BinderConfig<T> binderConfig, Tuple tuple) {

        try {
            T obj = binderConfig.getClazz().newInstance();
            for (TupleField tupleField : binderConfig.getTupleFields()) {
                tupleField.inject(obj, tuple);
            }
            return obj;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new BindingException("Could not instantiate object of " + binderConfig.getClazz(), e);
        }
    }

    /**
     * Returns a {@link BinderConfig} that can be used in the {@link TupleObjectBinder}. As creating such a config
     * is expensive it is only created once per class and cached.
     *
     * @param clazz the class for the {@link BinderConfig}
     * @param <T>   the type of the class modeled by this Class object
     * @return the {@link BinderConfig}
     */
    @SuppressWarnings("unchecked")
    // Justification: The cache can not use T, but we know the requested element is T
    /* package-private */ <T> BinderConfig<T> getBinderConfig(Class<T> clazz) {
        return (BinderConfig<T>) fieldCache.computeIfAbsent(clazz, BinderConfig::create);
    }

}
