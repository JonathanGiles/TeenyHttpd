package net.jonathangiles.tools.teenyhttpd.response;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

class EmptyResponse implements Response {
    final StatusCode statusCode;

    public EmptyResponse(final StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public List<String> getHeaders() {
        return Collections.emptyList();
    }

    @Override
    public void writeBody(BufferedOutputStream dataOut) throws IOException {
        // no-op
    }
}
