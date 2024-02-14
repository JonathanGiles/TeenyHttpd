package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.jonathangiles.tools.teenyhttpd.model.Method.*;

public class TeenyApplication {
    private static TeenyApplication instance;

    public static TeenyApplication start() {
        if (instance == null) {
            instance = new TeenyApplication();
        }

        instance._start();

        return instance;
    }

    public static void stop() {
        if (instance != null) {
            instance.server.stop();
            instance.started.set(false);
        }
    }

    private final TeenyHttpd server;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String, MessageConverter> messageConverterMap;

    private TeenyApplication() {
        server = new TeenyHttpd(Integer.parseInt(System.getProperty("server.port", "8080")));
        this.messageConverterMap = new HashMap<>();
    }

    public TeenyApplication registerMessageConverter(MessageConverter messageConverter) {
        messageConverterMap.put(messageConverter.getContentType(), messageConverter);
        return this;
    }

    public TeenyApplication register(Class<?> controller) {

        Objects.requireNonNull(controller, "Controller cannot be null");

        Optional<Constructor<?>> opConstructor = Arrays.stream(controller.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findFirst();

        if (opConstructor.isPresent()) {
            try {
                Object instance = opConstructor.get().newInstance();
                addController(instance);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        throw new IllegalArgumentException("Controller must have a default constructor");
    }

    @SuppressWarnings("UnusedReturnValue")
    public TeenyApplication register(Object controller) {
        addController(controller);
        return this;
    }

    private void addController(Object controller) {

        Objects.requireNonNull(controller, "Controller cannot be null");

        Method[] methods = controller.getClass().getDeclaredMethods();

        for (Method method : methods) {

            if (method.isAnnotationPresent(Get.class) ||
                    method.isAnnotationPresent(Post.class) ||
                    method.isAnnotationPresent(Delete.class) ||
                    method.isAnnotationPresent(Put.class) ||
                    method.isAnnotationPresent(Patch.class)) {

                addEndpoint(controller, method);
            }
        }
    }

    private void addEndpoint(Object controller, Method method) {
        server.addRoute(getMethod(method), getRoute(controller, method),
                new EndpointHandler(method, controller, messageConverterMap));
    }

    private String getRoute(Object controller, Method method) {

        Path context = controller.getClass()
                .getAnnotation(Path.class);

        String path = "";

        if (method.isAnnotationPresent(Get.class)) {
            path = method.getAnnotation(Get.class).value();
        }

        if (method.isAnnotationPresent(Post.class)) {
            path = method.getAnnotation(Post.class).value();
        }

        if (method.isAnnotationPresent(Delete.class)) {
            path = method.getAnnotation(Delete.class).value();
        }

        if (method.isAnnotationPresent(Put.class)) {
            path = method.getAnnotation(Put.class).value();
        }

        if (method.isAnnotationPresent(Patch.class)) {
            path = method.getAnnotation(Patch.class).value();
        }

        if (context != null && context.value() != null) {

            if (path.startsWith("/")) {
                path = context.value() + path;
            } else {
                path = context.value() + "/" + path;
            }

        }

        Logger.getLogger(TeenyApplication.class.getName())
                .log(Level.INFO, "Teeny-route: " + path);

        return path;
    }

    private net.jonathangiles.tools.teenyhttpd.model.Method getMethod(Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            return GET;
        }

        if (method.isAnnotationPresent(Post.class)) {
            return POST;
        }

        if (method.isAnnotationPresent(Delete.class)) {
            return DELETE;
        }

        if (method.isAnnotationPresent(Put.class)) {
            return PUT;
        }

        if (method.isAnnotationPresent(Patch.class)) {
            return PATCH;
        }

        throw new IllegalArgumentException("Method not supported: " + method.getName());
    }

    private synchronized void _start() {

        if (started.get()) {
            return;
        }

        if (System.getProperty("banner", "true").equals("true")) {
            System.out.println(" _________  ______   ______   ___   __    __  __    \n" +
                    "/________/\\/_____/\\ /_____/\\ /__/\\ /__/\\ /_/\\/_/\\   \n" +
                    "\\__.::.__\\/\\::::_\\/_\\::::_\\/_\\::\\_\\\\  \\ \\\\ \\ \\ \\ \\  \n" +
                    "   \\::\\ \\   \\:\\/___/\\\\:\\/___/\\\\:. `-\\  \\ \\\\:\\_\\ \\ \\ \n" +
                    "    \\::\\ \\   \\::___\\/_\\::___\\/_\\:. _    \\ \\\\::::_\\/ \n" +
                    "     \\::\\ \\   \\:\\____/\\\\:\\____/\\\\. \\`-\\  \\ \\ \\::\\ \\ \n" +
                    "      \\__\\/    \\_____\\/ \\_____\\/ \\__\\/ \\__\\/  \\__\\/ \n" +
                    "                                                    \n");
            System.out.println("Version: 1.0.0");
        }

        server.start();
    }
}
