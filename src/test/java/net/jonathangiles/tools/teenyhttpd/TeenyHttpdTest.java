package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.request.Method;
import net.jonathangiles.tools.teenyhttpd.response.StatusCode;
import net.jonathangiles.tools.teenyhttpd.response.StringResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class TeenyHttpdTest {

    private static final int TEST_PORT = 8080;

    private TeenyHttpd server;

    private CloseableHttpClient httpClient;

    @BeforeEach
    public void setup() {
        httpClient = HttpClients.createDefault();
        server = new TeenyHttpd(TEST_PORT);
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse executeRequest(Method method, String url) {
        try {
            switch (method) {
                case GET:
                    return httpClient.execute(new HttpGet(url));
                case POST:
                    return httpClient.execute(new HttpPost(url));
                case PUT:
                    return httpClient.execute(new HttpPut(url));
                case DELETE:
                    return httpClient.execute(new HttpDelete(url));
                case HEAD:
                    return httpClient.execute(new HttpHead(url));
                case TRACE:
                    return httpClient.execute(new HttpTrace(url));
                case OPTIONS:
                    return httpClient.execute(new HttpOptions(url));
//            case CONNECT:
//                return httpClient.execute(new HttpConnect(url));
                case PATCH:
                    return httpClient.execute(new HttpPatch(url));
                default:
                    throw new RuntimeException("Unsupported method: " + method);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetStaticStringRouteRequest() throws Exception {
        server.addStringRoute("/", request -> "Hello world!");
        HttpResponse response = executeRequest(Method.GET, "http://localhost");
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Hello world!", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetMultipleStaticStringRouteRequest() throws Exception {
        server.addStringRoute("/hello", request -> "Hello world!");
        server.addStringRoute("/goodbye", request -> "Goodbye world!");

        HttpResponse responseHello = executeRequest(Method.GET, "http://localhost/hello");
        assertEquals(200, responseHello.getStatusLine().getStatusCode());
        assertEquals("Hello world!", EntityUtils.toString(responseHello.getEntity()));

        HttpResponse responseGoodBye = executeRequest(Method.GET, "http://localhost/goodbye");
        assertEquals(200, responseGoodBye.getStatusLine().getStatusCode());
        assertEquals("Goodbye world!", EntityUtils.toString(responseGoodBye.getEntity()));
    }

    @Test
    public void testGetRequestWithPathParam() throws Exception {
        server.addGetRoute("/user/:id", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/user/123");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("User ID: 123", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithMultiplePathParams() throws Exception {
        server.addGetRoute("/user/:id/:name", request -> {
            String id = request.getPathParams().get("id");
            String name = request.getPathParams().get("name");
            return new StringResponse(StatusCode.OK, "User ID: " + id + ", Name: " + name);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/user/123/john");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("User ID: 123, Name: john", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithQueryParam() throws Exception {
        server.addGetRoute("/search", request -> {
            String query = request.getQueryParams().get("query");
            return new StringResponse(StatusCode.OK, "Search Query: " + query);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/search?query=test");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Search Query: test", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithMultipleQueryParams() throws Exception {
        server.addGetRoute("/search", request -> {
            String query = request.getQueryParams().get("query");
            String sort = request.getQueryParams().get("sort");
            return new StringResponse(StatusCode.OK, "Search Query: " + query + ", Sort: " + sort);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/search?query=test&sort=desc");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Search Query: test, Sort: desc", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithNonexistentPathParam() throws Exception {
        server.addGetRoute("/user/:id", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/user");

        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequestWithEmptyPathParam() throws Exception {
        server.addGetRoute("/user/:id", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/user/");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(EntityUtils.toString(response.getEntity()).contains("User ID: "));
    }

    @Test
    public void testGetRequestWithNonexistentQueryParam() throws Exception {
        server.addGetRoute("/search", request -> {
            String query = request.getQueryParams().get("query");
            return new StringResponse(StatusCode.OK, "Search Query: " + query);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/search");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(EntityUtils.toString(response.getEntity()).contains("Search Query: null"));
    }

    @Test
    public void testGetRequestWithEmptyQueryParam() throws Exception {
        server.addGetRoute("/search", request -> {
            String query = request.getQueryParams().get("query");
            return new StringResponse(StatusCode.OK, "Search Query: " + query);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/search?query=");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(EntityUtils.toString(response.getEntity()).contains("Search Query: "));
    }

    @Test
    public void testGetRequestWithSpecialCharactersInPathParam() throws Exception {
        server.addGetRoute("/user/:id", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/user/john%20doe");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("User ID: john doe", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithPathParamUrlEncoded() throws Exception {
        server.addGetRoute("/user/:id", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/user/john%2Fdoe");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("User ID: john/doe", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithSpecialCharactersInQueryParam() throws Exception {
        server.addGetRoute("/search", request -> {
            String query = request.getQueryParams().get("query");
            return new StringResponse(StatusCode.OK, "Search Query: " + query);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/search?query=java%20script");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Search Query: java script", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithQueryParamUrlEncoded() throws Exception {
        server.addGetRoute("/search", request -> {
            String query = request.getQueryParams().get("query");
            return new StringResponse(StatusCode.OK, "Search Query: " + query);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/search?query=java%2Fscript");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Search Query: java/script", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithNonAlphanumericPathParam() throws Exception {
        server.addGetRoute("/user/:id", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/user/()()()");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("User ID: ()()()", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetRequestWithNonAlphanumericQueryParam() throws Exception {
        server.addGetRoute("/search", request -> {
            String query = request.getQueryParams().get("query");
            return new StringResponse(StatusCode.OK, "Search Query: " + query);
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/search?query=()()()");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Search Query: ()()()", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testPostRequest() throws Exception {
        server.addRoute(Method.POST, "/post", request -> new StringResponse(StatusCode.OK, "Post request received"));

        HttpPost postRequest = new HttpPost("http://localhost/post");
        postRequest.setEntity(new StringEntity("test"));
        HttpResponse response = httpClient.execute(postRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Post request received", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testPutRequest() throws Exception {
        server.addRoute(Method.PUT,"/put", request -> new StringResponse(StatusCode.OK, "Put request received"));

        HttpPut putRequest = new HttpPut("http://localhost/put");
        putRequest.setEntity(new StringEntity("test"));
        HttpResponse response = httpClient.execute(putRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Put request received", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testDeleteRequest() throws Exception {
        server.addRoute(Method.DELETE, "/delete", request -> new StringResponse(StatusCode.OK, "Delete request received"));

        HttpDelete deleteRequest = new HttpDelete("http://localhost/delete");
        HttpResponse response = httpClient.execute(deleteRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Delete request received", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testHeadRequest() throws Exception {
        server.addRoute(Method.HEAD, "/head", request -> new StringResponse(StatusCode.OK, "Head request received"));

        HttpHead headRequest = new HttpHead("http://localhost/head");
        HttpResponse response = httpClient.execute(headRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertNull(response.getEntity()); // HEAD requests don't have a response body
    }

    @Test
    public void testPostRequestWithPathAndQueryParams() throws Exception {
        server.addRoute(Method.POST, "/post/:id", request -> {
            String id = request.getPathParams().get("id");
            String content = request.getQueryParams().get("content");
            return new StringResponse(StatusCode.OK, "Post request received with ID: " + id + " and content: " + content);
        });

        HttpPost postRequest = new HttpPost("http://localhost/post/123?content=test");
        postRequest.setEntity(new StringEntity("test"));
        HttpResponse response = httpClient.execute(postRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Post request received with ID: 123 and content: test", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testPutRequestWithPathAndQueryParams() throws Exception {
        server.addRoute(Method.PUT, "/put/:id", request -> {
            String id = request.getPathParams().get("id");
            String content = request.getQueryParams().get("content");
            return new StringResponse(StatusCode.OK, "Put request received with ID: " + id + " and content: " + content);
        });

        HttpPut putRequest = new HttpPut("http://localhost/put/123?content=test");
        putRequest.setEntity(new StringEntity("test"));
        HttpResponse response = httpClient.execute(putRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Put request received with ID: 123 and content: test", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testDeleteRequestWithPathAndQueryParams() throws Exception {
        server.addRoute(Method.DELETE, "/delete/:id", request -> {
            String id = request.getPathParams().get("id");
            String content = request.getQueryParams().get("content");
            return new StringResponse(StatusCode.OK, "Delete request received with ID: " + id + " and content: " + content);
        });

        HttpDelete deleteRequest = new HttpDelete("http://localhost/delete/123?content=test");
        HttpResponse response = httpClient.execute(deleteRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Delete request received with ID: 123 and content: test", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testHeadRequestWithPathAndQueryParams() throws Exception {
        server.addRoute(Method.HEAD, "/head/:id", request -> {
            String id = request.getPathParams().get("id");
            String content = request.getQueryParams().get("content");
            return new StringResponse(StatusCode.OK, "Head request received with ID: " + id + " and content: " + content);
        });

        HttpHead headRequest = new HttpHead("http://localhost/head/123?content=test");
        HttpResponse response = httpClient.execute(headRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertNull(response.getEntity()); // HEAD requests don't have a response body
    }

    @Test
    public void testConcurrentRequests() throws Exception {
        server.addGetRoute("/user/:id", request -> StatusCode.OK.asResponse());

        HttpGet request = new HttpGet("http://localhost/user/123");
        CompletableFuture<HttpResponse> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return httpClient.execute(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        CompletableFuture<HttpResponse> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return httpClient.execute(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        HttpResponse response1 = future1.get();
        HttpResponse response2 = future2.get();

        assertEquals(200, response1.getStatusLine().getStatusCode());
        assertEquals(200, response2.getStatusLine().getStatusCode());
    }

    @Test
    public void testNonExistentRouteReturns404() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "http://localhost/nonexistent");
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testUnsupportedMethodReturns405() throws Exception {
        // Add a GET route
        server.addGetRoute("/test", request -> StatusCode.OK.asResponse());

        // Try to access the route with POST method
        HttpPost postRequest = new HttpPost("http://localhost/test");
        postRequest.setEntity(new StringEntity("test"));
        HttpResponse response = httpClient.execute(postRequest);

        assertEquals(405, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testMultipleGetMethods() throws Exception {
        server.addGetRoute("/user/:id/details", request -> {
            String id = request.getPathParams().get("id");
            return new StringResponse(StatusCode.OK, "User ID: " + id);
        });
        server.addGetRoute("/QueryParams", request -> {
            request.getQueryParams().forEach((key, value) -> System.out.println(key + " = " + value));
            return new StringResponse(StatusCode.OK, "Query Params: " + request.getQueryParams());
        });

        HttpResponse response = executeRequest(Method.GET, "http://localhost/QueryParams?test=123&test2=456");
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Query Params: {test2=456, test=123}", EntityUtils.toString(response.getEntity()));
    }
}