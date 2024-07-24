package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnnotationScanner {

    public static Set<Class<?>> scan(Class<?> target) {

        Set<Class<?>> classList = new HashSet<>();

        String packageName = target.getPackageName();

        List<String> packages = getPackages(packageName);
        packages.add(packageName);

        for (String aPackage : packages) {
            classList.addAll(scan(aPackage));
        }

        if (hasEndpoints(target)) {
            classList.add(target);
        }

        return classList;
    }

    public static Set<Class<?>> scan(String aPackage) {
        Set<Class<?>> classList = new HashSet<>();
        try {
            List<Class<?>> classes = getClasses(aPackage);

            for (Class<?> clazz : classes) {
                if (hasAnnotations(clazz)) {
                    classList.add(clazz);
                }
            }

        } catch (ClassNotFoundException | IOException e) {
            Logger.getLogger(AnnotationScanner.class.getName())
                    .log(Level.SEVERE, null, e);
        }

        return classList;
    }

    private static boolean hasAnnotations(Class<?> clazz) {

        if (clazz.isInterface()) return false;

        if (clazz.isAnnotationPresent(Resource.class)) {
            return true;
        }

        if (clazz.isAnnotationPresent(Configuration.class)) {
            return true;
        }

        if (!clazz.isAnnotationPresent(Path.class)) {
            return false;
        }

        return hasEndpoints(clazz);
    }

    private static boolean hasEndpoints(Class<?> clazz) {


        for (Method method : clazz.getDeclaredMethods()) {

            if (method.isAnnotationPresent(ServerEvent.class)) {
                return true;
            }

            if (isEndpoint(method)) {
                return true;
            }

        }

        return false;
    }

    public static boolean isEndpoint(Method method) {


        if (method.isAnnotationPresent(Get.class)) {
            return true;
        }

        if (method.isAnnotationPresent(Delete.class)) {
            return true;
        }

        if (method.isAnnotationPresent(Put.class)) {
            return true;
        }

        if (method.isAnnotationPresent(Post.class)) {
            return true;
        }

        return method.isAnnotationPresent(Patch.class);
    }


    private static List<String> getPackages(String packageName) {
        List<String> packages = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            packages.add(packageName + "." + file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(AnnotationScanner.class.getName())
                    .log(Level.SEVERE, null, e);
        }
        return packages;
    }

    private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<>();

        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}