package net.jonathangiles.tools.teenyhttpd.response;

import net.jonathangiles.tools.teenyhttpd.request.Method;
import net.jonathangiles.tools.teenyhttpd.request.Request;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileResponse extends Response {
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    private static final File DEFAULT_WEB_ROOT = Paths.get(loader.getResource("webroot").getPath()).toFile();

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

    /**
     * This method is called when the file is about to be loaded from the file system. Overriding it offers the
     * opportunity of modifying where the file is retrieved from.
     *
     * @param filename The name of the file to be loaded, relative to the webroot (or otherwise).
     * @return A File reference of the file being requested.
     */
    protected File getFile(final String filename) {
        return new File(DEFAULT_WEB_ROOT, filename);
    }
}
