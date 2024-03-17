package net.jonathangiles.tools.teenyhttpd.json;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * A class that wraps a method and a function that can be used to invoke the method. This is used to invoke methods on
 * objects that are annotated with {@link JsonIgnore}, {@link JsonIncludeNonNull}, or {@link JsonRaw}.
 */
final class AccessorInvoker {
    private final Method method;
    private final Function<Object, Object> function;

    public AccessorInvoker(Method method, Function<Object, Object> function) {
        this.method = method;
        this.function = function;
    }

    /**
     * Invokes the method on the given instance, returning the result.
     * @param instance the instance to invoke the method on
     * @return the result of invoking the method on the given instance (or null if an error occurred)
     */
    public Object invoke(Object instance) {
        try {
            return function.apply(instance);
        } catch (Exception ex) {
            Logger.getLogger(AccessorInvoker.class.getName())
                    .severe("Error invoking method: " + method.getName());
        }

        return null;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return method.isAnnotationPresent(annotation);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return method.getAnnotation(annotation);
    }
}
