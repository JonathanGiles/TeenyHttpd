package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Utility class to deal with request mappings.
 */
public class EndpointMapping {

    private final net.jonathangiles.tools.teenyhttpd.model.Method method;
    private final String value;
    private final String contentType;
    private final Method target;
    private final String basePath;

    public EndpointMapping(String basePath, Method target) {
        this.basePath = basePath;
        this.target = target;
        if (target.isAnnotationPresent(Get.class)) {
            Get annot = target.getAnnotation(Get.class);
            contentType = annot.produces();
            method = net.jonathangiles.tools.teenyhttpd.model.Method.GET;
            value = annot.value().trim();
        } else if (target.isAnnotationPresent(Post.class)) {
            Post annot = target.getAnnotation(Post.class);
            contentType = annot.produces();
            value = annot.value().trim();
            method = net.jonathangiles.tools.teenyhttpd.model.Method.POST;
        } else if (target.isAnnotationPresent(Delete.class)) {
            Delete annot = target.getAnnotation(Delete.class);
            contentType = annot.produces();
            value = annot.value().trim();
            method = net.jonathangiles.tools.teenyhttpd.model.Method.DELETE;
        } else if (target.isAnnotationPresent(Put.class)) {
            Put annot = target.getAnnotation(Put.class);
            contentType = annot.produces();
            value = annot.value().trim();
            method = net.jonathangiles.tools.teenyhttpd.model.Method.PUT;
        } else if (target.isAnnotationPresent(Patch.class)) {
            Patch annot = target.getAnnotation(Patch.class);
            contentType = annot.produces();
            method = net.jonathangiles.tools.teenyhttpd.model.Method.PATCH;
            value = annot.value().trim();
        } else {
            throw new IllegalArgumentException("Method must be annotated with either @Get, @Post, @Delete, @Put, or @Patch");
        }

        validate();
    }

    /**
     * Validates the method to ensure that it is correctly annotated.
     */
    private void validate() {
        String route = getPath();

        if (route.contains("?")) {
            throw new IllegalStateException("Error at function " + target.getName() + " at route " + route + " Query parameters must be annotated with @QueryParam");
        }

        int bodyCount = 0;

        for (Parameter parameter : target.getParameters()) {

            if (parameter.isAnnotationPresent(RequestBody.class)) {
                bodyCount++;
            }

            if (parameter.isAnnotationPresent(QueryParam.class)) {
                QueryParam queryParam = parameter.getAnnotation(QueryParam.class);

                if (queryParam.value().isEmpty()) {
                    throw new IllegalStateException("Error at function " + target.getName() + " at parameter " + parameter.getName() + " QueryParam value cannot be empty");
                }
            }

            if (parameter.isAnnotationPresent(RequestHeader.class)) {
                RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);

                if (requestHeader.value().isEmpty()) {
                    throw new IllegalStateException("Error at function " + target.getName() + " at parameter " + parameter.getName() + " RequestHeader value cannot be empty");
                }
            }

            if (parameter.isAnnotationPresent(PathParam.class)) {
                PathParam pathParam = parameter.getAnnotation(PathParam.class);

                if (pathParam.value().isEmpty()) {
                    throw new IllegalStateException("Error at function " + target.getName() + " at parameter " + parameter.getName() + " PathParam value cannot be empty");
                }
            }
        }

        if (bodyCount > 1) {
            throw new IllegalStateException("Error at function " + target.getName() + " at route " + route + " Only one parameter can be annotated with @RequestBody");
        }
    }

    public Object call(Object parent, Object[] args) {
        try {
            return target.invoke(parent, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Parameter[] getParameters() {
        return target.getParameters();
    }

    public Method getTarget() {
        return target;
    }

    public String getValue() {
        return value;
    }

    public String getPath() {
        return basePath + value;
    }

    public String getContentType() {
        return contentType;
    }

    public net.jonathangiles.tools.teenyhttpd.model.Method getMethod() {
        return method;
    }
}
