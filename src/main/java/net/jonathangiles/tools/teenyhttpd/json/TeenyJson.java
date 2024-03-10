package net.jonathangiles.tools.teenyhttpd.json;


import net.jonathangiles.tools.teenyhttpd.implementation.ParameterizedTypeHelper;
import net.jonathangiles.tools.teenyhttpd.implementation.ReflectionUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A simple JSON serializer and deserializer.
 */
public final class TeenyJson {

    private final JsonEncoder encoder;
    private final Map<Class<?>, ValueParser<?>> parsers = new ConcurrentHashMap<>();

    public TeenyJson() {
        encoder = new JsonEncoder();
    }

    /**
     * Serialize the given object to a JSON string.
     * @param value the object to serialize
     * @return the serialized JSON string
     */
    public String writeValueAsString(Object value) {
        return encoder.writeValueAsString(value);
    }

    /**
     * Serialize the given object to a JSON string and write it to the given output stream.
     * @param outputStream the output stream to write to
     * @param value the object to serialize
     * @throws IOException if a problem occurs during writing
     */
    public void writeValue(BufferedOutputStream outputStream, Object value) throws IOException {
        if (value == null) return;
        outputStream.write(writeValueAsString(value).getBytes());
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
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Class<T> type) {
        if (json == null) return null;

        if (type == String.class) {
            return (T) json;
        }

        Object result = new JsonDecoder(json).read();

        if (result == null) {
            return null;
        }

        return parseObject(result, type);
    }

    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Type type) throws JsonParsingException {
        if (json == null) return null;
        //if the type is string, just return the string
        if (type == String.class) {
            return (T) json;
        }

        Object result = new JsonDecoder(json).read();

        if (result == null) {
            return null;
        }

        if (type instanceof Class<?>) {
            return (T) parse(result, type);
        }

        throw new JsonParsingException("Unsupported type: " + type.getTypeName() + " " + type.getClass().getName());
    }


    /**
     * Register a custom serializer for the given class.
     * <p>
     * @param clazz the class to register
     */
    @SuppressWarnings("UnusedReturnValue")
    public <T> TeenyJson registerSerializer(Class<T> clazz, ValueSerializer<T> serializer) {
        encoder.registerSerializer(clazz, serializer);
        return this;
    }

    /**
     * Register a custom parser for the given class.
     * <p>
     * @param clazz the class to register
     */
    @SuppressWarnings("unused")
    public <T> TeenyJson registerParser(Class<T> clazz, ValueParser<T> parser) {
        parsers.put(clazz, parser);
        return this;
    }

    /**
     * Parses an object into a target type.
     * @param object the object to parse
     * @param type the target type
     * @return the parsed object or null if the object is null
     * @param <T> the type of the target
     */
    @SuppressWarnings("unchecked")
    private <T> T parseObject(Object object, Class<T> type) {
        if (object == null) return null;

        T instance = ReflectionUtils.newInstance(type);

        Map<String, Field> fields = ReflectionUtils.getFields(type);
        Map<String, Method> mutators = ReflectionUtils.getMutators(type);
        Map<String, Object> map = (Map<String, Object>) object;

        for (Map.Entry<String, Field> entry : fields.entrySet()) {
            Object source = map.get(entry.getKey());

            if (source == null) continue;

            write(entry.getValue(), mutators.get(entry.getKey()), source, instance);
        }

        return instance;
    }

    /**
     * Writes a value to a field or method of an instance, is something goes wrong logs the error,
     * but does not throw an exception.
     * <p>
     * @param field the field to write if the method is missing
     * @param method the method to write
     * @param value the value to write
     * @param instance the instance to write to
     */
    private void write(Field field, Method method, Object value, Object instance) {
        try {
            if (method != null) {
                method.setAccessible(true);

                if (method.isAnnotationPresent(JsonDeserialize.class)) {
                    JsonDeserialize annotation = method.getAnnotation(JsonDeserialize.class);

                    if (annotation.as() != Object.class) {
                        method.invoke(instance, parse(value, annotation.as()));
                    } else if (annotation.contentAs() != Object.class) {
                        ParameterizedTypeHelper helper = ReflectionUtils.getParameterType(method.getGenericParameterTypes()[0]);
                        helper = helper.withFirstType(annotation.contentAs());
                        method.invoke(instance, parse(value, helper));
                    }

                    return;
                }

                try {
                    method.invoke(instance, parse(value, method.getGenericParameterTypes()[0]));
                } catch (JsonParsingException e) {
                    throw new JsonParsingException("Failed to write field: " + field.getName() + " on " + field.getDeclaringClass(), e);
                }
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

    /**
     * Parses a value into a target type.
     * <p>
     * @param value the value to parse
     * @param target the target type
     * @return the parsed value or null if the value is null or the target type is not supported
     * @throws IllegalStateException if the target type is not supported
     * @throws JsonParsingException if a problem occurs during parsing
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object parse(Object value, Type target) throws JsonParsingException {
        if (target instanceof ParameterizedType) {
            return parseCollectionOrMap(value, ReflectionUtils.getParameterType(target));
        }

        if (target instanceof ParameterizedTypeHelper) {
            return parseCollectionOrMap(value, (ParameterizedTypeHelper) target);
        }

        if (target instanceof Class<?>) {
            Class<?> targetClass = (Class<?>) target;

            if (targetClass.isInterface()) {
                throw new JsonParsingException("Unsupported type: " + target.getTypeName() + " " + target.getClass().getName());
            }

            if (targetClass.isPrimitive()) {
                return parseSimple(value, targetClass);
            }

            if (targetClass.isArray()) {
                return parseArray(value, ReflectionUtils.getArrayType(target));
            }

            if (ReflectionUtils.isEnum(targetClass)) {
                return Enum.valueOf((Class<Enum>) target, value.toString());
            }

            if (!targetClass.getName().startsWith("java")) {
                return parseObject(value, targetClass);
            }

            return parseSimple(value, targetClass);
        }

        throw new JsonParsingException("Unsupported type: " + target.getTypeName() + " " + target.getClass().getName());
    }

    private Object parseArray(Object value, Class<?> componentType) throws JsonParsingException {
        if (value == null) return null;

        List<?> list = (List<?>) value;

        Object array = java.lang.reflect.Array.newInstance(componentType, list.size());

        for (int i = 0; i < list.size(); i++) {
            java.lang.reflect.Array.set(array, i, parse(list.get(i), componentType));
        }

        return array;
    }

    /**
     * Parses a value into a collection or map.
     *
     * @param value the value to parse
     * @param helper the parameterized type helper
     * @return the parsed value or null if the value is null or the target type is not supported
     * @throws JsonParsingException if a problem occurs during parsing
     */
    @SuppressWarnings("unchecked")
    private Object parseCollectionOrMap(Object value, ParameterizedTypeHelper helper) throws JsonParsingException {
        if (helper.getParentType() == null)
            throw new JsonParsingException("Type is not a ParameterizedType: " + helper);

        if (helper.isParentTypeOf(List.class)) {
            List<?> list = (List<?>) value;
            List<Object> parsedList;

            if (helper.getParentType() == ArrayList.class || helper.getParentType() == List.class) {
                parsedList = new ArrayList<>();
            } else {
                parsedList = (List<Object>) ReflectionUtils.newInstance(helper.getParentType());
            }

            for (Object o : list) {
                parsedList.add(parse(o, helper.getFirstType()));
            }

            return parsedList;
        }

        if (helper.isParentTypeOf(Set.class)) {
            List<?> list = (List<?>) value;
            Set<Object> parsedSet;

            if (helper.getParentType() == HashSet.class || helper.getParentType() == Set.class) {
                parsedSet = new HashSet<>();
            } else {
                parsedSet = (Set<Object>) ReflectionUtils.newInstance(helper.getParentType());
            }

            for (Object o : list) {
                parsedSet.add(parse(o, helper.getFirstType()));
            }

            return parsedSet;
        }

        if (helper.isParentTypeOf(Map.class)) {
            Map<?, ?> map = (Map<?, ?>) value;
            //json only supports string keys
            Map<String, Object> result = new HashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(entry.getKey().toString(), parse(entry.getValue(), helper.getSecondType()));
            }

            return result;
        }

        throw new JsonParsingException("Unsupported parameterized type: " + helper.getParentType());
    }


    /**
     * Parses a value into a primitive target type such as int, long, double, float, or boolean.
     * <p>
     * @param obj the value to parse
     * @param target the target type
     * @return the parsed value or the default value for the target type if the value is null
     * @throws NumberFormatException if the value cannot be parsed into a number if the target is a number
     * @throws IllegalStateException if the target type is not supported
     */
    private Object parsePrimitive(Object obj, Class<?> target) {
        String value = obj == null ? null : obj.toString().trim();

        if (target == int.class) {
            if (value == null) return 0;

            return Integer.parseInt(value);
        }

        if (target == long.class) {
            if (value == null) return 0L;

            return Long.parseLong(value);
        }

        if (target == double.class) {
            if (value == null) return 0.0;

            return Double.parseDouble(value);
        }

        if (target == float.class) {
            if (value == null) return 0.0f;

            return Float.parseFloat(value);
        }

        if (target == boolean.class) {

            if (value == null) return false;

            return Boolean.parseBoolean(value);
        }

        throw new IllegalStateException("Unsupported primitive type: " + target.getName());
    }

    /**
     * Parses a simple value into a target type.
     * <p>
     * @param value the value to parse
     * @param target the target type
     * @throws RuntimeException if a problem occurs during parsing
     * @return the parsed value or null if the value is null or the target type is not supported
     */
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

        if (target == LocalTime.class) {
            return LocalTime.parse(value.toString());
        }

        if (target == Date.class) {
            try {
                return encoder.getDateFormatter()
                        .parse(value.toString());
            } catch (ParseException e) {
                return null;
            }
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
