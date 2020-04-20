package net.jonathangiles.teenyhttpd;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class SimpleResponse extends Response {
    final String[] headers;
    final byte[] body;

    public SimpleResponse(final String[] headers, final byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    @Override
    protected void send(PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (headers != null && headers.length > 0) {
            for (String header : headers) {
                out.println(header);
            }
            out.println(); // empty line between header and body
            out.flush();   // flush character output stream buffer
        }

        if (body != null) {
            dataOut.write(body, 0, body.length);
            dataOut.flush();
        }
    }
}
