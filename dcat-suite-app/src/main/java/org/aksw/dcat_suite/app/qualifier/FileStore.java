package org.aksw.dcat_suite.app.qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Qualifier for {@link Path} instances (folders) that act as storage for files
 *
 * @author raven
 *
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface FileStore {
    String value() default "";
}
