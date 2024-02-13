package net.jonathangiles.tools.teenyhttpd.winter.annot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Post {

    String value() default "/";
    String produces() default "application/json";
}
