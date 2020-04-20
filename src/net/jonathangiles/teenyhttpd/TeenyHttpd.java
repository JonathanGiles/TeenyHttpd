package net.jonathangiles.teenyhttpd;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeenyHttpd {

    static final File WEB_ROOT = new File("./wwwroot");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final int port;
    private boolean isRunning = false;

    public TeenyHttpd(int port) {
        this.port = port;
    }

    public void start() {
        isRunning = true;
        executorService = Executors.newSingleThreadExecutor();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (isRunning) {
                final Socket connect = serverSocket.accept();
                executorService.execute(() -> run(connect));
            }
        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
        executorService.shutdown();
    }

    private void run(final Socket connect) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
             PrintWriter out = new PrintWriter(connect.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            // get first line of the request from the client
            final String input = in.readLine();
            if (input == null) {
                return;
            }
            System.out.println(input);

            // we parse the request with a string tokenizer
            final StringTokenizer parse = new StringTokenizer(input);
            final String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client

            // we get file requested
            String path = parse.nextToken().toLowerCase();

            Request request = new Request(method, path);
            Response response = serve(request);

            if (response != null) {
                response.send(out, dataOut);
            }
        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Response serve(Request request) {
        return new FileResponse(request);
    }
}