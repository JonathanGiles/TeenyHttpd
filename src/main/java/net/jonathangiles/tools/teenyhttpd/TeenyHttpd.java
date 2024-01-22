package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.request.Header;
import net.jonathangiles.tools.teenyhttpd.request.Method;
import net.jonathangiles.tools.teenyhttpd.request.QueryParams;
import net.jonathangiles.tools.teenyhttpd.request.Request;
import net.jonathangiles.tools.teenyhttpd.response.FileResponse;
import net.jonathangiles.tools.teenyhttpd.response.Response;
import net.jonathangiles.tools.teenyhttpd.response.StatusCode;
import net.jonathangiles.tools.teenyhttpd.response.StringResponse;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TeenyHttpd server itself - instantiating an instance of this class and calling 'start()' is all that is required
 * to begin serving requests.
 */
public class TeenyHttpd {

    public static final File DEFAULT_WEB_ROOT = new File(Thread.currentThread().getContextClassLoader().getResource("webroot").getFile());

    private final int port;
    private final Supplier<? extends ExecutorService> executorSupplier;

    private ExecutorService executorService;

    private ServerSocket serverSocket;

    private boolean isRunning = false;

    private final Map<Method, Map<RequestPath, Function<Request, Response>>> routes = new HashMap<>();


    /**
     * Creates a single-threaded server that will work on the given port, although the server does not start until
     * 'stort()' is called.
     *
     * @param port The port for the server to listen to.
     */
    public TeenyHttpd(final int port) {
        this(port, Executors::newCachedThreadPool);
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

    public void addGetRoute(final String path, final Function<Request, Response> handler) {
        addRoute(Method.GET, path, handler);
    }

    public void addRoute(final Method method, final String path, final Function<Request, Response> handler) {
        _addRoute(method, path, handler);
    }

    public void addStringRoute(final String path, final Function<Request, String> handler) {
        _addRoute(Method.GET, path, request -> new StringResponse(handler.apply(request)));
    }

    public void addFileRoute(String path) {
        addFileRoute(path, DEFAULT_WEB_ROOT);
    }

    public void addFileRoute(String path, final File webroot) {
        if (path == null) {
            path = "/";
        }

        if (!path.endsWith("/")) {
            path += "/";
        }

        // change the path so that all text beyond the given path is treated as a regex named parameter called filePath
        _addRoute(Method.GET, path + "(?<filePath>.*)", request -> new FileResponse(request) {
            @Override protected File getFile(final String filename) {
                return new File(webroot, filename);
            }
        });
    }

    private void _addRoute(final Method method, final String path, final Function<Request, Response> handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(new RequestPath(path), handler);
    }

    /**
     * Starts the server instance.
     */
    public void start() {
        Thread serverThread = new Thread(this::startServer);
        serverThread.start();
    }

    private void startServer() {
        System.out.println("TeenyHttp server started.\nListening for connections on port : " + port);
        isRunning = true;
        executorService = executorSupplier.get();

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            while (isRunning) {
                final Socket clientSocket = serverSocket.accept();
                executorService.execute(() -> handleIncomingRequest(clientSocket));
            }
        } catch (SocketException e) {
            if (e.getMessage().contains("Socket closed")) {
                // TODO handle closed socket exception
            } else {
                // handle other socket exception
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

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        executorService.close();
//        try {
//            executorService.awaitTermination(1, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        System.out.println("TeenyHttp server stopped.");
    }

    private void handleIncomingRequest(final Socket clientSocket) {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
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

            // Get the map for the method from the incoming request
            Map<RequestPath, Function<Request, Response>> methodRoutes = routes.get(method);

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

            if (methodRoutes == null) {
                // we do not support this route on the given method, but we don't know if we support it on any other
                // methods. We need to check if we support it on any other methods, and if so, we need to return a
                // 405. If we don't support it on any other methods, we need to return a 404.
                boolean isSupportedOnOtherMethods = routes.values().stream()
                        .flatMap(m -> m.keySet().stream())
                        .anyMatch(p -> p.getPath().equals(request.getPath()));

                if (isSupportedOnOtherMethods) {
                    // we support this path on at least one other method, so we return a 405
                    sendStatusCode(clientSocket, StatusCode.METHOD_NOT_ALLOWED);
                } else {
                    // we don't support this path on any method, so we return a 404
                    sendStatusCode(clientSocket, StatusCode.NOT_FOUND);
                }
                return;
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

            // the request path is a full path, which may include path params within the path (e.g. ':id'), or extra path
            // information that comes after the root path (e.g. the root path may be '/', but we the path may be '/index.html').
            // We need to determine the best route to call based on the given full path, and then pass the request to that route.
            Optional<Map.Entry<RequestPath, Function<Request, Response>>> route = methodRoutes.entrySet().stream()
                .filter(entry -> {
                    // compare the regex path to the request path, and check if they match
                    return entry.getKey().getRegex().matcher(request.getPath()).matches();
                }).findFirst();

            final Response response;
            if (route.isPresent()) {
                // we have a route, so we call it, but first we need to parse the path params and set them in the
                // request
                final RequestPath requestPath = route.get().getKey();
                final Matcher matcher = requestPath.getRegex().matcher(request.getPath());
                if (matcher.matches()) {
                    // we have a match, so we need to parse the path params and set them in the request
                    final List<String> pathParams = requestPath.getPathParams();
                    for (int i = 0; i < pathParams.size(); i++) {
                        request.addPathParam(pathParams.get(i), URLDecoder.decode(matcher.group(i + 1), "UTF-8"));
                    }
                }
                response = route.get().getValue().apply(request);
            } else {
                response = StatusCode.NOT_FOUND.asResponse();
            }

            sendResponse(clientSocket, response);
        } catch (IOException e) {
            System.err.println("Server error 2 : " + e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendStatusCode(Socket clientSocket, StatusCode statusCode) {
        sendResponse(clientSocket, statusCode, null);
    }

    private void sendResponse(Socket clientSocket, Response response) {
        sendResponse(clientSocket, null, response);
    }

    private void sendResponse(Socket clientSocket, StatusCode statusCode, Response response) {
        try (final PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
             final BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())) {

            // write headers
            out.println((statusCode == null ? response.getStatusCode() : statusCode).toString());
            out.println("Server: TeenyHttpd from JonathanGiles.net : 1.0");
            out.println("Date: " + LocalDateTime.now());

            if (response != null) {
                // FIXME shouldn't add two lots of Content-Length here
                response.getHeaders().forEach(out::println);
                out.println("Content-Length: " + response.getBodyLength());
            }

            out.println(); // empty line between header and body
            out.flush();   // flush character output stream buffer

            if (response != null) {
                // write body
                response.writeBody(dataOut);
                dataOut.flush(); // flush binary output stream buffer
            }
        } catch (IOException ioe) {
            System.err.println("Server error when trying to serve request");
            System.err.println("Server error : " + ioe);
        }
    }

    private static class RequestPath {
        private final String path;
        private final Pattern regexPattern;
        private final List<String> pathParams = new ArrayList<>();

        RequestPath(final String path) {
            this.path = path;

            String regexString = path;
            if (path.contains(":")) {
                // we have one or more path params, so we need to create a regex string that will match the given path
                // and extract the path params into uniquely-named groups, where the group name should be the value after
                // the colon in the path param.
                // For example:
                //  If the path is '/user/:id', then the regex string should be '/user/(?<id>[^/]+)'.
                //  If the path is '/user/:id/:name', then the regex string should be '/user/(?<id>[^/]+)/(?<name>[^/]+)'.
                // To do this we need to extract the path params, and then replace them with the regex string.
                // We also need to ensure that the path params are not greedy, so we use '[^/]+', which means 'match
                // everything except a forward slash'.
                // We also need to ensure that the regex string is anchored to the start and end of the path, so we
                // add '^' and '$' to the start and end of the regex string.
                // Finally, we need to ensure that the regex string is case-insensitive, so we add '(?i)' to the start
                // of the regex string.
                // The most important thing is that the param name is not 'param', but the name of the path param.
                // This is because we will use the param name to extract the value from the path param, and we need
                // to know the name of the path param to do this.
                regexString = replaceTokens(path, Pattern.compile(":[^/]*+"), matcher -> {
                    final String paramName = matcher.group().substring(1);
                    return "(?<" + paramName + ">[^/]*+)";
                });
            }
            this.regexPattern = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);

            // get all named regex groups in the given path and put them in the pathParams list


            Matcher matcher = regexPattern.matcher(path);
            if (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    pathParams.add(matcher.group(i).substring(1)); // substring to strip off the ':'
                }
            }
        }

        Pattern getRegex() {
            return regexPattern;
        }

        String getPath() {
            return path;
        }

        List<String> getPathParams() {
            return pathParams;
        }

        private static String replaceTokens(String original, Pattern tokenPattern, Function<Matcher, String> converter) {
            int lastIndex = 0;
            StringBuilder output = new StringBuilder();
            Matcher matcher = tokenPattern.matcher(original);
            while (matcher.find()) {
                output.append(original, lastIndex, matcher.start())
                      .append(converter.apply(matcher));
                lastIndex = matcher.end();
            }
            if (lastIndex < original.length()) {
                output.append(original, lastIndex, original.length());
            }
            return output.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RequestPath that = (RequestPath) o;
            return Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }
    }
}