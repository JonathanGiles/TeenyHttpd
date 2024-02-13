package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Headers;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.model.Request;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;

public class ServerSentEventRequest implements Request, Closeable {
    private final Request request;
    private final PrintWriter out;

    public ServerSentEventRequest(final Request request, final Socket clientSocket) throws IOException {
        this.request = request;
        this.out = new PrintWriter(clientSocket.getOutputStream());
    }

    public PrintWriter getWriter() {
        return out;
    }

    @Override
    public void close() {
        out.close();
    }

    @Override
    public Method getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPath() {
        return request.getPath();
    }

//    @Override
//    public void addHeader(Header header) {
//        request.addHeader(header);
//    }

    @Override
    public Map<String, Header> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public Optional<Header> getHeader(String header) {
        return request.getHeader(header);
    }

    @Override
    public Optional<Header> getHeader(Headers header) {
        return request.getHeader(header);
    }

    @Override
    public Map<String, String> getQueryParams() {
        return request.getQueryParams();
    }

    @Override
    public Map<String, String> getPathParams() {
        return request.getPathParams();
    }

//    @Override
//    public void addPathParam(String name, String value) {
//        request.addPathParam(name, value);
//    }
}
