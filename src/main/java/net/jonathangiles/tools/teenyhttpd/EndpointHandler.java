package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.implementation.DefaultMessageConverter;
import net.jonathangiles.tools.teenyhttpd.implementation.EmptyResponse;
import net.jonathangiles.tools.teenyhttpd.implementation.StringResponse;
import net.jonathangiles.tools.teenyhttpd.model.*;
import net.jonathangiles.tools.teenyhttpd.implementation.ResponseEntity;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * This class is responsible for handling the invocation of the method annotated with one of the HTTP method annotations
 */
final class EndpointHandler implements Function<Request, Response> {

    private final Method target;
    private final Object parent;
    private final Parameter[] parameters;
    private final MessageConverter messageConverter;
    private String contentType;

    EndpointHandler(final Method target, final Object parent, Map<String, MessageConverter> converterMap) {
        this.target = target;
        this.parent = parent;
        this.parameters = target.getParameters();
        this.messageConverter = converterMap.getOrDefault(getContentType(), DefaultMessageConverter.INSTANCE);
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

        if (type == String.class) {
            try {
                return URLDecoder.decode(source, StandardCharsets.UTF_8.toString());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid URL encoding");
            }
        }

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

        if (contentType != null) return contentType;

        if (target.isAnnotationPresent(Get.class)) {
            contentType = target.getAnnotation(Get.class)
                    .produces();

            return contentType;
        }

        if (target.isAnnotationPresent(Post.class)) {
            contentType = target.getAnnotation(Post.class)
                    .produces();

            return contentType;
        }

        if (target.isAnnotationPresent(Delete.class)) {
            contentType = target.getAnnotation(Delete.class)
                    .produces();

            return contentType;
        }

        if (target.isAnnotationPresent(Put.class)) {
            contentType = target.getAnnotation(Put.class)
                    .produces();

            return contentType;
        }

        if (target.isAnnotationPresent(Patch.class)) {
            contentType = target.getAnnotation(Patch.class)
                    .produces();

            return contentType;
        }

        contentType = "application/json";

        return contentType;
    }

    @Override
    public Response apply(Request request) {

        try {

            Object result = invoke(buildArguments(request));

            if (result == null) {
                return new EmptyResponse(StatusCode.OK);
            }

            if (result instanceof ResponseEntity<?>) {
                return convert((ResponseEntity<?>) result);
            }

            if (result instanceof Response) {
                return (Response) result;
            }

            if (result instanceof String) {
                return new StringResponse(StatusCode.OK, (String) result);
            }

            if (result instanceof StatusCode) {
                return new EmptyResponse((StatusCode) result);
            }

            return convert(ResponseEntity.ok(result));
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

        if (response.getHeaders() == null) {
            response.setHeader(new Header("Content-Type", messageConverter.getContentType()));
        }

        return new ResponseEntityDecorator(response, messageConverter);
    }

    private static class ResponseEntityDecorator implements Response {
        private final ResponseEntity<?> response;
        private final MessageConverter messageConverter;

        public ResponseEntityDecorator(ResponseEntity<?> response, MessageConverter messageConverter) {
            this.response = response;
            this.messageConverter = messageConverter;
        }

        @Override
        public StatusCode getStatusCode() {
            return response.getStatusCode();
        }

        @Override
        public void setHeader(Header header) {
            response.setHeader(header);
        }

        @Override
        public void writeBody(BufferedOutputStream dataOut) throws IOException {
            messageConverter.write(response.getBody(), dataOut);
        }
    }
}
