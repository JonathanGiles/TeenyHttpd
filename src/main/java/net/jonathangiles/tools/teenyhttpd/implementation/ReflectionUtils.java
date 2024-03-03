package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.json.JsonAlias;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A utility class to help with reflection.
 */
public final class ReflectionUtils {

    /**
     * Create a new instance of the given class.
     * <p>
     * @param clazz the class to create a new instance of
     * @return a new instance of the given class
     * @param <T> the type of the class
     * @throws IllegalArgumentException if the class does not have a default constructor or if the constructor fails
     */
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

    public static ParameterizedTypeHelper getParameterType(Parameter parameter) {
        return getParameterType(parameter.getParameterizedType());
    }

    public static ParameterizedTypeHelper getParameterType(Field field) {
        return getParameterType(field.getGenericType());
    }

    /**
     * Creates an instance of ParameterizedTypeHelper from the given type.
     * <p>
     * @param type the type to create the instance from
     * @return the instance of ParameterizedTypeHelper or null if the type is not a ParameterizedType
     * @throws IllegalStateException if the type is not a Class or a ParameterizedType
     */
    public static ParameterizedTypeHelper getParameterType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        Type actualTypeArgument = ((ParameterizedType) type)
                .getActualTypeArguments()[0];

        if (actualTypeArgument instanceof Class<?>) {
            return new ParameterizedTypeHelper((Class<?>) actualTypeArgument);
        }

        if (actualTypeArgument instanceof ParameterizedType) {

            ParameterizedType parameterizedType = (ParameterizedType) actualTypeArgument;

            return new ParameterizedTypeHelper((Class<?>) parameterizedType.getRawType(),
                    parameterizedType.getActualTypeArguments());
        }

        throw new IllegalStateException("Unknown type: " + actualTypeArgument);
    }

    /**
     * Returns a map of the writable fields of the given class,
     * where the key is the name of the field or his alias and the value is the field itself.
     * <p>
     * @param clazz the class to get the fields from
     * @return a map of the fields of the given class
     */
    public static Map<String, Field> getFields(Class<?> clazz) {
        Map<String, Method> methodMap = Arrays.stream(clazz.getDeclaredMethods())
                .collect(Collectors.toMap(Method::getName, m -> m));

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(ReflectionUtils::isWritable)
                .collect(Collectors.toMap(field -> getFieldName(field, methodMap), f -> f));
    }

    /**
     * Here we seek for the alias key in the accessor method, if it exists.
     */
    private static String getFieldName(Field field, Map<String, Method> methodMap) {
        //base name of the field
        String baseName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        String alias = findAlias(methodMap, baseName, field.getType());

        return Objects.requireNonNullElse(alias, field.getName());
    }

    /**
     * Here we seek for the alias key in the accessor method, if it exists.
     */
    private static String getMutatorName(Method method, Map<String, Method> methodMap) {
        //base name of the method
        String baseName = method.getName().substring(3);
        //seek for the alias key in the accessor method, if it exists
        String alias = findAlias(methodMap, baseName, method.getParameterTypes()[0]);
        if (alias != null) {
            return alias;
        }

        return method.getName().substring(3, 4).toLowerCase()
                + method.getName().substring(4);
    }

    private static String findAlias(Map<String, Method> methodMap, String baseName, Class<?> type) {
        Method accessorMethod;

        if (type == boolean.class) {
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

        return null;
    }

    /**
     * Returns a map of the mutator methods of the given class,
     * where the key is the name of the mutator or his alias and the value is the method itself.
     * <p>
     * @param clazz the class to get the mutators from
     * @return a map of the mutators of the given class
     */
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
