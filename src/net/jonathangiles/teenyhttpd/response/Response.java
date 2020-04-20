package net.jonathangiles.teenyhttpd.response;

import net.jonathangiles.teenyhttpd.Request;
import net.jonathangiles.teenyhttpd.StatusCode;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class Response {
    private final Request request;

    protected Response(Request request) {
        this.request = request;
    }

    protected Request getRequest() {
        return request;
    }

    public abstract StatusCode getStatusCode();

    public abstract List<String> getHeaders();

    public abstract void writeBody(BufferedOutputStream dataOut) throws IOException;
}
