package net.jonathangiles.tools.teenyhttpd.model;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.jonathangiles.tools.teenyhttpd.implementation.ByteResponse;
import net.jonathangiles.tools.teenyhttpd.implementation.EmptyResponse;
import net.jonathangiles.tools.teenyhttpd.implementation.FileResponse;
import net.jonathangiles.tools.teenyhttpd.implementation.StringResponse;

public interface Response {

    static Response create(final StatusCode statusCode) {
        return new EmptyResponse(statusCode);
    }

    static Response create(final StatusCode statusCode, final List<Header> headers) {
        return new EmptyResponse(statusCode, headers);
    }

    static Response create(final StatusCode statusCode, final byte[] body) {
        return new ByteResponse(statusCode, body);
    }

    static Response create(final StatusCode statusCode, final List<Header> headers, final byte[] body) {
        return new ByteResponse(statusCode, headers, body);
    }

    static Response create(final String body) {
        return new StringResponse(body);
    }

    static Response create(final StatusCode statusCode, final String body) {
        return new StringResponse(statusCode, body);
    }

    static Response create(final StatusCode statusCode, final List<Header> headers, final String body) {
        return new StringResponse(statusCode, headers, body);
    }

    static Response createFileResponse(final Request request) {
        return new FileResponse(request);
    }

    StatusCode getStatusCode();

    default List<Header> getHeaders() {
        return Collections.emptyList();
    }

    void setHeader(Header header);

    void writeBody(BufferedOutputStream dataOut) throws IOException;
}
