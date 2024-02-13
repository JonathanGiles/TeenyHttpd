package net.jonathangiles.tools.teenyhttpd.model;

public enum Headers {
    ACCEPT("Accept"),
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length");

    private final String key;

    Headers(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Header asHeader(final String value) {
        return new Header(key, value);
    }

    public Header asHeader(final Object value) {
        return new Header(key, value.toString());
    }
}
