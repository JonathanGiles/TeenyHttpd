package net.jonathangiles.tools.teenyhttpd.json;


import net.jonathangiles.tools.teenyhttpd.implementation.ReflectionUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    /**
     * Read a JSON string and return a collection of objects of the given type.
     *
     * @param collectionType one of List or Set
     * @param json the JSON string to parse
     * @return the parsed object which could be null.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T, K> K readCollection(String json, Class<? extends Collection> collectionType, Class<T> type) {

        if (json == null) return null;

        Object result = new JsonDecoder(json)
                .read();

        if (result == null) {
            return null;
        }

        List<?> list = (List<?>) result;

        if (List.class.isAssignableFrom(collectionType)) {
            return (K) list.stream()
                    .map(o -> parseObject(o, type))
                    .collect(Collectors.toList());
        }

        if (Set.class.isAssignableFrom(collectionType)) {
            return (K) list.stream()
                    .map(o -> parseObject(o, type))
                    .collect(Collectors.toSet());
        }

        throw new IllegalStateException("Unsupported collection type: " + collectionType.getName());
    }

    /**
     *
     * Read a JSON string and return an object of the given type.
     *
     * @param json the JSON string to parse
     * @param type the type of the object to parse
     *
     */
    public <T> T readValue(String json, Class<T> type) {

        if (json == null) return null;

        Object result = new JsonDecoder(json).read();

        if (result == null) {
            return null;
        }

        return parseObject(result, type);
    }

    /**
     * Register a custom serializer for the given class.
     *
     * @param clazz the class to register
     */
    public TeenyJson setSerializer(Class<?> clazz, ValueSerializer serializer) {
        encoder.setSerializer(clazz, serializer);
        return this;
    }

    /**
     * Register a custom parser for the given class.
     *
     * @param clazz the class to register
     */
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
                method.invoke(instance, parse(value, method.getGenericParameterTypes()[0]));
                return;
            }

            // if the method is missing, try to set the field directly

            field.setAccessible(true);
            field.set(instance, parse(value, field.getType()));

        } catch (Exception ex) {
            Logger.getLogger(TeenyJson.class.getName())
                    .log(Level.SEVERE, "Failed to set field " + field.getName() + " on " + instance.getClass().getName(), ex);
        }
    }

    private Object parse(Object value, Type target) {

        if (target instanceof ParameterizedType) {

            ParameterizedType parameterizedType = (ParameterizedType) target;

            if (parameterizedType.getRawType() == List.class) {

                List<?> list = (List<?>) value;

                return list.stream()
                        .map(o -> parse(o, Objects.requireNonNull(ReflectionUtils.getParameterType(target),
                                "Unable to infer parameter type").getType()))
                        .collect(Collectors.toList());
            }

            if (parameterizedType.getRawType() == Set.class) {

                List<?> list = (List<?>) value;

                return list.stream()
                        .map(o -> parse(o, Objects.requireNonNull(ReflectionUtils.getParameterType(target),
                                "Unable to infer parameter type").getType()))
                        .collect(Collectors.toSet());
            }


            throw new IllegalStateException("Unsupported parameterized type: " + parameterizedType.getRawType() + " " + target.getClass().getName());
        }

        if (target instanceof Class<?>) {

            Class<?> targetClass = (Class<?>) target;

            if (targetClass.isPrimitive()) {
                return parseSimple(value, targetClass);
            }

            if (!targetClass.getName().startsWith("java")) {
                return parseObject(value, targetClass);
            }

            return parseSimple(value, targetClass);
        }

        throw new IllegalStateException("Unsupported type: " + target.getTypeName() + " " + target.getClass().getName());
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

    @SuppressWarnings({"unchecked", "rawtypes"})
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

        if (target.isEnum()) {
            return Enum.valueOf((Class<Enum>) target, value.toString());
        }

        ValueParser<?> parser = parsers.get(target);

        if (parser != null) {
            return parser.parse(value);
        }

        Logger.getLogger(TeenyJson.class.getName())
                .log(Level.WARNING, "not implemented: " + target.getName());

        return null;
    }

}
