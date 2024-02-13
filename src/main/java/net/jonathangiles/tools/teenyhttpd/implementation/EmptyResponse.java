package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.io.BufferedOutputStream;
import java.util.List;

public class EmptyResponse extends ResponseBase {
    public EmptyResponse(final StatusCode statusCode) {
        super(statusCode);
    }

    public EmptyResponse(final StatusCode statusCode, final List<Header> headers) {
        super(statusCode, headers);
    }

    @Override
    public void writeBody(BufferedOutputStream dataOut) {
        // no-op
    }
}
