package net.jonathangiles.tools.teenyhttpd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify a configuration class that should be used by the TeenyHttpd server.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {

    /**
     * The order in which this configuration class should be loaded. Lower numbers are loaded first.
     */
    int order() default 0;
}
