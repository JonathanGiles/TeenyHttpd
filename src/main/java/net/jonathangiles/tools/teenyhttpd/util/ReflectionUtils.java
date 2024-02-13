package net.jonathangiles.tools.teenyhttpd.util;

import java.lang.reflect.*;
import java.util.Arrays;

public class ReflectionUtils {

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

}
