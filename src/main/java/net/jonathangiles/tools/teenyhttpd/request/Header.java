package net.jonathangiles.tools.teenyhttpd.request;

/**
 * Represents a request header.
 */
public class Header {
    private final String keyValue;
    private String key;
    private String value;

    public Header(final String keyValue) {
        this.keyValue = keyValue;
    }

    public String getKey() {
        if (key == null) {
            parse();
        }
        return key;
    }

    public String getValue() {
        if (value == null) {
            parse();
        }
        return value;
    }

    @Override
    public String toString() {
        return getKey() + ": " + getValue();
    }

    private void parse() {
        final String[] split = keyValue.split(":", 2);
        key = split[0].trim();
        value = split[1].trim();
    }
}
