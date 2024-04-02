package net.jonathangiles.tools.teenyhttpd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify the packages that should be scanned for classes that are annotated with
 * TeenyHttpd annotations.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanPackage {

    String[] value();
}
