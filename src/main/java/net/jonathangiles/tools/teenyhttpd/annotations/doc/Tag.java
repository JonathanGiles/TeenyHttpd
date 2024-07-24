package net.jonathangiles.tools.teenyhttpd.annotations.doc;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(Tags.class)
public @interface Tag {
    String value();
}
