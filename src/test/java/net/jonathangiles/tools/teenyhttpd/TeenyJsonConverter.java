package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.json.TeenyJson;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class TeenyJsonConverter implements MessageConverter {

    final TeenyJson teenyJson = new TeenyJson();

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void write(Object value, BufferedOutputStream dataOut) throws IOException {


        if (value instanceof String) {
            dataOut.write(((String) value).getBytes());
            return;
        }

        dataOut.write(teenyJson.writeValueAsString(value).getBytes());
    }

    @Override
    public Object read(String value, Type type) {
        return teenyJson.readValue(value, type);
    }
}
