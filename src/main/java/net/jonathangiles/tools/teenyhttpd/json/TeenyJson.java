package net.jonathangiles.tools.teenyhttpd.json;


import net.jonathangiles.tools.teenyhttpd.implementation.ParameterType;
import net.jonathangiles.tools.teenyhttpd.implementation.ReflectionUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 0 dependencies Teeny JSON serializer
 *
 */
public class TeenyJson {

    public void writeValue(BufferedOutputStream baos, Object value) throws IOException {
        baos.write(writeValueAsString(value).getBytes());
    }

    public String writeValueAsString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;

        if (value instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) value;
            return "[" + collection.stream()
                    .map(this::serialize)
                    .collect(Collectors.joining(", ")) + "]";
        }

        return serialize(value);

    }

    public <T> T readValue(String json, Class<T> clazz) {

        if (json == null || json.isEmpty()) return null;

        if (json.startsWith("[")) {
            return null;//TODO: implement
        }

        T instance = ReflectionUtils.newInstance(clazz);

        Map<String, Object> tree = jsonToMap(json);

        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(JsonIgnore.class)) continue;
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (method.getParameterCount() != 1) continue;
            if (method.getName().equals("equals")) continue;

            String fieldName = getFieldName(method);

            if (tree.containsKey(fieldName)) {
                invoke(method, instance, tree.get(fieldName).toString());
            } else {
                System.err.println("Field not found: " + fieldName);
            }
        }

        return instance;
    }

    private Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();

        String[] split = json.substring(1, json.length() - 1).split(", ");

        for (String property : split) {
            String[] keyValue = split(property);
            map.put(cleanUp(keyValue[0]), cleanUp(keyValue[1]));
        }

        return map;
    }

    private String cleanUp(String value) {
        value = value.trim();

        if (value.startsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

    private String[] split(String property) {
        String key;
        String value;

        if (property.contains(":")) {
            key = property.substring(0, property.indexOf(":"));
            value = property.substring(property.indexOf(":") + 1);
        } else {
            key = property;
            value = "";
        }

        return new String[]{key, value};
    }

    private void invoke(Method method, Object parent, String value) {
        try {
            method.setAccessible(true);
            method.invoke(parent, parse(method.getParameters()[0], value));
        } catch (IllegalAccessException | InvocationTargetException e) {
            Logger.getLogger(TeenyJson.class.getName())
                    .log(Level.SEVERE, "Error invoking method: " + method.getName(), e);
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Object parseCollection(Class<?> itemType, Collection<?> collection, String values) {

        Collection<Object> resultCollection;

        if (collection instanceof List) {
            resultCollection = new ArrayList<>();
        } else if (collection instanceof Set) {
            resultCollection = new HashSet<>();
        } else {
            throw new IllegalArgumentException("Unsupported collection type: " + collection.getClass().getName());
        }

        String[] split = values.substring(1, values.length() - 1).split(", ");

        for (String value : split) {
            resultCollection.add(parse(itemType, value));
        }

        return collection;
    }

    private Object parse(Parameter parameter, String value) {

        if (Set.class.isAssignableFrom(parameter.getType())) {

            ParameterType type = ReflectionUtils.getParameterType(parameter);

            return parseCollection(type.getType(), new HashSet<>(), value);

        } else if (List.class.isAssignableFrom(parameter.getType())) {

            ParameterType type = ReflectionUtils.getParameterType(parameter);

            return parseCollection(type.getType(), new ArrayList<>(), value);

        }

        return parse(parameter.getType(), value);
    }

    private Object parse(Class<?> type, String value) {

        if (type.isPrimitive()) {
            if (type == int.class) return Integer.parseInt(value);
            if (type == long.class) return Long.parseLong(value);
            if (type == double.class) return Double.parseDouble(value);
            if (type == float.class) return Float.parseFloat(value);
            if (type == boolean.class) return Boolean.parseBoolean(value);
            if (type == char.class) return value.charAt(0);
        }

        if (type.getName().startsWith("java")) {
            if (type == String.class) return value;
            if (type == Integer.class) return Integer.parseInt(value);
            if (type == Long.class) return Long.parseLong(value);
            if (type == Double.class) return Double.parseDouble(value);
            if (type == Float.class) return Float.parseFloat(value);
            if (type == Boolean.class) return Boolean.parseBoolean(value);
            if (type == Character.class) return value.charAt(0);
            if (type == LocalDate.class) return LocalDate.parse(value);
            if (type == LocalDateTime.class) return LocalDateTime.parse(value);
        }

        return readValue(value, type);
    }

    private String serialize(Object object) {

        final Class<?> clazz = object.getClass();

        if (clazz.getName()
                .startsWith("java.lang")) {

            return writeValue(object);
        }

        final boolean includeNonNull = clazz.isAnnotationPresent(JsonIncludeNonNull.class);

        Method[] methods = clazz.getDeclaredMethods();

        StringBuilder builder = new StringBuilder();

        builder.append("{");

        List<String> properties = new ArrayList<>();

        for (Method method : methods) {
            if (method.isAnnotationPresent(JsonIgnore.class)) continue;
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (method.getParameterCount() > 0) continue;

            properties.add(writeField(method, object, includeNonNull));

        }

        builder.append(properties.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", ")));

        builder.append("}");

        return builder.toString();
    }

    private String writeField(Method method, Object object, boolean includeNonNull) {

        StringBuilder builder = new StringBuilder();

        if (method.getName().startsWith("get") || method.getName().startsWith("is")) {

            try {

                method.setAccessible(true);

                Object value = method.invoke(object);

                if (includeNonNull && value == null) return null;

                String name = getFieldName(method);

                builder.append("\"").append(name).append("\": ");

                if (value instanceof Collection) {
                    builder.append("[");

                    String collection = ((Collection<?>) value)
                            .stream()
                            .map(this::serialize)
                            .collect(Collectors.joining(", "));

                    builder.append(collection);

                    builder.append("]");

                    return builder.toString();
                }

                builder.append(writeValue(value));
            } catch (Exception e) {
                Logger.getLogger(TeenyJson.class.getName())
                        .log(Level.SEVERE, "Error serializing object: " + object.getClass().getName(), e);
            }
        }

        return builder.toString();
    }

    private String getFieldName(Method method) {

        if (method.isAnnotationPresent(JsonAlias.class)) {
            return method.getAnnotation(JsonAlias.class).value();
        }

        String name = method.getName();

        if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        }

        if (name.startsWith("is")) {
            name = name.substring(2);
        }

        return name.replace(name.charAt(0), Character.toLowerCase(name.charAt(0)));
    }

    private String writeValue(Object value) {

        if (value == null) return "null";


        if (value instanceof String) {
            return "\"" + escapeJsonString((String) value) + "\"";
        }

        if (value instanceof Number) {
            return value.toString();
        }

        if (value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof Character) {
            return "\"" + value + "\"";
        }

        if (value instanceof LocalDate) {
            return "\"" + value + "\"";
        }

        if (value instanceof LocalDateTime) {
            return "\"" + value + "\"";
        }

        if (value.getClass().isEnum()) {
            return "\"" + value + "\"";
        }

        if (!value.getClass().getName().startsWith("java.lang") && !value.getClass().getName().startsWith("java.time")) {
            return serialize(value);
        }

        System.err.println("Unknown type: " + value.getClass().getName());

        return null;
    }

    private String escapeJsonString(String s) {

        if (s == null) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                case '\\':
                    sb.append('\\');
                    sb.append(ch);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch <= 0x1F) {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

}
