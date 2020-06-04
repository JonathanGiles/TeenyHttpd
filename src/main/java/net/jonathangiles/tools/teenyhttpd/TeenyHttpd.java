package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.request.Header;
import net.jonathangiles.tools.teenyhttpd.request.Method;
import net.jonathangiles.tools.teenyhttpd.request.QueryParams;
import net.jonathangiles.tools.teenyhttpd.request.Request;
import net.jonathangiles.tools.teenyhttpd.response.FileResponse;
import net.jonathangiles.tools.teenyhttpd.response.Response;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
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

/**
 * The TeenyHttpd server itself - instantiating an instance of this class and calling 'start()' is all that is required
 * to begin serving requests.
 */
public class TeenyHttpd {

    private final int port;
    private final Supplier<? extends ExecutorService> executorSupplier;

    private ExecutorService executorService;
    private boolean isRunning = false;

    private File webroot;

    /**
     * Creates a single-threaded server that will work on the given port, although the server does not start until
     * 'stort()' is called.
     *
     * @param port The port for the server to listen to.
     */
    public TeenyHttpd(final int port) {
        this(port, Executors::newSingleThreadExecutor);
    }

    /**
     * Creates a server that will work on the given port, although the server does not start until 'stort()' is called.
     * The executor supplier enables creating {@link ExecutorService} instances that can handle requests with a range
     * of different threading models.
     *
     * @param port The port for the server to listen to.
     * @param executorSupplier A {@link ExecutorService} instances that can handle requests with a range
     *      of different threading models.
     */
    public TeenyHttpd(final int port, final Supplier<? extends ExecutorService> executorSupplier) {
        this.port = port;
        this.executorSupplier = executorSupplier;
    }

    /**
     * Sets the root directory to look for requested files.
     * @param webroot A path on the local file system for serving requested files from.
     */
    public void setWebroot(final File webroot) {
        this.webroot = webroot;
    }

    /**
     * Starts the server instance.
     */
    public void start() {
        System.out.println("TeenyHttp server started.\nListening for connections on port : " + port + " ...\n");
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

    /**
     * Requests that the server instance stop serving requests.
     */
    public void stop() {
        isRunning = false;
        executorService.shutdown();
    }

    /**
     * This method is called on every request, and allows for responses to be generated as appropriate.
     *
     * @param request The incoming request that must be responded to.
     * @return The response that will be given to the requestor.
     */
    public Response serve(final Request request) {
        return new FileResponse(request) {
            @Override protected File getFile(final String filename) {
                return new File(webroot, filename);
            }
        };
    }

    private void run(final Socket connect) {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()))) {
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
            final Request request;
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

            try (final PrintWriter out = new PrintWriter(connect.getOutputStream());
                 final BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

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
                System.err.println("Server error when trying to serve request: " + request);
                System.err.println("Server error : " + ioe);
            }
        } catch (IOException e) {
            System.err.println("Server error 2 : " + e);
        } finally {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}