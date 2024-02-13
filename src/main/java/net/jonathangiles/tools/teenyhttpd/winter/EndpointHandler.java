package net.jonathangiles.tools.teenyhttpd.winter;

import net.jonathangiles.tools.teenyhttpd.implementation.EmptyResponse;
import net.jonathangiles.tools.teenyhttpd.implementation.StringResponse;
import net.jonathangiles.tools.teenyhttpd.json.TeenyJson;
import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Request;
import net.jonathangiles.tools.teenyhttpd.model.Response;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.winter.annot.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.Function;

/**
 * This class is responsible for handling the invocation of the method annotated with one of the HTTP method annotations
 */
final class EndpointHandler implements Function<Request, Response> {

    private final Method target;
    private final Object parent;
    private final Parameter[] parameters;

    EndpointHandler(final Method target, final Object parent) {
        this.target = target;
        this.parent = parent;
        this.parameters = target.getParameters();
    }


    private Object[] buildArguments(Request request) {
        if (parameters.length == 0) return null;

        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(PathVariable.class)) {
                String param = request.getPathParams().get(parameter.getAnnotation(PathVariable.class).value());

                if (param == null || param.isEmpty()) {
                    throw new IllegalArgumentException("Path parameter " + parameter.getAnnotation(PathVariable.class).value() + " is required");
                }

                args[i] = parse(param, parameter.getType());
            }

            if (parameter.isAnnotationPresent(QueryParam.class)) {

                QueryParam queryParam = parameter.getAnnotation(QueryParam.class);

                String param = request.getQueryParams().get(queryParam.value());

                if (param == null) {

                    if (queryParam.defaultValue().isEmpty() && queryParam.required()) {
                        throw new IllegalArgumentException("Query parameter " + parameter.getAnnotation(QueryParam.class).value() + " is required");
                    }

                    param = queryParam.defaultValue();
                }

                args[i] = parse(param, parameter.getType());
            }

            if (parameter.isAnnotationPresent(RequestBody.class)) {
                System.out.println("Not supported yet!");
                return null;
            }
        }

        return args;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object parse(String source, Class<?> type) {

        if (type == String.class) return source;

        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, source);

        if (type.isPrimitive() && source.isEmpty()) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == float.class) return 0.0f;
            if (type == boolean.class) return false;
        }

        if (type == Integer.class || type == int.class) return Integer.parseInt(source);
        if (type == Long.class || type == long.class) return Long.parseLong(source);
        if (type == Double.class || type == double.class) return Double.parseDouble(source);
        if (type == Float.class || type == float.class) return Float.parseFloat(source);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(source);

        return source;
    }


    private String getContentType() {
        if (target.isAnnotationPresent(Get.class)) {
            return target.getAnnotation(Get.class)
                    .produces();
        }

        if (target.isAnnotationPresent(Post.class)) {
            return target.getAnnotation(Post.class)
                    .produces();
        }

        if (target.isAnnotationPresent(Delete.class)) {
            return target.getAnnotation(Delete.class)
                    .produces();
        }

        if (target.isAnnotationPresent(Put.class)) {
            return target.getAnnotation(Put.class)
                    .produces();
        }

        if (target.isAnnotationPresent(Patch.class)) {
            return target.getAnnotation(Patch.class)
                    .produces();
        }

        return "application/json";
    }

    @Override
    public Response apply(Request request) {

        try {

            Object result = invoke(buildArguments(request));

            if (result == null) {
                return new EmptyResponse(StatusCode.OK);
            }

            if (result instanceof Response) {
                return (Response) result;
            }

            if (result instanceof String) {
                return new StringResponse(StatusCode.OK, (String) result);
            }

            if (result instanceof ResponseEntity<?>) {
                return convert((ResponseEntity<?>) result);
            }

            return new StringResponse(StatusCode.OK, List.of(new Header("Content-Type", getContentType())), TeenyJson.write(result));

        } catch (Exception ex) {
            return new StringResponse(StatusCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

    }


    private Object invoke(Object... args) {

        if (Void.class.isAssignableFrom(target.getReturnType())) {
            return null;
        }

        try {
            return target.invoke(parent, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private Response convert(ResponseEntity<?> response) {

        List<Header> headers = response.getHeaders();

        if (headers == null) {
            headers = List.of(new Header("Content-Type", getContentType()));
        }

        return new StringResponse(response.getStatus(), headers, TeenyJson.write(response.getBody()));
    }
}
