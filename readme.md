# TeenyHttpd

TeenyHttpd is an extremely basic HTTP server. It is implemented in plain Java 8 with no runtime dependencies, making it lightweight and applicable in situations where a basic, small HTTP server is all that is required.

## Getting Started

To make use of TeenyHttpd in your project, just add the following Maven dependency to your build:

```xml
<dependency>
    <groupId>net.jonathangiles.tools</groupId>
    <artifactId>teenyhttpd</artifactId>
    <version>1.0.4</version>
</dependency>
```

## Examples

### Starting TeenyHttpd

You can start a new instance of TeenyHttpd using the following code:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.start();
```

Of course, starting an HTTP server with no routes defined is hardly interesting! TeenyHttpd supports serving files from a webroot directory, and also supports programmatically defining paths to serve.

### Serving Files from a Webroot

By default, with the configuration below, TeenyHttpd will serve files from within the `/src/main/resources/webroot` 
directory, and they will be accessible from the root path (for example, a GET request for `http://localhost/index.html` will look in the root of the `webroot` directory for an `index.html` file):

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.addFileRoute("/");
server.start();
```

You can change the path to the webroot directory by passing in a `File` object to the constructor:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.addFileRoute("/", new File("/path/to/webroot"));
server.start();
```

You can also change the request path that is used to access the webroot directory:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.addFileRoute("/files", new File("/path/to/webroot"));
server.start();
```

### Programmatic Routes

You can also programmatically define routes to serve. For example, the following code will serve a `Hello world!` 
when a GET request is received for the `/hello` path:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.addStringRoute("/hello", request -> "Hello world!");
server.start();
```

#### HTTP Methods

The TeenyHttpd server supports all HTTP methods. There is an `addGetRoute` method for the commonly-used GET method, 
as well as a generic `addRoute` method that can be used to specify any HTTP method. For example, the following code
will serve a `Hello world!` when a GET request is received for the `/hello` path, but will return a 404 for any other
HTTP method:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.addGetRoute("/hello", request -> "Hello world!");
server.start();
```

For all other HTTP methods, you can use the `addRoute` method. For example, the following code will serve a `Hello world!`
when a POST request is received for the `/hello` path, but will return a 404 for any other HTTP method:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.addRoute(HttpMethod.POST, "/hello", request -> "Hello world!");
server.start();
```

### Path Parameters

It is possible to specify path parameters in the path that is registered by using the `:` character. For example, 
the following code will serve a response of `User ID: 123` when a GET request is received for the `/user/123/details`

```java
server.addGetRoute("/user/:id/details", request -> {
    String id = request.getPathParams().get("id");
    return new StringResponse(StatusCode.OK, "User ID: " + id);
});
```

### Query Parameters

It is possible to access query parameters in the request. For example, the following code will print out all query
parameters that are received in a GET request on the route `/QueryParams`:

```java
server.addGetRoute("/QueryParams", request -> {
    request.getQueryParams().forEach((key, value) -> System.out.println(key + " = " + value));
    return StatusCode.OK.asResponse();
});
```

For example, calling the route above with the `http://localhost/queryParams?test=true&foo=bar` URL will print out the following:

```
test = true
foo = bar
```

### Stopping TeenyHttpd

You stop a running instance as follows:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.start();

// some time later...
server.stop();
```

## Project Management

Releases are performed using `mvn clean deploy -Prelease`._
