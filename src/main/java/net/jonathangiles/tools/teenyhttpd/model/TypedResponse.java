package net.jonathangiles.tools.teenyhttpd.model;

import net.jonathangiles.tools.teenyhttpd.implementation.TypedResponseImpl;

import java.util.Objects;

public interface TypedResponse<T> extends Response {

    T getBody();

    TypedResponse<T> setHeader(String key, String value);

    TypedResponse<T> setBody(T header);

    static <T> TypedResponse<T> ok() {
        return new TypedResponseImpl<>(StatusCode.OK, null);
    }

    static <T> TypedResponse<T> ok(T value) {
        return create(StatusCode.OK, value);
    }

    static <T> TypedResponse<T> status(StatusCode statusCode) {
        return create(Objects.requireNonNull(statusCode), null);
    }

    static <T> TypedResponse<T> create(StatusCode statusCode, T body) {
        return new TypedResponseImpl<>(statusCode, body);
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
