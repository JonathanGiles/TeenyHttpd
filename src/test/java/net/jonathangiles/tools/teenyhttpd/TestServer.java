package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.model.ServerSentEventHandler;
import net.jonathangiles.tools.teenyhttpd.model.ServerSentEventMessage;
import net.jonathangiles.tools.teenyhttpd.model.Response;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;

import java.util.concurrent.atomic.AtomicInteger;

public class TestServer {

    public static void main(String[] args) {
        final int PORT = 80;

        TeenyHttpd server = new TeenyHttpd(PORT);
//        server.addStringRoute("/", request -> "Hello world!");
//        server.addStringRoute("/goodbye", request -> "Goodbye world!");
//        server.addFileRoute("/file", new File("E:\\code\\projects\\TeenyHttpd\\src\\main\\resources\\webroot"));

        server.addGetRoute("/user/:id/details", request -> {
            String id = request.getPathParams().get("id");
            return Response.create(StatusCode.OK, "User ID: " + id);
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

        final ServerSentEventHandler sse = ServerSentEventHandler.create((ServerSentEventHandler _sse) -> {
            System.out.println("SSE active - sending messages to client(s)");

            // start a thread and send messages to the client(s)
            new Thread(() -> {
                // all clients share the same integer value, but they get a custom message based
                // on the path parameter for :username
                AtomicInteger i = new AtomicInteger(0);

                while (_sse.hasActiveConnections()) {
                    _sse.sendMessage(client -> {
                        String username = client.getPathParams().get("username");
                        return new ServerSentEventMessage("Hello " + username + " - " + i, "counter");
                    });
                    i.incrementAndGet();
//                        sendMessage(new ServerSentEventMessage("Message " + i++, "counter"));
                    System.out.println(i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        });
        server.addServerSentEventRoute("/sse/:username", sse);

        server.addServerSentEventRoute("/events", ServerSentEventHandler.create(simpleSse -> new Thread(() -> {
            int i = 0;
            while (simpleSse.hasActiveConnections()) {
                simpleSse.sendMessage(new ServerSentEventMessage("Message " + i++, "counter"));
                threadSleep(1000);
            }
        }).start()));

//        server.addFileRoute("/");

        server.start();
    }

    private static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
