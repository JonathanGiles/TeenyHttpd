# TeenyHttpd

TeenyHttpd is an extremely basic HTTP server. It is implemented in plain Java 8 with no external dependencies, making it lightweight and applicable in situations where a basic, small HTTP server is all that is required.

## Getting Started

To make use of TeenyHttpd in your project, just add the following Maven dependency to your build:

```xml
<dependency>
    <groupId>net.jonathangiles.tools</groupId>
    <artifactId>teenyhttpd</artifactId>
    <version>1.0.2</version>
</dependency>
```

## Examples

### Starting TeenyHttpd

You can start a new instance of TeenyHttpd up simply using the following code:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.start();
```

By default, TeenyHttpd will serve files from within its own library, so you will see placeholder files. To configure the webroot from where files should be served, do the following:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.setWebroot(new File("/Users/jonathan/Code/jonathangiles.net"));
server.start();
```

With this configured, all file requests will served from this base directory.

### Stopping TeenyHttpd

You stop a running instance as follows:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT);
server.start();

// some time later...
server.stop();
```

### Overriding Response Processing

In some cases, we don't want to just serve up files from a web root directory - we want to do something more custom when a request is received in the server. To do this, we override the `serve` method, as demonstrated below:

```java
final int PORT = 80;
TeenyHttpd server = new TeenyHttpd(PORT) {
    @Override
    public Response serve(final Request request) {
        String path = request.getPath();
        
        // do work...
        
        // return response to user
        return new StringResponse(request, StatusCode.OK, "Hello!");
    }
};
server.start();
``` 

In the above code sample, we return a `StringResponse`. TeenyHttpd also has `ByteResponse` and `FileResponse` types. `ByteResponse` simply returns a `byte[]` to the caller, and `FileResponse` parses the path information from the request to return a file (which is what the default behavior of TeenyHttpd uses to do file hosting).

## Project Management

Releases are performed using `mvn clean deploy -Prelease`.
