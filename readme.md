# TeenyHttpd

<picture><img src="https://img.shields.io/maven-central/v/net.jonathangiles.tools/teenyhttpd?color=blue" /></picture>
<picture><img src="https://img.shields.io/github/repo-size/JonathanGiles/TeenyHttpd?color=blue" /></picture>
<picture><img src="https://img.shields.io/github/license/JonathanGiles/TeenyHttpd?color=blue" /></picture>
<picture><img src="https://img.shields.io/github/actions/workflow/status/JonathanGiles/TeenyHttpd/main.yml?color=blue" /></picture>

TeenyHttpd is an extremely basic HTTP server, as well as an extremely basic application stack (similar to Spring). It is implemented in plain old Java with no runtime dependencies, making it lightweight and applicable in situations where a basic, small HTTP server and application stack is all that is required.

## Getting Started

To make use of TeenyHttpd in your project, just add the following Maven dependency to your build:

```xml
<dependency>
    <groupId>net.jonathangiles.tools</groupId>
    <artifactId>teenyhttpd</artifactId>
    <version>1.0.5</version>
</dependency>
```

## Reference Documentation

The TeenyHttpd JavaDoc for the current code in this repo is available [here](https://teenyhttpd.z22.web.core.windows.net/). This may be different than the JavaDoc for the most recent release.

## TeenyHttpd Examples

The following examples demonstrate how to use TeenyHttpd to serve static content, as well as how to programmatically define routes. Further down this page are examples of how to use the TeenyApplication application stack.

### Serving Static Content

If you already have content you want to serve, you can simply place the latest `teenyhttpd-x.y.z.jar` file in the same 
directory as your content, and then run the following command:

```bash
java -jar teenyhttpd-x.y.z.jar
```

This will start a server on port 80, and will serve all content from the directory that the JAR file is located in. 
There is a `--help` parameter that will give some useful guidance on how to customize the server, which is shown below:

``` 
Usage: java -jar teenyhttpd.jar [options]

Options:
  --port=80
      The port to run the server on
  --dir=.
      The root directory to serve files from
  --path=/
      The path to serve files from (e.g. '/blah' for http://localhost/blah
  -h, --help
      Print this help message and exit
```

### Programmatically Serving Files from a Webroot

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
    return Response.create(StatusCode.OK, "User ID: " + id);
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

## Server-Sent Events

TeenyHttpd supports Server-Sent Events (SSE). To use this feature, you need to use the `addServerSentEventRoute` method,
with a `ServerSentEventHandler`. For example, the following code will send a message to the client every second:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.addServerSentEventRoute("/events", ServerSentEventHandler.create(sse -> new Thread(() -> {
    int i = 0;
    while (sse.hasActiveConnections()) {
        sse.sendMessage(new ServerSentEventMessage("Message " + i++, "counter"));
        threadSleep(1000);
    }
}).start()));
server.start();
```

If more than one user connects to the same /events topic, they will share the same state, each getting the same value 
for `i` at the same time. If you want to customise the response per user (for example, based on a path parameter or
query parameter), you can use the message generator feature:

```java
ServerSentEventHandler sse = ServerSentEventHandler.create((ServerSentEventHandler _sse) -> {
    // start a thread and send messages to the client(s)
    new Thread(() -> {
        // all clients share the same integer value, but they get a custom message based
        // on the path parameter for :username
        AtomicInteger i = new AtomicInteger(0);

        while (_sse.hasActiveConnections()) {
            _sse.sendMessage(client -> {
                String username = client.getPathParams().get("username");
                return new ServerSentEventMessage("Hello " + username + " - " + i, "counter");
            });
            i.incrementAndGet();
            threadSleep(1000);
        }
    }).start();
});
server.addServerSentEventRoute("/sse/:username", sse);
```

The above samples assume that you start a thread when there are active connections, to send messages to connected 
clients at a regular interval. Another approach is to just have a ServerSentEventHandler that sends messages to
connected clients when a message is received. For example, the following code will send a message to all clients 
that are connected to the `/messages` topic, when a message is posted to `/message`:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
ServerSentEventHandler chatMessagesEventHandler = ServerSentEventHandler.create();
server.addServerSentEventRoute("/messages", chatMessagesEventHandler);
server.addRoute(Method.POST, "/message", request -> {
    String message = request.getQueryParams().get("message");
    if (message != null && !message.isEmpty()) {
        chatMessagesEventHandler.sendMessage(message);
    }
    return StatusCode.OK.asResponse();
});
```

For a complete example, check out the [ChatServer](https://github.com/JonathanGiles/TeenyHttpd/blob/master/src/test/java/net/jonathangiles/tools/teenyhttpd/chat/ChatServer.java) 
demo application, that demonstrates how to use Server-Sent Events to create a simple chat server.

### Stopping TeenyHttpd

You stop a running instance as follows:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.start();

// some time later...
server.stop();
```

## TeenyApplication Examples

What was shown above is the 'low-level' APIs of TeenyHttpd. However, TeenyHttpd also includes a simple application stack called TeenyApplication. This application stack is similar to Spring, but is much simpler and lighter weight.

A basic application starts as follows:

```java
public class RestApp {
    public static void main(String[] args) {
        System.setProperty("server.port", "80");
        TeenyApplication.start(RestApp.class);
    }
}
```

Of course, this isn't all that useful, as we have not defined any routes. Let's update the code above to do that now:

```java
public class RestApp {
    public static void main(String[] args) {
        System.setProperty("server.port", "80");
        TeenyApplication.start(RestApp.class);
    }

    @Post("/message")
    public void message(@QueryParam("message") String message) {
        // do something with the message that was posted to the /message route
    }

    @Get("/products")
    public Collection<Product> get() {
        // return a collection of products
    }

    @Get("/product/:id")
    public TypedResponse<Product> getProduct(@PathParam("id") int id) {
        // Do something like this, by taking the 'id' path parameter and looking up a product
        Product product = productMap.get(id);
        if (product != null) return TypedResponse.ok(product);
        return TypedResponse.notFound();
    }
}
```

As you might expect, there are annotations for many of the common use cases:

* HTTP Methods: `@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`
* Path Parameters: `@PathParam`
* Query Parameters: `@QueryParam`
* Headers: `@RequestHeader`
* Request Body: `@RequestBody`
* Server-Sent Events: `@ServerEvent`

### Server-Sent Events

Simply define the handler and give it a name, if no name is specified then the name of the method will be used instead.

```java
@ServerEvent(value = "/messages", name = "messages")
public ServerSentEventHandler chatMessages() {
    return ServerSentEventHandler.create();
}
```

Use it directly anywhere:

```java
@Post("/message")
public void message(@QueryParam("message") String message,
                    @EventHandler("messages") ServerSentEventHandler chatMessagesEventHandler) {
    chatMessagesEventHandler.sendMessage(message);
}
```

Similar to the [ChatServer](https://github.com/JonathanGiles/TeenyHttpd/blob/master/src/test/java/net/jonathangiles/tools/teenyhttpd/chat/ChatServer.java) demo application linked above (which simply uses TeenyHttpd), [there is also one built using annotations and TeenyApplication](https://github.com/JonathanGiles/TeenyHttpd/blob/master/src/test/java/net/jonathangiles/tools/teenyhttpd/chat/ChatServerButUsingAnnotations.java).

### Message Converters

TeenyApplication provides support for custom message converters. These converters are used to handle specific content types as specified by the user. For example the following code handles requests of content type `application/json`

```java
public class GsonMessageConverter implements MessageConverter {

    final Gson gson = new Gson();

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void write(Object value, BufferedOutputStream dataOut) throws IOException {
        if (value instanceof String) {
            dataOut.write(((String) value).getBytes());
            return;
        }

        dataOut.write(gson.toJson(value).getBytes());
    }

    @Override
    public Object read(String value, Type type) {
        if (String.class.isAssignableFrom((Class<?>) type)) {
            return value;
        }

        return gson.fromJson(value, type);
    }
}
```

Similar to Spring, `@Configuration` is used to provide configurations. To specify a custom MessageConverter, you can define your configuration as shown below, within your application:

```java
@Configuration
public GsonMessageConverter getGsonConverter() {
	return new GsonMessageConverter();
}
```

## TeenyJson

TeenyHttpd includes a simple JSON library called TeenyJson. It is a simple, lightweight JSON library that is used to convert JSON strings to Java objects, and vice versa.
It is not as feature-rich as other JSON libraries, but it is lightweight and easy to use.

### Parsing JSON

```java
Person person = new TeenyJson().readValue(json, Person.class);
```

Similar to jackson to parse a ambiguous property, you can use the `@JsonDeserialize` annotation:

```java
@JsonDeserialize(contentAs = ObjectC.class)
public void setList(List<? extends C> list) {
    this.list = list;
}

@JsonDeserialize(as = ObjectC.class)
public void setC(C c) {
    this.c = c;
}
```

### Generating JSON

```java
Person person = new Person("John", 30, null);
String json = new TeenyJson().writeValueAsString(person);
```

To give a property an alias in the JSON, you can use the `@JsonProperty` annotation:

```java
@JsonAlias("bestSongs")
public Set<String> getFavoriteSongs() {
    return favoriteSongs;
}
```

To ignore a property in the JSON, you can use the `@JsonIgnore` annotation:

```java
@JsonIgnore
public String getSecret() {
    return secret;
}
```

To ignore all properties that are null, you can use the `@JsonIncludeNonNull` annotation:

```java
@JsonIncludeNonNull
public class Person {
// ...
}
```

You can also customize the deserialization and serialization process by specifying a custom serializer or deserializer:

```java
new TeenyJson()
    .registerSerializer(String.class, String::toUpperCase)
    .registerParser(String.class, (value) -> value.toString().toLowerCase());
```

## Project Management

Releases are performed using `mvn clean deploy -Prelease`.