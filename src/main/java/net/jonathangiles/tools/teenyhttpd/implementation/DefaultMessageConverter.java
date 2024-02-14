package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class DefaultMessageConverter implements MessageConverter {

    public static final DefaultMessageConverter INSTANCE = new DefaultMessageConverter();

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public void write(Object value, BufferedOutputStream dataOut) throws IOException {
        dataOut.write(value.toString().getBytes());
    }
}
