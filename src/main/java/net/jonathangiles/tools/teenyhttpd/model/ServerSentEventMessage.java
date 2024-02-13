package net.jonathangiles.tools.teenyhttpd.model;

import java.time.Duration;

public class ServerSentEventMessage {
    private final String message;
    private final String event;
    private final String id;
    private final Duration retry;
    private final String comment;

    public ServerSentEventMessage(String message) {
        this.message = message;
        this.event = null;
        this.id = null;
        this.retry = null;
        this.comment = null;
    }

    public ServerSentEventMessage(String message, String event) {
        this.message = message;
        this.event = event;
        this.id = null;
        this.retry = null;
        this.comment = null;
    }

    public ServerSentEventMessage(String message, String event, String id) {
        this.message = message;
        this.event = event;
        this.id = id;
        this.retry = null;
        this.comment = null;
    }

    public ServerSentEventMessage(String message, String event, String id, Duration retry, String comment) {
        this.message = message;
        this.event = event;
        this.id = id;
        this.retry = retry;
        this.comment = comment;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (comment != null) {
            sb.append(": ").append(comment).append("\n");
        }
        if (event != null) {
            sb.append("event: ").append(event).append("\n");
        }
        if (id != null) {
            sb.append("id: ").append(id).append("\n");
        }
        if (message != null) {
            String[] lines = message.split("\n");
            for (String line : lines) {
                sb.append("data: ").append(line).append("\n");
            }
        }
        if (retry != null) {
            sb.append("retry: ").append(retry.toMillis()).append("\n");
        }

        return sb.toString();
    }
}
