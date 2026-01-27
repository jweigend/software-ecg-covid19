package de.qaware.ekg.awb.repository.api.schema;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to specify to which document field in the
 * persistence layer a member variable belongs to.
 * This must be match to the underlying schema of the database.
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface PersistedField {

    EkgSchemaField value();

    boolean child() default false;
}
