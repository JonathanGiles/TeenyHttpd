package net.jonathangiles.tools.teenyhttpd.json;

import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public final class TeenyJsonMessageConverter implements MessageConverter {

    public final TeenyJson teenyJson = new TeenyJson();

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void write(Object value, BufferedOutputStream dataOut) throws IOException {
        teenyJson.writeValue(dataOut, value);
    }

    @Override
    public Object read(String value, Type type) {
        try {
            return teenyJson.readValue(value, type);
        } catch (JsonParsingException e) {
            throw new RuntimeException(e);
        }
    }
}
