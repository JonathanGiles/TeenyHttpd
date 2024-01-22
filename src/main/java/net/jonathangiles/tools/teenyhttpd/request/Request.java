package net.jonathangiles.tools.teenyhttpd.request;

import java.util.*;

/**
 * Represents an incoming request.
 */
public class Request {
    private final Method method;
    private final String path;
    private final QueryParams queryParams;
    private List<Header> headers;
    private Map<String, String> headersMap;

    private Map<String, String> pathParams = new HashMap<>(); // FIXME: this is a hack

    public Request(final Method method, final String path, final QueryParams queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void addHeader(final Header header) {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        headers.add(header);
    }

    public Map<String, String> getHeaders() {
        if (headersMap == null) {
            headersMap = new LinkedHashMap<>();
            headers.forEach(header -> {
                headersMap.put(header.getKey(), header.getValue());
            });
        }
        return headersMap;
    }

    public Map<String, String> getQueryParams() {
        return queryParams.getQueryParams();
    }

    @Override
    public String toString() {
        return "Request{" +
                       "method=" + method +
                       ", path='" + path + '\'' +
                       ", queryParams=" + queryParams +
                       ", headers=" + headers +
                       '}';
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public void addPathParam(String name, String value) {
        pathParams.put(name, value);
    }
}
