package net.jonathangiles.tools.teenyhttpd.json;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

final class JsonEncoder {

    private final Map<Class<?>, Mapper> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, ValueSerializer> serializers = new ConcurrentHashMap<>();

    public JsonEncoder setSerializer(Class<?> clazz, ValueSerializer serializer) {
        serializers.put(clazz, serializer);
        return this;
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


    private String serialize(Object object) {

        Mapper cachedMapper = cache.get(object.getClass());

        if (cachedMapper != null) {
            return cachedMapper.serialize(object, this);
        }

        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            return "{" + map.entrySet()
                    .stream()
                    .map(entry -> "\"" + entry.getKey() + "\": " + serialize(entry.getValue()))
                    .collect(Collectors.joining(", ")) + "}";
        }

        if (object instanceof Collection) {
            Collection<?> list = (Collection<?>) object;
            return "[" + list.stream()
                    .map(this::serialize)
                    .collect(Collectors.joining(", ")) + "]";
        }

        final Class<?> clazz = object.getClass();

        if (clazz.getName()
                .startsWith("java.lang")) {

            return writeValue(object);
        }

        Method[] methods = clazz.getDeclaredMethods();

        Mapper mapper = new Mapper(clazz);
        cache.put(clazz, mapper);

        for (Method method : methods) {
            try {

                if (method.getName().equals("hashCode")) continue;
                if (method.getName().equals("toString")) continue;

                mapper.put(method);
            } catch (Throwable e) {
                Logger.getLogger(JsonEncoder.class.getName())
                        .log(Level.SEVERE, "Error serializing object: " + object.getClass().getName(), e);
            }

        }

        return mapper.serialize(object, this);
    }

    private String writeField(String name, Object value, boolean includeNonNull) {

        StringBuilder builder = new StringBuilder();

        try {

            if (includeNonNull && value == null) return null;

            builder.append("\"").append(name).append("\": ");

            if (value instanceof Map) {
                builder.append("{");

                String map = ((Map<?, ?>) value)
                        .entrySet()
                        .stream()
                        .map(entry -> "\"" + entry.getKey() + "\": " + serialize(entry.getValue()))
                        .collect(Collectors.joining(", "));

                builder.append(map)
                        .append("}");

                return builder.toString();
            }

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
            Logger.getLogger(JsonEncoder.class.getName())
                    .log(Level.SEVERE, null, e);
        }

        return builder.toString();
    }

    private static String getFieldName(Method method) {

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


        ValueSerializer serializer = serializers.get(value.getClass());

        if (serializer != null) {
            return serializer.serialize(value);
        }

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

    private static class Mapper extends HashMap<String, Function<Object, Object>> {

        private final Class<?> target;
        private final boolean includeNonNull;

        public Mapper(Class<?> target) {
            this.target = target;
            includeNonNull = target.isAnnotationPresent(JsonIncludeNonNull.class);
        }

        private void put(Method method) throws Throwable {

            if (method.isAnnotationPresent(JsonIgnore.class)) return;
            if (Modifier.isStatic(method.getModifiers())) return;
            if (method.getParameterCount() > 0) return;

            String fieldName = getFieldName(method);
            put(fieldName, createFunction(target, method.getName(), method.getReturnType()));
        }

        private String serialize(Object object, JsonEncoder encoder) {

            List<String> properties = new ArrayList<>();

            for (Entry<String, Function<Object, Object>> entry : entrySet()) {

                Object value = entry.getValue().apply(object);

                if (value == null && !includeNonNull) continue;

                String field = encoder.writeField(entry.getKey(), value, includeNonNull);

                if (field == null) continue;

                properties.add(field);
            }

            return "{" + String.join(", ", properties) + "}";
        }
    }

    private static Function<Object, Object> createFunction(Class<?> targetClass, String targetMethod, Class<?> targetMethodReturnType) throws Throwable {
        try {
            MethodHandles.Lookup lookup = getLookup(targetClass);
            MethodHandle virtualMethodHandle = lookup.findVirtual(targetClass, targetMethod, MethodType.methodType(targetMethodReturnType));
            CallSite site = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    virtualMethodHandle,
                    MethodType.methodType(targetMethodReturnType, targetClass));
            @SuppressWarnings("unchecked")
            Function<Object, Object> getterFunction = (Function<Object, Object>) site.getTarget().invokeExact();
            return getterFunction;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to create function for " + targetClass + " at " + targetMethod, e);
        }
    }

    private static MethodHandles.Lookup getLookup(Class<?> targetClass) {
        MethodHandles.Lookup lookupMe = MethodHandles.lookup();

        try {
            return MethodHandles.privateLookupIn(targetClass, lookupMe);
        } catch (IllegalAccessException e) {
            return lookupMe;
        }
    }
}
