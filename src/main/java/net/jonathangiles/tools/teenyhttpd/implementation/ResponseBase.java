package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Response;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ResponseBase implements Response {
    private StatusCode statusCode;
    private List<Header> headers;

    ResponseBase() {

    }

    public ResponseBase(final StatusCode statusCode) {
        this(statusCode, new ArrayList<>());
    }

    public ResponseBase(final StatusCode statusCode, final List<Header> headers) {
        this.statusCode = statusCode;
        this.headers = headers;
    }

    @Override
    public StatusCode getStatusCode() {
        return statusCode;
    }

    void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public List<Header> getHeaders() {
        return headers;
    }

    @Override
    public Response removeHeader(String key) {
        if (headers == null) {
            return this;
        }

        headers.removeIf(h -> h.getKey().equalsIgnoreCase(key));

        return this;
    }

    @Override
    public Response setHeader(Header header) {
        if (headers == null) {
            headers = new ArrayList<>();
        }

        headers.removeIf(h -> h.getKey().equalsIgnoreCase(header.getKey()));

        headers.add(header);

        return this;
    }


    public Response setHeader(String key, String value) {

        if (value == null) {
            return this;
        }

        setHeader(new Header(key, value));

        return this;
    }

    public Response setHeader(String key, String... values) {
        if (values == null || values.length == 0) {
            return this;
        }

        setHeader(new Header(key, Arrays.asList(values)));

        return this;
    }

    @Override
    public Response addHeader(String key, String... values) {
        if (values == null || values.length == 0) {
            return this;
        }

        return addHeader(new Header(key, Arrays.asList(values)));
    }

    @Override
    public Response addHeader(String key, String value) {
        return addHeader(new Header(key, value));
    }

    @Override
    public Response addHeader(Header header) {
        Header target = null;

        for (int i = 0; i < headers.size(); i++) {
            Header h = headers.get(i);
            if (h.getKey().equalsIgnoreCase(header.getKey())) {
                target = h;
                headers.remove(i);
                break;
            }
        }

        if (target != null) {

            List<String> values = new ArrayList<>(target.getValues());
            values.addAll(header.getValues());

            headers.add(new Header(target.getKey(), values));
            return this;
        }

        headers.add(header);
        return this;
    }

}
