package net.jonathangiles.tools.teenyhttpd.annotations.doc;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Schema {

    int maxLength() default Integer.MAX_VALUE;

    int minLength() default 0;

    String pattern() default "";

    String format() default "";

    String description() default "";

    boolean required() default false;

    String minimum() default "";

    String maximum() default "";
}
