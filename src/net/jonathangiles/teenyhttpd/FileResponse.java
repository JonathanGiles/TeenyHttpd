package net.jonathangiles.teenyhttpd;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class FileResponse extends Response {

    public FileResponse(final Request request) {
        super(request);
    }

    @Override
    protected void send(final PrintWriter out, final BufferedOutputStream dataOut) throws IOException {
        final String method = getRequest().getMethod();
        String path = getRequest().getPath();

        File fileToReturn = null;
        String response = null;

        // we support only GET and HEAD methods, we check
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            // we return the not supported file to the client
            fileToReturn = getFile(TeenyHttpd.METHOD_NOT_SUPPORTED);
            response = "HTTP/1.1 501 Not Implemented";
        } else {
            // GET or HEAD method
            if (path.endsWith("/")) {
                path += TeenyHttpd.DEFAULT_FILE;
            }

            if ("GET".equals(method)) { // GET method so we return content
                fileToReturn = getFile(path);
                if (!fileToReturn.exists()) {
                    fileToReturn = getFile(TeenyHttpd.FILE_NOT_FOUND);
                    response = "HTTP/1.1 404 File Not Found";
                } else {
                    response = "HTTP/1.1 200 OK";
                }
            }
        }

        final int fileLength = (int) fileToReturn.length();

        String[] headers = new String[] {
                response,
                "Server: TeenyHttpd from JonathanGiles.net : 1.0",
                "Date: " + LocalDateTime.now(),
                "Content-type: " + getContentType(path),
                "Content-length: " + fileLength
        };

        // write headers
        for (String header : headers) {
            out.println(header);
        }
        out.println(); // empty line between header and body
        out.flush();   // flush character output stream buffer

        // write body
        Files.copy(fileToReturn.toPath(), dataOut);
        dataOut.flush();
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    private File getFile(String filename) {
        return new File(TeenyHttpd.WEB_ROOT, filename);
    }
}
