package net.jonathangiles.tools.teenyhttpd.model;

import net.jonathangiles.tools.teenyhttpd.implementation.SimpleRequest;

import java.util.*;

/**
 * Represents an incoming request.
 */
public interface Request {

    static Request create(final Method method, final String path, final QueryParams queryParams) {
        return new SimpleRequest(method, path, queryParams);
    }

    static Request create(Method method, String path, QueryParams queryParams, List<Header> headers, Map<String,
            String> pathParamsMap, String body) {
        return new SimpleRequest(method, path, queryParams, headers, pathParamsMap, body);
    }

    static Request create(Method method, String path, QueryParams queryParams, Map<String, Header> headers, Map<String,
            String> pathParamsMap) {
        return new SimpleRequest(method, path, queryParams, headers, pathParamsMap);
    }

    Method getMethod();

    String getPath();

    String getBody();

    /**
     * Returns a read-only Map of headers.
     * @return
     */
    Map<String, Header> getHeaders();

    Optional<Header> getHeader(final String header);

    Optional<Header> getHeader(final Headers header);

    Map<String, String> getQueryParams();

    Map<String, String> getPathParams();
}
