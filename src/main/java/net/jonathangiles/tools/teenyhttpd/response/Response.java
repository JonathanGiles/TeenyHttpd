package net.jonathangiles.tools.teenyhttpd.response;

import net.jonathangiles.tools.teenyhttpd.request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public interface Response {

    StatusCode getStatusCode();

    default List<String> getHeaders() {
        return Collections.emptyList();
    }

    default long getBodyLength() {
        return 0;
    }

    void writeBody(BufferedOutputStream dataOut) throws IOException;

}
