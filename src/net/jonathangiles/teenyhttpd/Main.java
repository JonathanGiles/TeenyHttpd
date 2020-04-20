package net.jonathangiles.teenyhttpd;

public class Main {

    public static void main(String[] args) {
        final int PORT = 8182;

        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
        TeenyHttpd server = new TeenyHttpd(PORT);
        server.start();
    }
}
