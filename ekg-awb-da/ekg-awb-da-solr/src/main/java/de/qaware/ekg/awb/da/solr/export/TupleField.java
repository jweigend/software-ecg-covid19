package de.qaware.ekg.awb.da.solr.export;

import org.apache.solr.client.solrj.io.Tuple;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * A field of a {@link Tuple} as returned by the /export handler. Use this to inject the field's value into a POJO's
 * field.
 */
/* package-private */ abstract class TupleField {

    private final Field field;

    /**
     * Constructs a {@link TupleField}. Visible only to subclasses.
     *
     * @param field the {@link Field} of the target Java object into which the value will be injected
     */
    private TupleField(Field field) {
        this.field = field;
    }

    /**
     * Creates a {@link TupleField}.
     *
     * @param name  the name of the field in the {@link Tuple}
     * @param field the {@link Field} of the target Java object into which the value will be injected
     * @param type  the type of the field of the target Java object (e.g. Integer)
     * @return the {@link TupleField}
     */
    // Justification: This method is not complex, there are just many types to cover.
    @SuppressWarnings("squid:MethodCyclomaticComplexity")
    /* package-private */ static TupleField create(String name, Field field, Class<?> type) {
        field.setAccessible(true);

        if (type.equals(String.class)) {
            return new StringTupleField(field, name);

        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return new BooleanTupleField(field, name);

        } else if (type.equals(byte[].class)) {
            return new ByteArrayTupleField(field, name);

        } else if (type.equals(Date.class)) {
            return new DateTupleField(field, name);

        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return new DoubleTupleField(field, name);

        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return new FloatTupleField(field, name);

        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return new IntegerTupleField(field, name);

        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return new LongTupleField(field, name);

        } else if (type.equals(List.class)) {
            // Treat all Lists as List<String> as the generic type is difficult to find and all current Lists use String
            return new StringListTupleField(field, name);

        } else {
            throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    /**
     * Injects the value of this field of the given {@link Tuple} into the given {@link Object}.
     *
     * @param to   the target Object into which the field value is injected
     * @param from the source {@link Tuple} from which the field value is taken
     * @throws IllegalAccessException if the field can't be set via reflection
     */
    /* package-private */ void inject(Object to, Tuple from) throws IllegalAccessException {
        field.set(to, getValue(from));
    }

    /**
     * Extracts the value of this field from the given {@link Tuple}.
     *
     * @param from the source {@link Tuple} from which the field value is taken
     * @return the field's value
     */
    protected abstract Object getValue(Tuple from);

    private static final class DateTupleField extends TupleField {

        private final String name;

        private DateTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected Object getValue(Tuple from) {
            return from.getDate(name);
        }
    }

    private static final class DoubleTupleField extends TupleField {

        private final String name;

        private DoubleTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected Object getValue(Tuple from) {
            return from.getDouble(name);
        }
    }

    private static final class FloatTupleField extends TupleField {

        private final String name;

        private FloatTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected Object getValue(Tuple from) {
            Double aDouble = from.getDouble(name);

            if (aDouble == null) {
                return null;
            }

            return aDouble.floatValue();
        }
    }

    private static final class IntegerTupleField extends TupleField {

        private final String name;

        private IntegerTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected Object getValue(Tuple from) {
            Long obj = from.getLong(name);
            if (obj == null) {
                return null;
            }
            return obj.intValue();
        }
    }

    private static final class LongTupleField extends TupleField {

        private final String name;

        private LongTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected Object getValue(Tuple from) {
            return from.getLong(name);
        }
    }

    private static final class BooleanTupleField extends TupleField {

        private final String name;

        private BooleanTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected Object getValue(Tuple from) {
            return from.getBool(name);
        }
    }

    private static final class StringTupleField extends TupleField {

        private final String name;

        private StringTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected String getValue(Tuple from) {
            // We don't use getString() because it returns String value "null" for null
            return (String) from.get(name);
        }
    }

    private static final class ByteArrayTupleField extends TupleField {

        private final String name;

        private ByteArrayTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected byte[] getValue(Tuple from) {
            return (byte[]) from.get(name);
        }
    }

    private static final class StringListTupleField extends TupleField {

        private final String name;

        private StringListTupleField(Field field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        protected List<String> getValue(Tuple from) {
            return from.getStrings(name);
        }
    }

}
