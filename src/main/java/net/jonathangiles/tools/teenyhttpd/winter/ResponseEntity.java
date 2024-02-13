package net.jonathangiles.tools.teenyhttpd.winter;


import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple class to represent a response entity, which contains a status code and a body.
 */
public class ResponseEntity<T> {

    private final StatusCode status;
    private final T body;
    private List<Header> headers;

    public ResponseEntity(StatusCode status, T body) {
        this.status = status;
        this.body = body;
        this.headers = new ArrayList<>();
    }

    public ResponseEntity<T> headers(List<Header> headers) {
        this.headers = headers;
        return this;
    }

    public StatusCode getStatus() {
        return status;
    }

    public T getBody() {
        return body;
    }

    public List<Header> getHeaders() {
        return headers;
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
