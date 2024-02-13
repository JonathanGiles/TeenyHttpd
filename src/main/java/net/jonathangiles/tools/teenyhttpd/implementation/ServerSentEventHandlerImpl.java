package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.model.Request;
import net.jonathangiles.tools.teenyhttpd.model.ServerSentEventHandler;
import net.jonathangiles.tools.teenyhttpd.model.ServerSentEventMessage;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public abstract class ServerSentEventHandlerImpl implements ServerSentEventHandler {
    private boolean isActive = false;

    private final List<ServerSentEventRequest> clients = new CopyOnWriteArrayList<>();

    /** {@inheritDoc} */
    @Override public final void onConnect(Request request) {
        if (!(request instanceof ServerSentEventRequest)) {
            throw new IllegalArgumentException("Request must be an instance of ServerSentEventRequest");
        }
        clients.add((ServerSentEventRequest) request);
        checkState();
    }

    /** {@inheritDoc} */
    @Override public final void onDisconnect(Request request) {
        if (!(request instanceof ServerSentEventRequest)) {
            throw new IllegalArgumentException("Request must be an instance of ServerSentEventRequest");
        }
        clients.remove((ServerSentEventRequest) request);
        checkState();
    }

    /** {@inheritDoc} */
    @Override public boolean hasActiveConnections() {
        return isActive;
    }

    /** {@inheritDoc} */
    @Override public final void sendMessage(final Function<Request, ServerSentEventMessage> messageGenerator) {
        if (!hasActiveConnections()) {
            return;
        }

        clients.forEach(client -> {
            PrintWriter writer = client.getWriter();
            if (writer != null) {
                if (writer.checkError()) {
                    // an error here means that the client has disconnected - so we should perform a disconnection
                    onDisconnect(client);
                    return;
                }
                writer.write(messageGenerator.apply(client).toString());
                writer.write("\n\n");
                writer.flush();
            }
        });
    }

    private void checkState() {
        if (!isActive && !clients.isEmpty()) {
            isActive = true;
            onActive();
        } else if (isActive && clients.isEmpty()) {
            isActive = false;
            onInactive();
        }
    }
}
