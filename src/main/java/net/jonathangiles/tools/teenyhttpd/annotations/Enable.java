package net.jonathangiles.tools.teenyhttpd.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to enable a specific configuration class to be used by the TeenyHttpd server.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Enables.class)
public @interface Enable {

    Class<?> value();

    int order() default 0;

}
