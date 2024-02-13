package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.implementation.Main;
import net.jonathangiles.tools.teenyhttpd.model.ContentType;
import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Headers;
import net.jonathangiles.tools.teenyhttpd.model.ServerSentEventHandler;
import net.jonathangiles.tools.teenyhttpd.implementation.ServerSentEventRequest;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.model.QueryParams;
import net.jonathangiles.tools.teenyhttpd.model.Request;
import net.jonathangiles.tools.teenyhttpd.implementation.FileResponse;
import net.jonathangiles.tools.teenyhttpd.model.Response;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.implementation.StringResponse;

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
import java.util.concurrent.CountDownLatch;
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
    public static final File DEFAULT_WEB_ROOT;
    static {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        // we firstly check for the existence of a webroot directory in the classpath, and if it exists, we use it
        if (cl.getResource("webroot") != null) {
            DEFAULT_WEB_ROOT = new File(cl.getResource("webroot").getFile());
        } else {
            // otherwise, we are running from the IDE or command line, so we use the current working directory as the
            // web root
            DEFAULT_WEB_ROOT = new File(".");
        }
    }

    private final int port;

    private final Supplier<? extends ExecutorService> executorSupplier;

    private ExecutorService executorService;

    private ServerSocket serverSocket;

    private volatile boolean isRunning = false;

    private CountDownLatch startLatch;

    private final Map<Method, List<Route>> routes = new HashMap<>();

    /**
     * Starts a new server instance.
     */
    public static void main(String... args) {
        Main.main(args);
    }

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

    public void addServerSentEventRoute(String path, ServerSentEventHandler sse) {
        Route sseRoute = new Route(Method.GET, new RequestPath(path), request -> {
            Response response = StatusCode.OK.asResponse();
            response.setHeader(Headers.CONTENT_TYPE.asHeader(ContentType.EVENT_STREAM.getHeaderValue()));
            response.setHeader(Headers.CACHE_CONTROL.asHeader("no-cache"));
            response.setHeader(Headers.CONNECTION.asHeader("keep-alive"));
            response.setHeader(Headers.ACCESS_CONTROL_ALLOW_ORIGIN.asHeader("*"));
            return response;
        });
        sseRoute.setServerSentEventRoute(true);
        sseRoute.setSseHandler(sse);
        _addRoute(sseRoute);
    }

    private void _addRoute(final Method method, final String path, final Function<Request, Response> handler) {
        _addRoute(new Route(method, new RequestPath(path), handler));
    }

    private void _addRoute(final Route route) {
        routes.computeIfAbsent(route.method, k -> new ArrayList<>()).add(route);
    }

    /**
     * Starts the server instance.
     */
    public void start() {
        Thread serverThread = new Thread(this::startServer);
        serverThread.start();
        try {
            startLatch = new CountDownLatch(1);
            startLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startServer() {
        System.out.println("TeenyHttp server started.\nListening for connections on port : " + port);
        executorService = executorSupplier.get();

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            isRunning = true;
            startLatch.countDown();
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
        boolean isLongRunningConnection = false;

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

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
            List<Route> methodRoutes = routes.get(method);

            // we get request-uri requested. For now we assume it is an absolute path
            final String requestUri = parse.nextToken();

            // split it at the query param, if it exists
            final String path;
            final QueryParams queryParams;
            if (requestUri.contains("?")) {
                final String[] uriSplit = requestUri.split("\\?", 2);
                path = uriSplit[0];

                // create a lazily-evaluated object to represent the query parameters
                queryParams = new QueryParams(uriSplit[1]);
            } else {
                path = requestUri;
                queryParams = QueryParams.EMPTY;
            }

            if (methodRoutes == null) {
                // we do not support this route on the given method, but we don't know if we support it on any other
                // methods. We need to check if we support it on any other methods, and if so, we need to return a
                // 405. If we don't support it on any other methods, we need to return a 404.
                boolean isSupportedOnOtherMethods = routes.values().stream()
                        .flatMap(Collection::stream)
                        .anyMatch(p -> p.routePath.path.equals(path));

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
            List<Header> headers = new ArrayList<>();
            while (true) {
                line = in.readLine();
                if (line == null || line.isEmpty() || "\r\n".equals(line)) {
                    break;
                }
                headers.add(new Header(line));
            }

            // the request path is a full path, which may include path params within the path (e.g. ':id'), or extra path
            // information that comes after the root path (e.g. the root path may be '/', but we the path may be '/index.html').
            // We need to determine the best route to call based on the given full path, and then pass the request to that route.
            Optional<Route> route = methodRoutes.stream()
                .filter(r -> {
                    // compare the regex path to the request path, and check if they match
                    return r.routePath.getRegex().matcher(path).matches();
                }).findFirst();

            final Response response;
            Map<String, String> pathParamsMap = null;
            if (route.isPresent()) {
                // we have a route, so we call it, but first we need to parse the path params and set them in the
                // request
                final RequestPath requestPath = route.get().routePath;
                final Matcher matcher = requestPath.getRegex().matcher(path);
                if (matcher.matches()) {
                    // we have a match, so we need to parse the path params and set them in the request
                    pathParamsMap = new HashMap<>();
                    final List<String> pathParams = requestPath.getPathParams();
                    for (int i = 0; i < pathParams.size(); i++) {
                        pathParamsMap.put(pathParams.get(i), URLDecoder.decode(matcher.group(i + 1), "UTF-8"));
                    }
                }

                final Request request = Request.create(method, path, queryParams, headers, pathParamsMap);

                // This is where we actually call the callback that the user has provided for the given route.
                // Check if the response should be a streaming type based on the request headers
                if (route.get().isServerSentEventRoute()) {
                    // we have a request for a server-sent event, so we need to create a new ServerSentEvent instance
                    // and pass the request
                    isLongRunningConnection = true;
                    ServerSentEventRequest sseRequest = new ServerSentEventRequest(request, clientSocket);

                    // send the standard SSE-related headers first
                    response = route.get().handler.apply(sseRequest);
                    sendResponse(sseRequest.getWriter(), null, response.getStatusCode(), response);

                    // now start the SSE connection
                    route.get().getSseHandler().onConnect(sseRequest);
                } else {
                    // we have a normal request, so we call the route
                    response = route.get().handler.apply(request);
                    sendResponse(clientSocket, response);
                }
            } else {
                System.out.println("No route found for " + path + " on method " + method);
                System.out.println("  - Available routes are:");
                methodRoutes.forEach(rp -> System.out.println("    - " + rp.routePath));
                response = StatusCode.NOT_FOUND.asResponse();
                sendResponse(clientSocket, response);
            }
        } catch (IOException e) {
            System.err.println("Server error 2 : " + e);
        } finally {
            try {
                if (!isLongRunningConnection) {
                    if (in != null) {
                        in.close();
                    }
                    clientSocket.close();
                }
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
            sendResponse(out, dataOut, statusCode, response);
        } catch (IOException ioe) {
            System.err.println("Server error when trying to serve request");
            System.err.println("Server error : " + ioe);
        }
    }

    private void sendResponse(PrintWriter out, BufferedOutputStream dataOut, StatusCode statusCode, Response response) {
        try {
            if (out != null) {
                // write headers
                out.println((statusCode == null ? response.getStatusCode() : statusCode).toString());
                out.println("Server: TeenyHttpd from JonathanGiles.net : 1.0");
                out.println("Date: " + LocalDateTime.now());

                if (response != null) {
                    response.getHeaders().forEach(h -> out.println(h.toString()));
                }

                out.println(); // empty line between header and body
                out.flush();   // flush character output stream buffer
            }

            if (response != null && dataOut != null) {
                // write body
                response.writeBody(dataOut);
                dataOut.flush(); // flush binary output stream buffer
            }
        } catch (IOException ioe) {
            System.err.println("Server error when trying to serve request");
            System.err.println("Server error : " + ioe);
        }
    }

    private static class Route {
        private final Method method;
        private final RequestPath routePath;
        private final Function<Request, Response> handler;
        private boolean isServerSentEventRoute;

        private ServerSentEventHandler sseHandler;

        public Route(Method method, RequestPath routePath, Function<Request, Response> handler) {
            this.method = method;
            this.routePath = routePath;
            this.handler = handler;
        }

        public boolean isServerSentEventRoute() {
            return isServerSentEventRoute;
        }

        public void setServerSentEventRoute(boolean isServerSentEventRoute) {
            this.isServerSentEventRoute = isServerSentEventRoute;
        }

        public ServerSentEventHandler getSseHandler() {
            return sseHandler;
        }

        public void setSseHandler(ServerSentEventHandler sseHandler) {
            this.sseHandler = sseHandler;
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

        @Override
        public String toString() {
            return "RequestPath{" +
                    "path='" + path + "'" +
                    ", regexPattern=" + regexPattern +
                    ", pathParams=" + pathParams +
                    '}';
        }
    }
}