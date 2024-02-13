package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ByteResponse extends ResponseBase {
    final byte[] body;

//    public ByteResponse(final StatusCode statusCode) {
//        this(statusCode, Collections.emptyList());
//    }

//    public ByteResponse(final StatusCode statusCode, final List<Header> headers) {
//        this(statusCode, headers, null);
//    }

    public ByteResponse(final StatusCode statusCode, final byte[] body) {
        this(statusCode, Collections.emptyList(), body);
    }

    public ByteResponse(final StatusCode statusCode, final List<Header> headers, final byte[] body) {
        super(statusCode, headers);
        this.body = body;
    }

    @Override
    public long getBodyLength() {
        return body == null ? 0 : body.length;
    }

    @Override
    public void writeBody(BufferedOutputStream dataOut) throws IOException {
        if (body != null) {
            dataOut.write(body, 0, body.length);
            dataOut.flush();
        }
    }
}
