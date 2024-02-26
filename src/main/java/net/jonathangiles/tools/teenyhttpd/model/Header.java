package net.jonathangiles.tools.teenyhttpd.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a request header.
 */
public class Header {

    public static final Pattern KEY_PATTERN = Pattern.compile("^[!#$%&'*+.^_`|~0-9A-Za-z-]+$");

    private final String keyValue;
    private String key;
    private List<String> values;

    public Header(final String keyValue) {
        this.keyValue = keyValue;
    }

    public Header(final String key, final String value) {
        this(key, Collections.singletonList(value));
    }

    public Header(final String key, final List<String> values) {
        this.keyValue = null;
        this.key = key;
        this.values = values;

        validateKey(key);
    }

    public String getKey() {
        if (key == null) {
            parse();
        }
        return key;
    }

    public List<String> getValues() {
        if (values == null) {
            parse();
        }
        return values;
    }

    public String getFirstValue() {
        return getValues().get(0);
    }

    @Override
    public String toString() {
        return getKey() + ": " + String.join(", ", getValues());
    }

    private void parse() {
        if (keyValue == null || keyValue.isEmpty()) {
            throw new IllegalArgumentException("keyValue must not be null or empty");
        }

        final String[] split = keyValue.split(":", 2);
        key = split[0].trim();
        values = Arrays.asList(split[1].split(","));
        values.replaceAll(String::trim);
    }

    private void validateKey(String key) {

        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty");
        }

        if (!Header.KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid header name: " + key);
        }
    }
}
