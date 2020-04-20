package net.jonathangiles.teenyhttpd;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class Response {
    private final Request request;

    protected Response(Request request) {
        this.request = request;
    }

    protected Request getRequest() {
        return request;
    }

    protected abstract void send(PrintWriter out, BufferedOutputStream dataOut) throws IOException;
}
