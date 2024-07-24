package net.jonathangiles.tools.teenyhttpd.annotations;

import net.jonathangiles.tools.teenyhttpd.configuration.OpenApiConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to enable the OpenApi configuration class to be used by the TeenyHttpd server.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Enable(OpenApiConfiguration.class)
public @interface EnableOpenApi {
}
