package net.jonathangiles.tools.teenyhttpd.winter.annot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a method parameter as a query param (request param). The value of the annotation is the name of the
 * query param to be extracted from the URL.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryParam {

    String value();
    boolean required() default false;
    String defaultValue() default "";
}
