package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.implementation.*;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;
import net.jonathangiles.tools.teenyhttpd.model.ServerSentEventHandler;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeenyApplication {
    private static TeenyApplication instance;

    public static TeenyApplication start() {
        if (instance == null) {
            instance = new TeenyApplication(new BootstrapConfiguration());
        }

        instance._start();

        return instance;
    }

    public static TeenyApplication start(Class<?> clazz) {
        start();
        instance.scan(clazz);
        return instance;
    }

    public static <T> T getResource(Class<T> clazz) {
        return instance.resourceManager.getFirstInstanceOf(clazz);
    }

    public static MessageConverter getMessageConverter(String contentType) {
        return instance.messageConverterMap.get(contentType);
    }

    public static String getProperty(String key) {
        return instance.configuration.getProperty(key, true, String.class);
    }

    public static void stop() {
        if (instance == null) return;

        instance._stop();
    }

    private final TeenyHttpd server;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String, MessageConverter> messageConverterMap;
    private final Map<String, ServerSentEventHandler> eventMap = new HashMap<>();
    private ResourceManager resourceManager;
    private final BootstrapConfiguration configuration;

    private TeenyApplication(BootstrapConfiguration configuration) {
        this.configuration = configuration;
        configuration.readConfigurations();
        server = new TeenyHttpd(configuration.getServerPort());
        this.messageConverterMap = new ConcurrentHashMap<>();
        this.messageConverterMap.put(DefaultMessageConverter.INSTANCE.getContentType(), DefaultMessageConverter.INSTANCE);
        this.messageConverterMap.put("application/json", new net.jonathangiles.tools.teenyhttpd.json.TeenyJsonMessageConverter());
    }

    public TeenyApplication registerMessageConverter(MessageConverter messageConverter) {
        messageConverterMap.put(messageConverter.getContentType(), messageConverter);
        return this;
    }

    private void scan(Class<?> clazz) {
        Set<Class<?>> classList = AnnotationScanner.scan(clazz);

        //Annotated classes with @Configuration will be automatically registered
        classList.addAll(AnnotationScanner.scan("net.jonathangiles.tools.teenyhttpd.configuration"));

        if (clazz.isAnnotationPresent(ScanPackage.class)) {
            ScanPackage scanPackage = clazz.getAnnotation(ScanPackage.class);
            for (String packageName : scanPackage.value()) {
                Logger.getLogger(TeenyApplication.class.getName())
                        .log(Level.INFO, "Scanning package: " + packageName);

                classList.addAll(AnnotationScanner.scan(packageName));
            }
        }

        Set<ResourceManager.OrderedClass> enabledClasses = new HashSet<>();

        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation instanceof Enables) {
                Enables enables = (Enables) annotation;

                for (Enable enable : enables.value()) {
                    enabledClasses.add(new ResourceManager.OrderedClass(enable.value(), enable.order()));
                }
            }

            if (annotation instanceof Enable) {
                Enable enable = (Enable) annotation;
                enabledClasses.add(new ResourceManager.OrderedClass(enable.value(), enable.order()));
            }

            //Register sugar annotations like @EnableOpenApi
            if (annotation.annotationType().isAnnotationPresent(Enables.class)) {
                Enables enables = annotation.annotationType().getAnnotation(Enables.class);

                for (Enable enable : enables.value()) {
                    enabledClasses.add(new ResourceManager.OrderedClass(enable.value(), enable.order()));
                }
            }

            if (annotation.annotationType().isAnnotationPresent(Enable.class)) {
                Enable enable = annotation.annotationType().getAnnotation(Enable.class);
                enabledClasses.add(new ResourceManager.OrderedClass(enable.value(), enable.order()));
            }
        }

        resourceManager = new ResourceManager(classList, enabledClasses, configuration);
        resourceManager.initialize();

        resourceManager.findInstancesOf(MessageConverter.class)
                .forEach(this::registerMessageConverter);

        resourceManager.findControllers()
                .forEach(this::register);

        resourceManager.getOnApplicationReadyListeners()
                .forEach(listener -> listener.invoke(null));
    }

    /**
     * Register a controller class with the application
     * @param controller The controller class to register
     * @return The application instance
     */
    public TeenyApplication register(Class<?> controller) {
        Objects.requireNonNull(controller, "Controller cannot be null");

        Optional<Constructor<?>> opConstructor = Arrays.stream(controller.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findFirst();

        if (opConstructor.isPresent()) {
            try {
                Object instance = opConstructor.get().newInstance();

                addController(instance);

                return this;
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
        Logger.getLogger(TeenyApplication.class.getName())
                .log(Level.INFO, "Registering controller: " + controller.getClass().getName());

        Objects.requireNonNull(controller, "Controller cannot be null");

        Method[] methods = controller.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Configuration.class)) {
                handleConfiguration(controller, method);
                continue;
            }

            if (method.isAnnotationPresent(ServerEvent.class)
                    && ServerSentEventHandler.class.isAssignableFrom(method.getReturnType())) {
                addServerEvent(controller, method);
            }
        }

        Path context = controller.getClass()
                .getAnnotation(Path.class);

        String contextPath = context == null ? "" : context.value();

        List<EventListenerHandler<EndpointMapping>> eventListeners = resourceManager.getEventListeners(EndpointMapping.class);

        for (Method method : methods) {
            if (isEndpoint(method)) {
                EndpointMapping mapping = addEndpoint(contextPath, controller, method);

                eventListeners.forEach(listener -> listener.invoke(mapping));
            }
        }
    }

    public static boolean isEndpoint(Method method) {
        return method.isAnnotationPresent(Get.class) ||
                method.isAnnotationPresent(Post.class) ||
                method.isAnnotationPresent(Delete.class) ||
                method.isAnnotationPresent(Put.class) ||
                method.isAnnotationPresent(Patch.class);
    }

    private void handleConfiguration(Object controller, Method method) {
        if (Void.class.isAssignableFrom(method.getReturnType())) {
            return;
        }

        try {
            method.setAccessible(true);
            Object configuration = method.invoke(controller);

            if (configuration instanceof MessageConverter) {
                registerMessageConverter((MessageConverter) configuration);
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void addServerEvent(Object controller, Method method) {

        method.setAccessible(true);

        try {

            ServerEvent annot = method.getAnnotation(ServerEvent.class);

            String route = annot.value();
            String name = annot.name().trim();

            if (name.isEmpty()) {
                name = method.getName();
            }

            if (eventMap.containsKey(name)) {
                throw new IllegalStateException("Error at function " + method.getName() + " at route " + route + " Event name already exists: " + name);
            }

            ServerSentEventHandler result = (ServerSentEventHandler) method.invoke(controller);
            eventMap.put(name, result);
            server.addServerSentEventRoute(route, result);

            Logger.getLogger(TeenyApplication.class.getName()).log(Level.INFO, "Added server event: " + name + " at route " + route);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private EndpointMapping addEndpoint(String basePath, Object controller, Method method) {
        EndpointMapping mapping = new EndpointMapping(basePath, method);

        if (File.class.isAssignableFrom(method.getReturnType())) {
            try {
                method.setAccessible(true);
                server.addFileRoute(mapping.getPath(), (File) method.invoke(controller));
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.getLogger(TeenyApplication.class.getName()).log(Level.SEVERE, "Error adding file route", e);
            }

            return mapping;
        }

        server.addRoute(mapping.getMethod(), mapping.getPath(),
                new EndpointHandler(mapping, controller, messageConverterMap, eventMap));

        return mapping;
    }


    private synchronized void _stop() {
        server.stop();
        started.set(false);
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
