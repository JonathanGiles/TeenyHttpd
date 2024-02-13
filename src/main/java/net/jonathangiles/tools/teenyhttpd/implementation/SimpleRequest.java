package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Headers;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.model.QueryParams;
import net.jonathangiles.tools.teenyhttpd.model.Request;

import java.util.*;

/**
 * Represents an incoming request.
 */
public class SimpleRequest implements Request {
    private final Method method;
    private final String path;
    private final QueryParams queryParams;

    // These are 'raw' headers - they are not yet parsed into a map of headers
    private List<Header> headers;
    private Map<String, Header> headersMap;

    private Map<String, String> pathParams; // FIXME: this is a hack

    public SimpleRequest(final Method method, final String path, final QueryParams queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    public SimpleRequest(final Method method,
                   final String path,
                   final QueryParams queryParams,
                   final Map<String, Header> headers) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headersMap = headers;
    }

    public SimpleRequest(Method method, String path, QueryParams queryParams, final Map<String, Header> headers, Map<String, String> pathParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headersMap = headers;
        this.pathParams = Collections.unmodifiableMap(pathParams);
    }

    public SimpleRequest(Method method, String path, QueryParams queryParams, final List<Header> headers,
                         Map<String, String> pathParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.pathParams = Collections.unmodifiableMap(pathParams);
    }

    @Override public Method getMethod() {
        return method;
    }

    @Override public String getPath() {
        return path;
    }

//    public void addHeader(final Header header) {
//        if (headersMap == null) {
//            headersMap = new LinkedHashMap<>();
//        }
//        headersMap.put(header.getKey(), header);
//    }

    /**
     * Returns a read-only Map of headers.
     * @return
     */
    @Override public Map<String, Header> getHeaders() {
        if (headersMap == null) {
            if (headers != null) {
                headersMap = new LinkedHashMap<>();
                headers.forEach(header -> headersMap.put(header.getKey(), header));
                headersMap = Collections.unmodifiableMap(headersMap);
                headers = null;
            } else {
                return Collections.emptyMap();
            }
        }
        return Collections.unmodifiableMap(headersMap);
    }

    @Override public Optional<Header> getHeader(final String header) {
        return Optional.ofNullable(getHeaders().get(header));
    }

    @Override public Optional<Header> getHeader(final Headers header) {
        return Optional.ofNullable(getHeaders().get(header.getKey()));
    }

    @Override public Map<String, String> getQueryParams() {
        return queryParams.getQueryParams();
    }

    @Override
    public String toString() {
        return "Request{" +
                "method=" + method +
                ", path='" + path + '\'' +
                ", queryParams=" + queryParams +
                ", headers=" + headersMap +
                '}';
    }

    @Override public Map<String, String> getPathParams() {
        return pathParams == null ? Collections.emptyMap() : pathParams;
    }

//    @Override public void addPathParam(String name, String value) {
//        pathParams.put(name, value);
//    }
}
