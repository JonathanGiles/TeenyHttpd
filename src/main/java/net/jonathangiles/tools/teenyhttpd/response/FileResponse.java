package net.jonathangiles.tools.teenyhttpd.response;

import net.jonathangiles.tools.teenyhttpd.request.Method;
import net.jonathangiles.tools.teenyhttpd.request.Request;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FileResponse extends Response {
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    private static final Map<String, String> contentTypes;
    static {
        Properties props = new Properties();
        try(InputStream resourceStream = FileResponse.class.getResourceAsStream("contentTypes.properties")) {
            props.load(resourceStream);
        } catch (IOException e) {
            props = null;
            e.printStackTrace();
        }

        contentTypes = props == null ? Collections.emptyMap() : (Map<String, String>) (Object) props;
    }

    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    private static final File DEFAULT_WEB_ROOT = Paths.get(loader.getResource("webroot").getPath()).toFile();

    private static final FileNameMap FILE_NAME_MAP = URLConnection.getFileNameMap();

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
            case PUT:
            case POST:
            case OPTIONS:
            case HEAD:
            case TRACE:
            case DELETE:
            case CONNECT:
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
        final String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);

        String contentType = contentTypes.get(ext);
        if (contentType != null) {
            return contentType;
        }

        contentType = FILE_NAME_MAP.getContentTypeFor(file.getName());
        if (contentType != null) {
            return contentType;
        }

        System.err.println("Unable to determine content type for file " + file.getName());;
        return "text/plain";
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
