package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.io.BufferedOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypedResponseImpl<T> implements TypedResponse<T> {

    private final StatusCode statusCode;
    private T value;
    private final Map<String, List<String>> headers;

    public TypedResponseImpl() {
        this.statusCode = StatusCode.OK;
        this.value = null;
        this.headers = new HashMap<>();
    }

    public TypedResponseImpl(StatusCode statusCode, T value) {
        this.statusCode = statusCode;
        this.value = value;
        this.headers = new HashMap<>();
    }

    public TypedResponseImpl(StatusCode statusCode, T value, Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        this.value = value;
        this.headers = headers;
    }

    @Override
    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public void setHeader(Header header) {
        headers.put(header.getKey(), header.getValues());
    }

    @Override
    public TypedResponse<T> header(String key, String value) {
        if (value == null) {
            return this;
        }

        headers.put(key, List.of(value));

        return this;
    }

    @Override
    public List<Header> getHeaders() {
        return headers.entrySet().stream()
                .map(entry -> new Header(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void writeBody(BufferedOutputStream dataOut) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T getValue() {
        return value;
    }
}
