package net.jonathangiles.teenyhttpd;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

public class FileResponse extends Response {
    private final String[] headers;
    private final File file;

    public FileResponse(final String[] headers, final File file) {
        this.headers = headers;
        this.file = file;
    }

    @Override
    protected void send(final PrintWriter out, final BufferedOutputStream dataOut) throws IOException {
        if (headers != null && headers.length > 0) {
            for (String header : headers) {
                out.println(header);
            }
            out.println(); // empty line between header and body
            out.flush();   // flush character output stream buffer
        }

        Files.copy(file.toPath(), dataOut);
        dataOut.flush();
    }
}
