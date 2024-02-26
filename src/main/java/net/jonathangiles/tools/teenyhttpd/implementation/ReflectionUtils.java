package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.json.JsonAlias;

import java.lang.reflect.*;
import java.util.Arrays;
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
        if (type instanceof Class<?>) {
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

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(ReflectionUtils::isWritable)
                .collect(Collectors.toMap(Field::getName, f -> f));
    }

    public static Map<String, Method> getMutators(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(ReflectionUtils::isMutator)
                .collect(Collectors.toMap(ReflectionUtils::getName, m -> m));

    }

    private static String getName(Method method) {

        if (method.isAnnotationPresent(JsonAlias.class)) {
            return method.getAnnotation(JsonAlias.class).value();
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
