package net.jonathangiles.tools.teenyhttpd.json;


import net.jonathangiles.tools.teenyhttpd.implementation.ReflectionUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 0 dependencies Teeny JSON serializer
 *
 */
public class TeenyJson {

    private final JsonEncoder encoder;
    private final Map<Class<?>, ValueParser<?>> parsers = new ConcurrentHashMap<>();

    public TeenyJson() {
        encoder = new JsonEncoder();
    }

    public String writeValueAsString(Object value) {
        return encoder.writeValueAsString(value);
    }

    public void writeValue(BufferedOutputStream baos, Object value) throws IOException {
        baos.write(writeValueAsString(value).getBytes());
    }

    public <T> List<T> readList(String json, Class<T> type) {
        Object result = new JsonDecoder(json)
                .read();

        if (result == null) {
            return null;
        }

        List<?> list = (List<?>) result;

        return list.stream()
                .map(o -> parseObject(o, type))
                .collect(Collectors.toList());
    }

    public <T> T readValue(String json, Class<T> type) {
        Object result = new JsonDecoder(json).read();

        if (result == null) {
            return null;
        }

        return parseObject(result, type);
    }

    public TeenyJson setSerializer(Class<?> clazz, ValueSerializer serializer) {
        encoder.setSerializer(clazz, serializer);
        return this;
    }

    public TeenyJson setParser(Class<?> clazz, ValueParser<?> parser) {
        parsers.put(clazz, parser);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseObject(Object object, Class<T> type) {

        T instance = ReflectionUtils.newInstance(type);

        Map<String, Field> fields = ReflectionUtils.getFields(type);
        Map<String, Method> mutators = ReflectionUtils.getMutators(type);

        Map<String, Object> map = (Map<String, Object>) object;

        for (Map.Entry<String, Field> entry : fields.entrySet()) {

            Object source = map.get(entry.getKey());

            if (source == null) {
                continue;
            }

            write(entry.getValue(), mutators.get(entry.getKey()), source, instance);
        }

        return instance;
    }

    private void write(Field field, Method method, Object value, Object instance) {


        try {

            if (method != null) {
                method.setAccessible(true);
                method.invoke(instance, parse(value, method.getParameterTypes()[0]));
                return;
            }

            field.setAccessible(true);
            field.set(instance, parse(value, field.getType()));
        } catch (Exception ex) {
            Logger.getLogger(TeenyJson.class.getName())
                    .log(Level.SEVERE, "Failed to set field " + field.getName() + " on " + instance.getClass().getName(), ex);
        }
    }

    private Object parse(Object value, Class<?> target) {

        if (target == List.class) {

            List<?> list = (List<?>) value;

            return list.stream()
                    .map(o -> parse(o, Objects.requireNonNull(ReflectionUtils.getParameterType(target),
                            "Unable to infer parameter type").getType()))
                    .collect(Collectors.toList());
        }

        if (target.isPrimitive()) {
            return parseSimple(value, target);
        }


        if (!target.getName().startsWith("java")) {
            return parseObject(value, target);
        }

        return parseSimple(value, target);
    }

    private Object parsePrimitive(Object value, Class<?> target) {
        if (target == int.class) {

            if (value == null) return 0;

            return Integer.parseInt(value.toString());
        }

        if (target == long.class) {

            if (value == null) return 0L;

            return Long.parseLong(value.toString());
        }

        if (target == double.class) {

            if (value == null) return 0.0;

            return Double.parseDouble(value.toString());
        }

        if (target == float.class) {

            if (value == null) return 0.0f;

            return Float.parseFloat(value.toString());
        }

        if (target == boolean.class) {

            if (value == null) return false;

            return Boolean.parseBoolean(value.toString());
        }

        throw new IllegalStateException("Unsupported primitive type: " + target.getName());
    }

    private Object parseSimple(Object value, Class<?> target) {

        if (value == null) {
            return null;
        }

        if (target == String.class) {
            return value.toString();
        }

        if (target.isPrimitive()) {
            return parsePrimitive(value, target);
        }

        if (target == Integer.class) {
            return Integer.parseInt(value.toString());
        }

        if (target == BigDecimal.class) {
            return new BigDecimal(value.toString());
        }

        if (target == BigInteger.class) {
            return new BigInteger(value.toString());
        }

        if (target == Long.class) {
            return Long.parseLong(value.toString());
        }

        if (target == Double.class) {
            return Double.parseDouble(value.toString());
        }

        if (target == Float.class) {
            return Float.parseFloat(value.toString());
        }

        if (target == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }

        if (target == LocalDateTime.class) {
            return LocalDateTime.parse(value.toString());
        }

        if (target == LocalDate.class) {
            return LocalDate.parse(value.toString());
        }

        ValueParser<?> parser = parsers.get(target);

        if (parser != null) {
            return parser.parse(value);
        }

        System.out.println("not implemented: " + target.getName());

        return null;
    }

}
