package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.io.BufferedOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TypedResponseImpl<T> implements TypedResponse<T> {

    private final StatusCode statusCode;
    private T body;
    private final Map<String, List<String>> headers;

    public TypedResponseImpl() {
        this(StatusCode.OK, null);
    }

    public TypedResponseImpl(StatusCode statusCode, T body) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>();
    }

    @Override
    public TypedResponseImpl<T> setBody(T body) {
        this.body = body;
        return this;
    }

    @Override
    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public void setHeader(Header header) {
        Objects.requireNonNull(header, "Header cannot be null");
        headers.put(header.getKey(), header.getValues());
    }

    static Pattern HEADER_PATTERN = Pattern.compile("^[!#$%&'*+.^_`|~0-9A-Za-z-]+$");

    @Override
    public TypedResponse<T> setHeader(String key, String value) {

        if (!HEADER_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid header name");
        }

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
        throw new UnsupportedOperationException("This class does not support writing to an output stream.");
    }

    @Override
    public T getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "TypedResponseImpl{" +
                "statusCode=" + statusCode +
                ", body=" + body +
                ", headers=" + headers +
                '}';
    }
}
