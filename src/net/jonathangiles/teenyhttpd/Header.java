package net.jonathangiles.teenyhttpd;

public class Header {
    private final String keyValue;

    public Header(String keyValue) {
        this.keyValue = keyValue;
    }

    @Override
    public String toString() {
        return keyValue;
    }
}
