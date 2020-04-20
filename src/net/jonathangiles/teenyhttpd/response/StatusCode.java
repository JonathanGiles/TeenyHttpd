package net.jonathangiles.teenyhttpd.response;

public enum StatusCode {
    // https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html

    OK(200, "OK"),
    FILE_NOT_FOUND(404, "File Not Found"),
    NOT_IMPLEMENTED(501, "Not Implemented");

    private final int code;
    private final String reasonPhrase;
    private final String fullString;

    StatusCode(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
        this.fullString = "HTTP/1.1 " + code + " " + reasonPhrase;
    }

    @Override
    public String toString() {
        return fullString;
    }
}
