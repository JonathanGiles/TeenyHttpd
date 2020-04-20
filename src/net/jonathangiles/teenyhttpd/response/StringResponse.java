package net.jonathangiles.teenyhttpd.response;

import net.jonathangiles.teenyhttpd.request.Request;

import java.util.List;

public class StringResponse extends ByteResponse {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[] { };

    public StringResponse(final Request request, final StatusCode statusCode) {
        super(request, statusCode);
    }

    public StringResponse(final Request request, final StatusCode statusCode, final List<String> headers) {
        super(request, statusCode, headers);
    }

    public StringResponse(final Request request, final StatusCode statusCode, final String body) {
        super(request, statusCode, body == null ? EMPTY_BYTE_ARRAY : body.getBytes());
    }

    public StringResponse(final Request request, final StatusCode statusCode, final List<String> headers, final String body) {
        super(request, statusCode, headers, body == null ? EMPTY_BYTE_ARRAY : body.getBytes());
    }
}
