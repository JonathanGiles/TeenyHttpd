package net.jonathangiles.tools.teenyhttpd.implementation;


import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Response;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic response that is used to infer the response body type.
 */
public class ResponseEntity<T> implements Response {

    private final StatusCode statusCode;
    private final T body;
    private List<Header> headers;

    public ResponseEntity(StatusCode statusCode, T body) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new ArrayList<>();
    }

    public ResponseEntity<T> headers(List<Header> headers) {
        this.headers = headers;
        return this;
    }

    public T getBody() {
        return body;
    }

    @Override
    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public List<Header> getHeaders() {
        return headers;
    }

    @Override
    public void setHeader(Header header) {
        headers.add(header);
    }

    @Override
    public void writeBody(BufferedOutputStream dataOut) throws IOException {

    }

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(StatusCode.OK, body);
    }

    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(StatusCode.NOT_FOUND, null);
    }

    public static <T> ResponseEntity<T> badRequest() {
        return new ResponseEntity<>(StatusCode.BAD_REQUEST, null);
    }

    public static <T> ResponseEntity<T> internalServerError() {
        return new ResponseEntity<>(StatusCode.INTERNAL_SERVER_ERROR, null);
    }

    public static <T> ResponseEntity<T> unauthorized() {
        return new ResponseEntity<>(StatusCode.UNAUTHORIZED, null);
    }

    public static <T> ResponseEntity<T> forbidden() {
        return new ResponseEntity<>(StatusCode.FORBIDDEN, null);
    }

    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(StatusCode.CREATED, body);
    }

    public static <T> ResponseEntity<T> noContent() {
        return new ResponseEntity<>(StatusCode.NO_CONTENT, null);
    }
}
