package net.jonathangiles.teenyhttpd;

public class Request {
    private final String method;
    private final String path;

    public Request(final String method, final String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
