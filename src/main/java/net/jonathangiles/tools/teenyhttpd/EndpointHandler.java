package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.implementation.DefaultMessageConverter;
import net.jonathangiles.tools.teenyhttpd.implementation.EmptyResponse;
import net.jonathangiles.tools.teenyhttpd.implementation.StringResponse;
import net.jonathangiles.tools.teenyhttpd.model.*;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for handling the invocation of the method annotated with one of the HTTP method annotations
 */
final class EndpointHandler implements Function<Request, Response> {

    private final Method target;
    private final Object parent;
    private final Parameter[] parameters;
    private final MessageConverter defaultConverter;
    private final Map<String, MessageConverter> converterMap;
    private String contentType;

    EndpointHandler(final Method target, final Object controller, Map<String, MessageConverter> converterMap) {
        this.target = target;
        this.parent = controller;
        this.parameters = target.getParameters();
        this.converterMap = converterMap;
        this.defaultConverter = converterMap.getOrDefault(getContentType(), DefaultMessageConverter.INSTANCE);

        if (!target.trySetAccessible()) {
            Logger.getLogger(EndpointHandler.class.getName())
                    .info("Could not set method accessible");
        }
    }


    private Object[] buildArguments(Request request) {

        if (parameters.length == 0) return null;

        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(PathParam.class)) {
                String param = request.getPathParams().get(parameter.getAnnotation(PathParam.class).value());

                if (param == null || param.isEmpty()) {
                    throw new IllegalArgumentException("Path parameter " + parameter.getAnnotation(PathParam.class).value() + " is required");
                }

                args[i] = parse(param, parameter.getType());

                continue;
            }

            if (parameter.isAnnotationPresent(QueryParam.class)) {

                QueryParam queryParam = parameter.getAnnotation(QueryParam.class);

                String param = request.getQueryParams().get(queryParam.value());

                if (param == null) {

                    if (queryParam.defaultValue().isEmpty() && queryParam.required()) {
                        throw new BadRequestException("Query parameter " + parameter.getAnnotation(QueryParam.class).value() + " is required");
                    }

                    param = queryParam.defaultValue();
                }

                args[i] = parse(param, parameter.getType());

                continue;
            }

            if (parameter.isAnnotationPresent(RequestHeader.class)) {

                Header header = request.getHeaders()
                        .get(parameter.getAnnotation(RequestHeader.class).value());

                if (Header.class.isAssignableFrom(parameter.getType())) {
                    args[i] = header;
                    continue;
                }

                if (header == null || header.getValues().isEmpty()) {
                    args[i] = parse(null, parameter.getType());
                    continue;
                }

                args[i] = parse(header.getValues().get(0), parameter.getType());

                continue;
            }

            if (Request.class.isAssignableFrom(parameter.getType())) {
                args[i] = request;
            }

            if (parameter.isAnnotationPresent(RequestBody.class)) {

                if (request.getBody() == null) {
                    continue;
                }

                try {
                    args[i] = getMessageConverter(request).read(request.getBody(), parameter.getType());
                } catch (Exception ex) {
                    throw new BadRequestException("Invalid request body");
                }
            }
        }

        return args;
    }

    private static class BadRequestException extends RuntimeException {

        public BadRequestException(String message) {
            super(message);
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object parse(String source, Class<?> type) {

        if (type == String.class) {

            if (source == null) return null;
            if (source.isEmpty()) return "";

            try {
                return URLDecoder.decode(source, StandardCharsets.UTF_8.toString());
            } catch (Exception ex) {
                throw new BadRequestException("Invalid URL encoding");
            }
        }

        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, source);

        if (type.isPrimitive() && (source == null || source.isEmpty())) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == float.class) return 0.0f;
            if (type == boolean.class) return false;

            throw new IllegalArgumentException("Unable to parse primitive type");
        }

        try {
            if (type == Integer.class || type == int.class) return Integer.parseInt(source);
            if (type == Long.class || type == long.class) return Long.parseLong(source);

            if (type == Double.class || type == double.class) return Double.parseDouble(source);
            if (type == Float.class || type == float.class) return Float.parseFloat(source);
            if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(source);

        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid number value '" + source + "' ");
        }

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

    private MessageConverter getMessageConverter(Request request) {

        Optional<Header> opHeader = request.getHeader("Accept");

        if (opHeader.isPresent()) {
            return converterMap.getOrDefault(opHeader.get().getValues().get(0), defaultConverter);
        }

        return defaultConverter;
    }

    @Override
    public Response apply(Request request) {

        try {

            MessageConverter converter = getMessageConverter(request);

            Object result = invoke(buildArguments(request));

            if (result == null) {
                return new EmptyResponse(StatusCode.OK);
            }

            if (result instanceof TypedResponse<?>) {
                return convert((TypedResponse<?>) result, converter);
            }

            if (result instanceof Response) {
                return (Response) result;
            }

            if (result instanceof String) {
                return new StringResponse(StatusCode.OK, getHeaders(converter), (String) result);
            }

            if (result instanceof StatusCode) {
                return new EmptyResponse((StatusCode) result);
            }

            return convert(TypedResponse.ok(result), converter);
        } catch (BadRequestException ex) {
            return new StringResponse(StatusCode.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {

            Logger.getLogger(EndpointHandler.class.getName())
                    .log(Level.SEVERE, null, ex);

            return new StringResponse(StatusCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

    }


    private List<Header> getHeaders(MessageConverter converter) {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", converter.getContentType()));

        return headers;
    }

    private Object invoke(Object[] args) {
        try {
            return target.invoke(parent, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private Response convert(TypedResponse<?> response, MessageConverter converter) {

        if (response.getBody() == null) {
            return new EmptyResponse(response.getStatusCode());
        }

        if (response.getHeaders() == null || response.getHeaders().isEmpty()) {
            response.setHeader(new Header("Content-Type", converter.getContentType()));
        }

        return new TypedResponseDecorator(response, converter);
    }

    private static class TypedResponseDecorator implements Response {
        private final TypedResponse<?> response;
        private final MessageConverter messageConverter;

        public TypedResponseDecorator(TypedResponse<?> response, MessageConverter messageConverter) {
            this.response = response;
            this.messageConverter = messageConverter;
        }

        @Override
        public List<Header> getHeaders() {
            return response.getHeaders();
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
            dataOut.flush();
        }
    }
}
