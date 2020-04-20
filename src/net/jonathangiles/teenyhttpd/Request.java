package net.jonathangiles.teenyhttpd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Request {
    private final Method method;
    private final String path;
    private final QueryParams queryParams;
    private List<Header> headers;

    public Request(final Method method, final String path, QueryParams queryParams) {
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

    public void addHeader(Header header) {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        headers.add(header);
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
}
