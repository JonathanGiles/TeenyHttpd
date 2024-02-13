package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.util.List;

public class StringResponse extends ByteResponse {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[] { };

    public StringResponse(final String body) {
        this(StatusCode.OK, body);
    }

    public StringResponse(final StatusCode statusCode, final String body) {
        super(statusCode, body == null ? EMPTY_BYTE_ARRAY : body.getBytes());
    }

    public StringResponse(final StatusCode statusCode, final List<Header> headers, final String body) {
        super(statusCode, headers, body == null ? EMPTY_BYTE_ARRAY : body.getBytes());
    }
}
