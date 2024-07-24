package net.jonathangiles.tools.teenyhttpd.implementation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class to deal with event listeners.
 */
public class EventListenerHandler<T> {

    private final Method method;
    private final Object parent;

    public EventListenerHandler(Method method, Object parent) {
        this.method = method;
        this.parent = parent;
    }

    public void invoke(T arg) {
        try {
            if(method.getParameterCount() == 0){
                method.invoke(parent);
                return;
            }

            method.invoke(parent, arg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
