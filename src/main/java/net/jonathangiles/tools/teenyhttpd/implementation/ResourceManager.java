package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceManager {

    private final Map<Class<?>, Map<String, Object>> resources;
    private Set<Class<?>> classes;
    private final BootstrapConfiguration configuration;

    public ResourceManager(Set<Class<?>> classes, BootstrapConfiguration configuration) {
        this.configuration = configuration;
        this.resources = new HashMap<>();
        this.classes = classes;
    }

    public void initialize() {
        List<Class<?>> configurations = classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Configuration.class))
                .collect(Collectors.toList());

        List<Class<?>> resources = classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Resource.class))
                .collect(Collectors.toList());

        for (Class<?> configuration : configurations) {
            initialize(configuration);
        }

        for (Class<?> resource : resources) {
            initialize(resource);
        }

        classes = classes.stream()
                .filter(clazz -> !clazz.isAnnotationPresent(Configuration.class)
                        && !clazz.isAnnotationPresent(Resource.class))
                .sorted((A, B) -> Integer.compare(getConstructor(A).getParameterCount(),
                        getConstructor(B).getParameterCount()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Class<?> aClass : classes) {
            initialize(aClass);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findInstancesOf(Class<T> clazz) {
        List<T> result = new LinkedList<>();
        for (Map<String, Object> map : resources.values()) {
            for (Object object : map.values()) {
                if (clazz.isAssignableFrom(object.getClass())) {
                    result.add((T) object);
                }
            }
        }
        return result;
    }

    public <T> T getFirstInstanceOf(Class<T> clazz) {
        List<T> instances = findInstancesOf(clazz);
        if (instances.isEmpty()) {
            return null;
        }
        return instances.get(0);
    }

    public List<Object> findControllers() {
        List<Object> result = new LinkedList<>();

        for (Map<String, Object> map : resources.values()) {
            for (Object object : map.values()) {

                if (object.getClass().isAnnotationPresent(Path.class)) {
                    result.add(object);
                    continue;
                }

                Method[] methods = object.getClass().getDeclaredMethods();

                for (Method method : methods) {
                    if (method.isAnnotationPresent(Get.class)
                            || method.isAnnotationPresent(Delete.class)
                            || method.isAnnotationPresent(Put.class)
                            || method.isAnnotationPresent(Post.class)
                            || method.isAnnotationPresent(Patch.class)
                            || method.isAnnotationPresent(ServerEvent.class)) {
                        result.add(object);
                        break;
                    }
                }
            }
        }

        return result;
    }

    public List<Object> findAnnotatedWith(Class<? extends Annotation> annotation) {
        List<Object> result = new LinkedList<>();
        for (Map<String, Object> map : resources.values()) {
            for (Object object : map.values()) {
                if (object.getClass().isAnnotationPresent(annotation)) {
                    result.add(object);
                }
            }
        }
        return result;
    }

    public List<Object> allResources() {
        List<Object> result = new LinkedList<>();
        for (Map<String, Object> map : resources.values()) {
            result.addAll(map.values());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T findResource(Class<?> clazz, String name) {
        Map<String, Object> resourceMap = resources.get(clazz);

        if (resourceMap == null) {
            return initialize((Class<T>) clazz);
        }

        if (name != null) {
            return (T) resourceMap.get(name);
        }

        return (T) resourceMap.values().iterator().next();
    }

    private Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length > 1) {
            throw new RuntimeException("Class " + clazz.getName() + " has more than one constructor");
        }

        return constructors[0];
    }


    @SuppressWarnings("unchecked")
    private <T> T initialize(Class<T> clazz) {
        System.out.println("Starting: " + clazz);

        Constructor<?> constructor = getConstructor(clazz);

        T instance;

        if (constructor.getParameterCount() == 0) {
            try {
                instance = (T) constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize class " + clazz.getName(), e);
            }
        } else {

            Object[] args = new Object[constructor.getParameterCount()];

            for (int i = 0; i < args.length; i++) {
                Class<?> parameterType = constructor.getParameterTypes()[i];
                String name = parameterType.getName();
                args[i] = findResource(parameterType, name);
            }

            try {
                instance = (T) constructor.newInstance(args);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize class " + clazz.getName(), e);
            }
        }

        start(clazz, instance);
        register(clazz, instance);

        return instance;
    }

    private void start(Class<?> clazz, Object instance) {
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Value.class)) {
                Value value = field.getAnnotation(Value.class);
                Object property = configuration.getProperty(value.value(), value.required(), field.getType());
                try {
                    field.setAccessible(true);
                    field.set(instance, property);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject property " + value.value() + " into field " + field.getName() + " on class " + clazz.getName(), e);
                }
            }

            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                String name = fieldType.getName();
                Object resource = findResource(fieldType, name);

                try {
                    field.setAccessible(true);
                    field.set(instance, resource);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject resource " + name + " into field " + field.getName() + " on class " + clazz.getName(), e);
                }
            }
        }

        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Inject.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] args = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    String name = parameterType.getName();
                    args[i] = findResource(parameterType, name);
                }

                try {
                    method.invoke(instance, args);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke method " + method.getName() + " on class " + clazz.getName(), e);
                }
            }
        }

        for (Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                try {

                    if (!Modifier.isPublic(method.getModifiers())) {
                        method.setAccessible(true);
                    }

                    method.invoke(instance);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke method " + method.getName() + " on class " + clazz.getName(), e);
                }
            }
        }
    }

    private void register(Class<?> clazz, Object instance) {
        resources.put(clazz, new HashMap<>());
        resources.get(clazz).put(clazz.getName(), instance);

        for (Class<?> anInterface : clazz.getInterfaces()) {
            resources.putIfAbsent(anInterface, new HashMap<>());
            resources.get(anInterface).put(anInterface.getName(), instance);
        }

    }

}
