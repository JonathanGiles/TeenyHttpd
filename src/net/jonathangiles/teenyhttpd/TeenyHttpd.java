package net.jonathangiles.teenyhttpd;

import net.jonathangiles.teenyhttpd.request.Method;
import net.jonathangiles.teenyhttpd.request.QueryParams;
import net.jonathangiles.teenyhttpd.request.Request;
import net.jonathangiles.teenyhttpd.response.FileResponse;
import net.jonathangiles.teenyhttpd.response.Response;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class TeenyHttpd {

    private final int port;
    private final Supplier<? extends ExecutorService> executorSupplier;

    private ExecutorService executorService;
    private boolean isRunning = false;

    public TeenyHttpd(final int port) {
        this(port, Executors::newSingleThreadExecutor);
    }

    public TeenyHttpd(final int port, final Supplier<? extends ExecutorService> executorSupplier) {
        this.port = port;
        this.executorSupplier = executorSupplier;
    }

    public void start() {
        isRunning = true;
        executorService = executorSupplier.get();

        try (final ServerSocket serverSocket = new ServerSocket(port)) {
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

    public Response serve(Request request) {
        return new FileResponse(request);
    }

    private void run(final Socket connect) {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
             final PrintWriter out = new PrintWriter(connect.getOutputStream());
             final BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            // get first line of the request from the client
            final String input = in.readLine();
            if (input == null) {
                return;
            }

            // we parse the request line - https://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
            // For now we do not care about the HTTP Version
            final StringTokenizer parse = new StringTokenizer(input);

            // the HTTP Method
            final Method method = Method.valueOf(parse.nextToken().toUpperCase());

            // we get request-uri requested. For now we assume it is an absolute path
            final String requestUri = parse.nextToken().toLowerCase();

            // split it at the query param, if it exists
            Request request;
            if (requestUri.contains("?")) {
                final String[] uriSplit = requestUri.split("\\?", 2);

                // create a lazily-evaluated object to represent the query parameters
                request = new Request(method, uriSplit[0], new QueryParams(uriSplit[1]));
            } else {
                request = new Request(method, requestUri, QueryParams.EMPTY);
            }

            // read (but not parse) all request headers and put them into the request.
            // They will be parsed on-demand.
            String line;
            while (true) {
                line = in.readLine();
                if (line == null || line.isEmpty() || "\r\n".equals(line)) {
                    break;
                }
                request.addHeader(new Header(line));
            }

            final Response response = serve(request);

            if (response != null) {
                // write headers
                out.println(response.getStatusCode().toString());
                out.println("Server: TeenyHttpd from JonathanGiles.net : 1.0");
                out.println("Date: " + LocalDateTime.now());
                response.getHeaders().forEach(out::println);
                out.println(); // empty line between header and body
                out.flush();   // flush character output stream buffer

                // write body
                response.writeBody(dataOut);
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
}