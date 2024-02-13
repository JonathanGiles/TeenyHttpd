package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Response;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.util.ArrayList;
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
    public void setHeader(Header header) {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        headers.add(header);
    }
}
