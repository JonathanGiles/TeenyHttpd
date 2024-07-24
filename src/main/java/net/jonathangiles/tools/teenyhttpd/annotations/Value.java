package net.jonathangiles.tools.teenyhttpd.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to be used on fields in a class that is annotated with {@link Configuration}.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface Value {

    String value();

    boolean required() default true;
}
