package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.io.BufferedOutputStream;

public final class TypedResponseImpl<T> extends ResponseBase implements TypedResponse<T> {

    private T body;

    public TypedResponseImpl() {
        this(StatusCode.OK, null);
    }

    public TypedResponseImpl(StatusCode statusCode, T body) {
        super(statusCode);
        this.body = body;
    }

    @Override
    public TypedResponseImpl<T> setBody(T body) {
        this.body = body;
        return this;
    }

    @Override
    public TypedResponse<T> removeHeader(String key) {
        super.removeHeader(key);
        return this;
    }

    @Override
    public TypedResponse<T> addHeader(Header header) {
        super.addHeader(header);
        return this;
    }

    @Override
    public TypedResponse<T> addHeader(String key, String value) {
        super.addHeader(key, value);
        return this;
    }

    @Override
    public TypedResponse<T> addHeader(String key, String... values) {
        super.addHeader(key, values);
        return this;
    }

    @Override
    public TypedResponse<T> setHeader(Header header) {
        super.setHeader(header);
        return this;
    }

    @Override
    public TypedResponse<T> setHeader(String key, String value) {
        super.setHeader(key, value);
        return this;
    }

    @Override
    public TypedResponse<T> setHeader(String key, String... values) {
        super.setHeader(key, values);
        return this;
    }

    @Override
    public void writeBody(BufferedOutputStream dataOut) {
        throw new UnsupportedOperationException("This class does not support writing to an output stream.");
    }

    @Override
    public T getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "TypedResponseImpl{" +
                "statusCode=" + getStatusCode() +
                ", body=" + body +
                ", headers=" + getHeaders() +
                '}';
    }
}
