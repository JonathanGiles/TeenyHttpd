package net.jonathangiles.tools.teenyhttpd;

import com.google.gson.Gson;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class TeenyApplicationTest {

    private static final int TEST_PORT = 8080;



    private static class ProtocolBufferMessageConverter implements MessageConverter {

        @Override
        public String getContentType() {
            return "application/x-protobuf";
        }

        @Override
        public void write(Object value, BufferedOutputStream dataOut) throws IOException {
            dataOut.write("hi".getBytes());
        }

        @Override
        public Object read(String value, Type type) {
            return "hi";
        }
    }

    @BeforeEach
    public void setup() {

        System.setProperty("banner", "false");

        TeenyApplication.start()
                .registerMessageConverter(new GsonMessageConverter())
                .registerMessageConverter(new ProtocolBufferMessageConverter())
                .register(new StoreController());

    }

    @AfterEach
    public void tearDown() {
        TeenyApplication.stop();
    }

    static class Response {
        private final String body;
        private final int statusCode;
        private final Map<String, String> headers = new HashMap<>();

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public String getFirstHeader(String key) {
            return headers.get(key);
        }

        public Response(HttpResponse response) {
            this.statusCode = response.getStatusLine().getStatusCode();

            try {
                this.body = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            for (org.apache.http.Header header : response.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
        }
    }

    private Response executeRequest(Method method, String url) {
        return executeRequest(method, url, null, null);
    }

    private Response executeRequest(String url, Map<String, String> headers) {
        return executeRequest(Method.GET, url, headers, null);
    }

    private Response executeRequest(Method method, String url, Map<String, String> headers, String requestBody) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String basePath = "http://localhost:" + TEST_PORT;

        url = basePath + url;

        HttpUriRequest request;

        try {
            switch (method) {
                case GET:
                    request = new HttpGet(url);
                    break;
                case POST:
                    request = new HttpPost(url);
                    break;
                case PUT:
                    request = new HttpPut(url);
                    break;
                case DELETE:
                    request = new HttpDelete(url);
                    break;
                case HEAD:
                    request = new HttpHead(url);
                    break;
                case TRACE:
                    request = new HttpTrace(url);
                    break;
                case OPTIONS:
                    request = new HttpOptions(url);
                    break;
                case PATCH:
                    return new Response(httpClient.execute(new HttpPatch(url)));
                default:
                    throw new RuntimeException("Unsupported method: " + method);
            }

            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::addHeader);
            }

            if (requestBody != null && request instanceof HttpEntityEnclosingRequestBase) {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(requestBody));
            }

            return new Response(httpClient.execute(request));

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                System.out.println("Ugh");
            }
        }
    }

    @Test
    void testTypedResponse() {

        Assertions.assertDoesNotThrow(() -> {

            TypedResponse.ok("Hello, World!")
                    .setHeader("Authorization", "AuthorizedByMe");

            TypedResponse.noContent()
                    .setHeader("Accept", "application/exceldb");

            TypedResponse.status(StatusCode.OK);

        });

    }

    @RepeatedTest(10)
    void testGetStaticStringRouteRequest() {
        Response response = executeRequest(Method.GET, "/store/products");
        assertEquals(200, response.getStatusCode());

        String json = response.getBody();

        Product[] products = new Gson().fromJson(json, Product[].class);

        assertEquals(2, products.length);
    }


    @Order(1)
    @Test
    void testGetProductById() {
        Response response = executeRequest(Method.GET, "/store/product/1");
        assertEquals(200, response.getStatusCode());

        String json = response.getBody();

        Product product = new Gson().fromJson(json, Product.class);

        Assertions.assertEquals(1, product.getId());
        Assertions.assertEquals(100, product.getPrice());
    }

    @Order(2)
    @Test
    void testGetMissingProduct() {
        Response response = executeRequest(Method.GET, "/store/product/3");
        assertEquals(404, response.getStatusCode());
    }

    @Order(2)
    @Test
    void repeatedDeleteRequests() {
        Response response = executeRequest(Method.GET, "/store/product/10");
        assertEquals(404, response.getStatusCode());

        response = executeRequest(Method.GET, "/store/product/101");
        assertEquals(404, response.getStatusCode());

        response = executeRequest(Method.GET, "/store/product/102");
        assertEquals(404, response.getStatusCode());

        response = executeRequest(Method.GET, "/store/product/103");
        assertEquals(404, response.getStatusCode());
    }

    @Order(3)
    @Test
    void testCreateProduct() {
        Response response = executeRequest(Method.PUT, "/store/product", Map.of("Content-Type", "application/json"),
                new Gson().toJson(new Product(5, "Orange", 300)));
        assertEquals(201, response.getStatusCode());
        assertEquals("Product created", response.getBody());

        response = executeRequest(Method.GET, "/store/product/5");
        assertEquals(200, response.getStatusCode());
    }


    @Order(4)
    @Test
    void deleteProduct() {

        Response response = executeRequest(Method.PUT, "/store/product", Map.of("Content-Type", "application/json"),
                new Gson().toJson(new Product(5, "Orange", 300)));
        assertEquals(201, response.getStatusCode());

        response = executeRequest(Method.DELETE, "/store/product/5");
        assertEquals(200, response.getStatusCode());

        response = executeRequest(Method.DELETE, "/store/product/5");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testHello() {
        Response response = executeRequest(Method.GET, "/store/");
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello, World!", response.getBody());
    }

    @Test
    void testPrivateMethod() {
        Response response = executeRequest(Method.GET, "/store/empty");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testHeader() {
        Response response = executeRequest("/store/header", Map.of("Authorization", "AuthorizedByMe"));
        assertEquals(200, response.getStatusCode());
        assertEquals("AuthorizedByMe", response.getBody());
    }

    @Test
    void testHeader2() {
        Response response = executeRequest("/store/header2", Map.of("Authorization", "AuthorizedByMe"));
        assertEquals(200, response.getStatusCode());
        assertEquals("AuthorizedByMe", response.getBody());
    }

    @Test
    void testRequiredQueryParam() {
        Response response = executeRequest(Method.GET, "/store/requiredQueryParam?name=John");
        assertEquals(200, response.getStatusCode());
        assertEquals("John", response.getBody());

        response = executeRequest(Method.GET, "/store/requiredQueryParam");
        assertEquals(400, response.getStatusCode());
    }

    @Test
    void testContentType() {
        Response response = executeRequest(Method.GET, "/store/contentType");
        assertEquals(200, response.getStatusCode());
        assertEquals("application/x-protobuf", response.getFirstHeader("Content-Type"));
    }

    @Test
    void testComplex() {
        Response response = executeRequest(Method.GET, "/store/complex/Fluffy?age=3");
        assertEquals(200, response.getStatusCode());
        String json = response.getBody();

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Fluffy", 3, "Dog"), pet);
        assertNull(response.getFirstHeader("Authorization"));
    }

    @Test
    void testComplex2() {
        Response response = executeRequest("/store/complex/Fluffy?age=3", Map.of("Authorization", "AuthorizedByMe"));
        assertEquals(200, response.getStatusCode());
        String json = response.getBody();

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Fluffy", 3, "Dog"), pet);
        assertNotNull(response.getFirstHeader("Authorization"));
        assertEquals(response.getFirstHeader("Authorization"), "AuthorizedByMe");
    }

    @Test
    void testRequestAsParameter() {
        Response response = executeRequest(Method.GET, "/store/request");
        assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());
    }

    @Test
    void testComplex3() {
        Response response = executeRequest(Method.GET, "/store/complex/Rocky?type=Cat");
        assertEquals(200, response.getStatusCode());
        String json = response.getBody();

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Rocky", 21, "Cat"), pet);
    }

    @Test
    void testPet() {
        Response response = executeRequest("/store/pet", Map.of("Accept", "text/plain"));
        assertEquals(200, response.getStatusCode());
        String body = response.getBody();

        assertEquals("text/plain", response.getFirstHeader("Content-Type"));

        Pet e = new Pet("Bodoque", 21, "Dog");

        assertEquals(e.toString(), body);
    }

    @Test
    void testStatusCode() {
        Response response = executeRequest(Method.GET, "/store/statusCode");
        assertEquals(403, response.getStatusCode());
    }

    @Test
    void testPost() {
        Response response = executeRequest(Method.POST, "/store/requestBody", null, "Hello, World!");
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello, World!" + " Handled!", response.getBody());
    }

}
