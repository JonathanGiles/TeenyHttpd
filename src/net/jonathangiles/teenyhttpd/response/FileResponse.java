package net.jonathangiles.teenyhttpd.response;

import net.jonathangiles.teenyhttpd.request.Method;
import net.jonathangiles.teenyhttpd.request.Request;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileResponse extends Response {
    static final File WEB_ROOT = new File("./wwwroot");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    private final StatusCode statusCode;
    private final List<String> headers;
    private File fileToReturn;

    public FileResponse(final Request request) {
        super(request);

        final Method method = getRequest().getMethod();
        String path = getRequest().getPath();

        fileToReturn = null;

        switch (method) {
            case GET: {
                if (path.endsWith("/")) {
                    path += DEFAULT_FILE;
                }

                fileToReturn = getFile(path);
                if (!fileToReturn.exists()) {
                    fileToReturn = getFile(FILE_NOT_FOUND);
                    statusCode = StatusCode.NOT_FOUND;
                } else {
                    statusCode = StatusCode.OK;
                }
                break;
            }
            default: {
                fileToReturn = getFile(METHOD_NOT_SUPPORTED);
                statusCode = StatusCode.NOT_IMPLEMENTED;
                break;
            }
        }

        final int fileLength = (int) fileToReturn.length();

        headers = new ArrayList<>();
        headers.add("Content-type: " + getContentType(fileToReturn));
        headers.add("Content-length: " + fileLength);
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
    public void writeBody(final BufferedOutputStream dataOut) throws IOException {
        Files.copy(fileToReturn.toPath(), dataOut);
        dataOut.flush();
    }

    // return supported MIME Types
    private String getContentType(final File file) {
        final String filename = file.getName();
        if (filename.endsWith(".htm") || filename.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    private File getFile(final String filename) {
        return new File(WEB_ROOT, filename);
    }
}
