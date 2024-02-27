package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.json.JsonAlias;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz) {

        Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(cons -> cons.getParameterCount() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No default constructor found for " + clazz.getName()));

        try {

            constructor.setAccessible(true);

            return (T) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to create new instance of " + clazz.getName(), e);
        }
    }

    public static ParameterType getParameterType(Parameter parameter) {
        return getParameterType(parameter.getParameterizedType());
    }

    public static ParameterType getParameterType(Field field) {
        return getParameterType(field.getGenericType());
    }

    public static ParameterType getParameterType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            System.out.println("type = " + type);
            return null;
        }

        Type actualTypeArgument = ((ParameterizedType) type)
                .getActualTypeArguments()[0];

        if (actualTypeArgument instanceof Class<?>) {

            Class<?> c = (Class<?>) actualTypeArgument;

            return new ParameterType(c, null);
        }

        if (actualTypeArgument instanceof ParameterizedType) {

            ParameterizedType parameterizedType = (ParameterizedType) actualTypeArgument;

            return new ParameterType((Class<?>) parameterizedType.getActualTypeArguments()[0],
                    (Class<?>) parameterizedType.getRawType());
        }

        throw new IllegalStateException("Unknown type: " + actualTypeArgument);
    }

    public static Map<String, Field> getFields(Class<?> clazz) {

        Map<String, Method> methodMap = Arrays.stream(clazz.getDeclaredMethods())
                .collect(Collectors.toMap(Method::getName, m -> m));

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(ReflectionUtils::isWritable)
                .collect(Collectors.toMap(field -> getFieldName(field, methodMap), f -> f));
    }

    private static String getFieldName(Field field, Map<String, Method> methodMap) {

        Method accessorMethod;

        String baseName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

        if (field.getType() == boolean.class) {
            accessorMethod = methodMap.get("is" + baseName);
        } else {
            accessorMethod = methodMap.get("get" + baseName);
        }

        if (accessorMethod != null) {
            JsonAlias jsonAlias = accessorMethod.getAnnotation(JsonAlias.class);

            if (jsonAlias != null) {
                return jsonAlias.value();
            }
        }

        return field.getName();
    }

    public static Map<String, Method> getMutators(Class<?> clazz) {
        Map<String, Method> methodMap = Arrays.stream(clazz.getDeclaredMethods())
                .collect(Collectors.toMap(Method::getName, m -> m));

        Map<String, Method> resultMap = new HashMap<>();

        for (Method method : methodMap.values()) {
            if (!isMutator(method)) continue;

            String mutatorName = getMutatorName(method, methodMap);

            resultMap.put(mutatorName, method);
        }

        return resultMap;
    }

    /**
     * Here we seek for the alias key in the accessor method, if it exists.
     */
    private static String getMutatorName(Method method, Map<String, Method> methodMap) {

        String baseName = method.getName().substring(3);

        Method accessorMethod;

        if (method.getParameterTypes()[0] == boolean.class) {
            accessorMethod = methodMap.get("is" + baseName);
        } else {
            accessorMethod = methodMap.get("get" + baseName);
        }

        if (accessorMethod != null) {
            JsonAlias jsonAlias = accessorMethod.getAnnotation(JsonAlias.class);

            if (jsonAlias != null) {
                return jsonAlias.value();
            }
        }

        return method.getName().substring(3, 4).toLowerCase()
                + method.getName().substring(4);
    }

    private static boolean isMutator(Method method) {
        return method.getName().startsWith("set") && method.getParameterCount() == 1;
    }

    private static boolean isWritable(Field field) {

        if (Modifier.isStatic(field.getModifiers())) {
            return false;
        }

        return !Modifier.isFinal(field.getModifiers());
    }

}
