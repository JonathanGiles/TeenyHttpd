package net.jonathangiles.tools.teenyhttpd.model;

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * A strategy interface for converting an object to a message.
 */
public interface MessageConverter {

    String getContentType();

    void write(Object value, BufferedOutputStream dataOut) throws IOException;

}
