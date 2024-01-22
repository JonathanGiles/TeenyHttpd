package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.response.StatusCode;
import net.jonathangiles.tools.teenyhttpd.response.StringResponse;

import java.io.File;

public class TestServer {

    public static void main(String[] args) {
        final int PORT = 80;

        TeenyHttpd server = new TeenyHttpd(PORT);
//        server.addStringRoute("/", request -> "Hello world!");
//        server.addStringRoute("/goodbye", request -> "Goodbye world!");
//        server.addFileRoute("/file", new File("E:\\code\\projects\\TeenyHttpd\\src\\main\\resources\\webroot"));

        server.addGetRoute("/user/:id/details", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });
//
//        server.addGetRoute("/foo/:bar/:baz", request -> {
//            String bar = request.getPathParams().get("bar");
//            String baz = request.getPathParams().get("baz");
//            return new StringResponse(StatusCode.OK, "bar: " + bar + ", baz: " + baz);
//        });
//
        server.addGetRoute("/QueryParams", request -> {
            request.getQueryParams().forEach((key, value) -> System.out.println(key + " = " + value));
            return StatusCode.OK.asResponse();
        });

//        server.addFileRoute("/");

        server.start();
    }
}
