package net.jonathangiles.tools.teenyhttpd.json;

/**
 * A simple interface for serializing a value into a String.
 */
public interface ValueSerializer {
    String serialize(Object value);

}
