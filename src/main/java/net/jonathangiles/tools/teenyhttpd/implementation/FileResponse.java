package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.TeenyHttpd;
import net.jonathangiles.tools.teenyhttpd.model.ContentType;
import net.jonathangiles.tools.teenyhttpd.model.Headers;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.model.Request;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;

public class FileResponse extends ResponseBase {
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    private static final FileNameMap FILE_NAME_MAP = URLConnection.getFileNameMap();

    private File fileToReturn;

    public FileResponse(final Request request) {
        final Method method = request.getMethod();
        String path = request.getPath();

        fileToReturn = null;

        switch (method) {
            case GET: {
                if (path.endsWith("/")) {
                    path += DEFAULT_FILE;
                }

                fileToReturn = getFile(path);
                if (!fileToReturn.exists()) {
                    fileToReturn = getFile(FILE_NOT_FOUND);
                    setStatusCode(StatusCode.NOT_FOUND);
                } else {
                    setStatusCode(StatusCode.OK);
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
                setStatusCode(StatusCode.NOT_IMPLEMENTED);
                break;
            }
        }

        final int fileLength = (int) fileToReturn.length();

        setHeader(Headers.CONTENT_TYPE.asHeader(getContentType(fileToReturn)));
        setHeader(Headers.CONTENT_LENGTH.asHeader(Integer.toString(fileLength)));
    }

    @Override
    public long getBodyLength() {
        return fileToReturn.length();
    }

    @Override
    public void writeBody(final BufferedOutputStream dataOut) throws IOException {
        Files.copy(fileToReturn.toPath(), dataOut);
        dataOut.flush();
    }

    // return supported MIME Types
    private String getContentType(final File file) {
        final String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);

        ContentType contentType = ContentType.fromFileExtension(ext);
        if (contentType != null) {
            return contentType.getHeaderValue();
        }

        String contentTypeString = FILE_NAME_MAP.getContentTypeFor(file.getName());
        if (contentTypeString != null) {
            return contentTypeString;
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
        return new File(TeenyHttpd.DEFAULT_WEB_ROOT, filename);
    }
}
