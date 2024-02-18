package net.jonathangiles.tools.teenyhttpd.chat;

import com.google.gson.Gson;
import net.jonathangiles.tools.teenyhttpd.GsonMessageConverter;
import net.jonathangiles.tools.teenyhttpd.Message;
import net.jonathangiles.tools.teenyhttpd.TeenyApplication;
import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.model.ServerSentEventHandler;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatServerButUsingAnnotations {


    public static void main(String[] args) {
        System.setProperty("server.port", "80");
        TeenyApplication.start(ChatServerButUsingAnnotations.class);
    }

    // collection of all connected users
    private final Set<String> users = Collections.synchronizedSet(new HashSet<>());

    private final Gson gson = new Gson();

    @Configuration
    public GsonMessageConverter getGsonConverter() {
        return new GsonMessageConverter();
    }

    @ServerEvent(value = "/messages", name = "messages")
    public ServerSentEventHandler chatMessages() {
        return ServerSentEventHandler.create();
    }

    @ServerEvent("/users")
    public ServerSentEventHandler users() {
        return ServerSentEventHandler.create();
    }

    @Get
    public File getWebRoot() {
        return new File("src/test/java/net/jonathangiles/tools/teenyhttpd/chat");
    }

    @Post("/message")
    public void message(@QueryParam("message") String message,
                        @EventHandler("messages") ServerSentEventHandler chatMessagesEventHandler) {

        if (message.isEmpty()) {
            return;
        }

        chatMessagesEventHandler.sendMessage(message);
    }

    @Post("/login")
    public void login(@QueryParam("username") String username,
                      @EventHandler("users") ServerSentEventHandler usersEventHandler,
                      @EventHandler("messages") ServerSentEventHandler chatMessagesEventHandler) {

        if (username.isEmpty()) {
            return;
        }

        users.add(username);
        usersEventHandler.sendMessage(String.join(",", users));
        chatMessagesEventHandler.sendMessage(gson.toJson(new Message("system", username + " has joined the chat")));
    }

    @Post("/logout")
    public void logout(@QueryParam("username") String username,
                       @EventHandler("users") ServerSentEventHandler usersEventHandler,
                       @EventHandler("messages") ServerSentEventHandler chatMessagesEventHandler) {

        if (username.isEmpty()) {
            return;
        }

        users.remove(username);
        usersEventHandler.sendMessage(String.join(",", users));
        chatMessagesEventHandler.sendMessage(gson.toJson(new Message("system", username + " has left the chat")));
    }

}
