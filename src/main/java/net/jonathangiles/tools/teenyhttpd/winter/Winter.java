package net.jonathangiles.tools.teenyhttpd.winter;

import net.jonathangiles.tools.teenyhttpd.TeenyHttpd;
import net.jonathangiles.tools.teenyhttpd.winter.annot.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.jonathangiles.tools.teenyhttpd.model.Method.*;

/**
 * Winter teeny-framework to bootstrap a teeny server and his controllers
 */
public class Winter {

    private static Winter instance;

    public static Winter bootstrap() {
        if (instance == null) {
            instance = new Winter();
        }

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

    private Winter() {
        server = new TeenyHttpd(Integer.parseInt(System.getProperty("server.port", "8080")));
    }


    public Winter add(Class<?> controller) {

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

        return this;
    }

    public Winter add(Object controller) {
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
                    method.isAnnotationPresent(Patch.class)
            ) {
                addEndpoint(controller, method);
            }
        }
    }

    private void addEndpoint(Object controller, Method method) {
        server.addRoute(getMethod(method), getRoute(controller, method), new EndpointHandler(method, controller));
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

        path = path.replaceAll("}", "")
                .replaceAll("\\{", ":");

        if (context.value() != null) {

            if (path.startsWith("/")) {
                path = context.value() + path;
            } else {
                path = context.value() + "/" + path;
            }

        }

        Logger.getLogger(Winter.class.getName())
                .log(Level.INFO, "Winter-route: " + path);

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

    public synchronized void start() {
        if (started.get()) {
            throw new IllegalStateException("Server already started");
        }

        //spend more time doing this banner lol
        if (System.getProperty("banner", "true").equals("true")) {
            System.out.println("          _       _            \n" +
                    "         (_)     | |                .      .\n" +
                    "__      ___ _ __ | |_ ___ _ __      _\\/  \\/_\n" +
                    "\\ \\ /\\ / / | '_ \\| __/ _ \\ '__|      _\\/\\/_\n" +
                    " \\ V  V /| | | | | ||  __/ |     _\\_\\_\\/\\/_/_/_\n" +
                    "  \\_/\\_/ |_|_| |_|\\__\\___|_|      / /_/\\/\\_\\ \\\n" +
                    "  version 1.0                        _/\\/\\_\n" +
                    "                                     /\\  /\\\n" +
                    "                                    '      '\n");
        }

        server.start();
    }

}
