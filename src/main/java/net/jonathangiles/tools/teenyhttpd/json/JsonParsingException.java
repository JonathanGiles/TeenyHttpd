package net.jonathangiles.tools.teenyhttpd.json;

/**
 * An exception that is thrown when there is an error parsing a JSON string.
 */
public class JsonParsingException extends Exception {

    public JsonParsingException(String message) {
        super(message);
    }

    public JsonParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
