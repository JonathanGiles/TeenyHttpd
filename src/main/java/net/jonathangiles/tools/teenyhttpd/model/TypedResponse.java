package net.jonathangiles.tools.teenyhttpd.model;

import net.jonathangiles.tools.teenyhttpd.implementation.TypedResponseImpl;

public interface TypedResponse<T> extends Response {

    T getValue();

    TypedResponse<T> header(String key, String value);

    static <T> TypedResponse<T> ok() {
        return new TypedResponseImpl<>(StatusCode.OK, null);
    }

    static <T> TypedResponse<T> ok(T value) {
        return of(value, StatusCode.OK);
    }

    static <T> TypedResponse<T> status(StatusCode statusCode) {
        return of(null, statusCode);
    }

    static <T> TypedResponse<T> of(T value, StatusCode statusCode) {
        return new TypedResponseImpl<>(statusCode, value);
    }

    static <T> TypedResponse<T> noContent() {
        return new TypedResponseImpl<>(StatusCode.NO_CONTENT, null);
    }

    static <T> TypedResponse<T> notFound() {
        return new TypedResponseImpl<>(StatusCode.NOT_FOUND, null);
    }

    static <T> TypedResponse<T> badRequest() {
        return new TypedResponseImpl<>(StatusCode.BAD_REQUEST, null);
    }
}
