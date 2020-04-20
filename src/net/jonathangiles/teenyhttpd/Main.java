package net.jonathangiles.teenyhttpd;

import net.jonathangiles.teenyhttpd.response.Response;
import net.jonathangiles.teenyhttpd.response.ByteResponse;
import net.jonathangiles.teenyhttpd.response.StringResponse;

public class Main {

    public static void main(String[] args) {
        final int PORT = 8182;

        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
        TeenyHttpd server = new TeenyHttpd(PORT) {
            @Override
            public Response serve(final Request request) {
//                return new ByteResponse(request, StatusCode.OK, "Hello world!".getBytes());
                return new StringResponse(request, StatusCode.OK, "Hello!");
            }
        };
        server.start();
    }
}
