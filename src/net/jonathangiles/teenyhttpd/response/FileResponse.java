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

    private StatusCode statusCode;
    private List<String> headers;
    private File fileToReturn;
    private int fileLength;

    public FileResponse(final Request request) {
        super(request);

        final Method method = getRequest().getMethod();
        String path = getRequest().getPath();

        fileToReturn = null;

        // we support only GET and HEAD methods, we check
        if (! (method == Method.GET || method == Method.HEAD)) {
            // we return the not supported file to the client
            fileToReturn = getFile(METHOD_NOT_SUPPORTED);
            statusCode = StatusCode.NOT_IMPLEMENTED;
        } else {
            // GET or HEAD method
            if (path.endsWith("/")) {
                path += DEFAULT_FILE;
            }

            if (method == Method.GET) { // GET method so we return content
                fileToReturn = getFile(path);
                if (!fileToReturn.exists()) {
                    fileToReturn = getFile(FILE_NOT_FOUND);
                    statusCode = StatusCode.FILE_NOT_FOUND;
                } else {
                    statusCode = StatusCode.OK;
                }
            }
        }

        fileLength = (int) fileToReturn.length();

        headers = new ArrayList<>();
        headers.add("Content-type: " + getContentType(path));
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
    public void writeBody(BufferedOutputStream dataOut) throws IOException {
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
        return new File(WEB_ROOT, filename);
    }
}
