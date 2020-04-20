package net.jonathangiles.teenyhttpd;

public class EmptyResponse extends SimpleResponse {

    public EmptyResponse(final Request request) {
        super(request, null, null);
    }
}
