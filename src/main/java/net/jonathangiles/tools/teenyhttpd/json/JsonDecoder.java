package net.jonathangiles.tools.teenyhttpd.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple JSON decoder that can decode a JSON string into a Map or List of Maps.
 */
final class JsonDecoder {

    private final Buffer buffer;

    public JsonDecoder(String json) {
        this.buffer = new Buffer(json);
    }

    private boolean isCloser(char c) {
        return c == '}' || c == ']';
    }

    public Object read() {

        char current = buffer.current();

        if (current == '{') {
            return readObject();
        }

        if (current == '"') {
            return readString();
        }

        if (current == '[') {
            return readArray();
        }

        if (Character.isDigit(current) ||
                current == '-' ||
                current == '+' ||
                current == '.') {

            return readNumber();
        }

        if (current == 'n' || current == 'N') {
            return readNull();
        }

        if (current == 'f' || current == 'F' ||
                current == 't' || current == 'T') {
            return readBoolean(current);
        }

        return null;
    }

    private Object readBoolean(char current) {

        if (current == 't' || current == 'T') {
            return Boolean.parseBoolean(buffer.next(4));
        }

        return Boolean.parseBoolean(buffer.next(5));
    }

    private Object readNull() {
        buffer.next(4);
        return null;
    }

    private String readNumber() {

        StringBuilder sb = new StringBuilder();

        if (buffer.current() != '+') {
            sb.append(buffer.current());
        }

        while (buffer.next()) {
            char temp = buffer.current();

            if (isCloser(temp) || temp == ',') {
                break;
            } else {
                sb.append(temp);
            }
        }

        buffer.back();

        return sb.toString();
    }

    private List<Object> readArray() {

        List<Object> result = new ArrayList<>();

        boolean hasNext = true;
        while (hasNext) {

            buffer.next();
            if (buffer.current() == ']') {
                return result;
            }

            Object value = read();

            result.add(value);

            if (buffer.current() == ']') {
                hasNext = false;
            }

            buffer.next();

            if (buffer.current() == ']') {
                hasNext = false;
            }
        }

        return result;
    }

    private String readString() {
        StringBuilder result = new StringBuilder();

        while (buffer.current() == ' ') {
            buffer.next();
        }

        if (buffer.current() == '"') {
            while (buffer.next()) {
                char temp = buffer.current();

                if ('"' == temp) {
                    if ('\\' != buffer.readBefore()) {
                        break;
                    }
                }
                result.append(temp);
            }
        } else {
            return "";
        }

        return result.toString();
    }

    private Map<String, Object> readObject() {

        Map<String, Object> result = new HashMap<>();

        boolean hasNext = true;

        while (hasNext) {
            buffer.next();

            if (buffer.current() == '}') {
                return result;
            }

            String key = readString();

            while (buffer.next()) {
                char temp = buffer.current();
                if (temp == ':') {
                    buffer.next();
                    break;
                }
            }

            result.put(key, read());

            buffer.next();

            if (buffer.current() == '}') {
                hasNext = false;
            }
        }

        return result;
    }


    private static class Buffer {
        private final String json;
        private int index;

        public Buffer(String json) {
            this.json = json;
        }

        public char current() {
            return json.charAt(index);
        }

        public void back() {
            index--;
        }

        public char readBefore() {
            return json.charAt(index - 1);
        }

        public boolean next() {
            if (index + 1 >= json.length()) {
                return false;
            } else {
                index++;
                return true;
            }
        }

        public String next(int i) {
            String value = json.substring(index, index + i);

            index += i;

            return value;
        }

        public boolean hasNext() {
            return index < json.length();
        }
    }

}
