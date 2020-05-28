package net.jonathangiles.tools.teenyhttpd.response;

import net.jonathangiles.tools.teenyhttpd.request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class Response {
    private final Request request;

    protected Response(final Request request) {
        this.request = request;
    }

    protected Request getRequest() {
        return request;
    }

    public abstract StatusCode getStatusCode();

    public abstract List<String> getHeaders();

    public abstract void writeBody(BufferedOutputStream dataOut) throws IOException;
}
