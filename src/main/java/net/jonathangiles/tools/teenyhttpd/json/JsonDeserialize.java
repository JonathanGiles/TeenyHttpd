package net.jonathangiles.tools.teenyhttpd.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify how to deserialize a JSON string into a Java object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonDeserialize {

    Class<?> contentAs() default Object.class;

    Class<?> as() default Object.class;
}
