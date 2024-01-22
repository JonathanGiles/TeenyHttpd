package net.jonathangiles.tools.teenyhttpd.response;

import net.jonathangiles.tools.teenyhttpd.request.Request;

import java.util.List;

public class StringResponse extends ByteResponse {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[] { };

    public StringResponse(final StatusCode statusCode) {
        super(statusCode);
    }

    public StringResponse(final StatusCode statusCode, final List<String> headers) {
        super(statusCode, headers);
    }

    public StringResponse(final String body) {
        this(StatusCode.OK, body);
    }

    public StringResponse(final StatusCode statusCode, final String body) {
        super(statusCode, body == null ? EMPTY_BYTE_ARRAY : body.getBytes());
    }

    public StringResponse(final StatusCode statusCode, final List<String> headers, final String body) {
        super(statusCode, headers, body == null ? EMPTY_BYTE_ARRAY : body.getBytes());
    }
}
