package net.jonathangiles.tools.teenyhttpd.response;

import net.jonathangiles.tools.teenyhttpd.request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ByteResponse extends Response {
    final StatusCode statusCode;
    final List<String> headers;
    final byte[] body;

    public ByteResponse(final Request request, final StatusCode statusCode) {
        this(request, statusCode, Collections.emptyList());
    }

    public ByteResponse(final Request request, final StatusCode statusCode, final List<String> headers) {
        this(request, statusCode, headers, null);
    }

    public ByteResponse(final Request request, final StatusCode statusCode, final byte[] body) {
        this(request, statusCode, Collections.emptyList(), body);
    }

    public ByteResponse(final Request request, final StatusCode statusCode, final List<String> headers, final byte[] body) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public void writeBody(BufferedOutputStream dataOut) throws IOException {
        if (body != null) {
            dataOut.write(body, 0, body.length);
            dataOut.flush();
        }
    }
}
