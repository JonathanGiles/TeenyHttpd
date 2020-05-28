package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.request.Request;
import net.jonathangiles.tools.teenyhttpd.response.Response;
import net.jonathangiles.tools.teenyhttpd.response.StatusCode;
import net.jonathangiles.tools.teenyhttpd.response.StringResponse;

import java.io.File;

public class TestServer {

    public static void main(String[] args) {
        final int PORT = 80;

        TeenyHttpd server = new TeenyHttpd(PORT);
        server.setWebroot(new File("/Users/jonathan/Code/jonathangiles.net/output"));
        server.start();
    }
}
