package net.jonathangiles.tools.teenyhttpd;

import com.google.gson.Gson;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class GsonMessageConverter implements MessageConverter {

    final Gson gson = new Gson();

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

        dataOut.write(gson.toJson(value).getBytes());
    }

    @Override
    public Object read(String value, Type type) {

        if (String.class.isAssignableFrom((Class<?>) type)) {
            return value;
        }

        return gson.fromJson(value, type);
    }
}