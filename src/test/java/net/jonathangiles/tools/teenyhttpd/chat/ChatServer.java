package net.jonathangiles.tools.teenyhttpd.chat;

import net.jonathangiles.tools.teenyhttpd.TeenyHttpd;
import net.jonathangiles.tools.teenyhttpd.model.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Start this app then browse to http://localhost to see the chat app.
 */
public class ChatServer {
    // collection of all connected users
    private final Set<String> users = Collections.synchronizedSet(new HashSet<>());

    // event handler for sending chat messages to all clients
    private final ServerSentEventHandler chatMessagesEventHandler = ServerSentEventHandler.create();

    // event handler for sending all connected users to all clients
    private final ServerSentEventHandler usersEventHandler = ServerSentEventHandler.create();

    public static void main(String[] args) {
        new ChatServer().start();
    }

    private void start() {
        final int PORT = 80;

        TeenyHttpd server = new TeenyHttpd(PORT);

        // post a JSON message to this endpoint to send a message to all clients.
        // JSON format is {"user":"<username>", "message":"<message>"}
        server.addRoute(Method.POST, "/message", request -> {
            String json = request.getQueryParams().get("message");
            if (json != null && !json.isEmpty()) {
                sendMessage(json);
            }
            return StatusCode.OK.asResponse();
        });

        // Post a username to this endpoint to join the chat and broadcast you are connected to all other users
        server.addRoute(Method.POST, "/login", request -> {
            String username = request.getQueryParams().get("username");
            if (username != null && !username.isEmpty()) {
                users.add(username);
                usersEventHandler.sendMessage(String.join(",", users));
                sendSystemMessage(username + " has joined the chat."); // Send system message
            }
            return StatusCode.OK.asResponse();
        });

        // post a username to this endpoint to leave the chat and broadcast you are disconnected to all other users
        server.addRoute(Method.POST, "/logout", request -> {
            String username = request.getQueryParams().get("username");
            if (username != null && !username.isEmpty()) {
                users.remove(username);
                usersEventHandler.sendMessage(String.join(",", users));
                sendSystemMessage(username + " has left the chat."); // Send system message
            }
            return StatusCode.OK.asResponse();
        });

        // The two SSE endpoints, for messages and connected users
        server.addServerSentEventRoute("/messages", chatMessagesEventHandler);
        server.addServerSentEventRoute("/users", usersEventHandler);

        // we serve the web page from here
        server.addFileRoute("/", new File("src/test/java/net/jonathangiles/tools/teenyhttpd/chat"));

        server.start();
    }

    private void sendSystemMessage(String message) {
        sendMessage("System", message);
    }

    private void sendMessage(String user, String message) {
        String jsonString = String.format("{\"user\":\"%s\", \"message\":\"%s\"}", user, message);
        sendMessage(jsonString);
    }

    private void sendMessage(String json) {
        chatMessagesEventHandler.sendMessage(json);
    }
}
