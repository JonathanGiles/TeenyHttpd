package net.jonathangiles.tools.teenyhttpd;

import com.google.gson.Gson;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class TeenyApplicationTest {

    private static final int TEST_PORT = 8080;
    private CloseableHttpClient httpClient;

    private static class GsonMessageConverter implements MessageConverter {

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
    }

    private static class ProtocolBufferMessageConverter implements MessageConverter {

        @Override
        public String getContentType() {
            return "application/x-protobuf";
        }

        @Override
        public void write(Object value, BufferedOutputStream dataOut) throws IOException {
            dataOut.write("hi".getBytes());
        }
    }

    @BeforeEach
    public void setup() {

        System.setProperty("banner", "false");

        httpClient = HttpClients.createDefault();

        TeenyApplication.start()
                .registerMessageConverter(new GsonMessageConverter())
                .registerMessageConverter(new ProtocolBufferMessageConverter())
                .register(new StoreController());

    }

    @AfterEach
    public void tearDown() {
        TeenyApplication.stop();

        try {
            httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private HttpResponse executeRequest(Method method, String url) {
        return executeRequest(method, url, null);
    }

    private HttpResponse executeRequest(Method method, String url, Map<String, String> headers) {

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
//            case CONNECT:
//                return httpClient.execute(new HttpConnect(url));
                case PATCH:
                    return httpClient.execute(new HttpPatch(url));
                default:
                    throw new RuntimeException("Unsupported method: " + method);
            }

            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::addHeader);
            }

            return httpClient.execute(request);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testTypedResponse() {

        Assertions.assertDoesNotThrow(() -> {

            TypedResponse.ok("Hello, World!")
                    .header("Authorization", "AuthorizedByMe");

            TypedResponse.noContent()
                    .header("Accept", "application/exceldb");

            TypedResponse.status(StatusCode.OK);

        });

    }

    @RepeatedTest(10)
    void testGetStaticStringRouteRequest() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/products");
        assertEquals(200, response.getStatusLine().getStatusCode());

        String json = EntityUtils.toString(response.getEntity());

        Product[] products = new Gson().fromJson(json, Product[].class);

        assertEquals(2, products.length);
    }


    @Test
    void testGetProductById() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/product/1");
        assertEquals(200, response.getStatusLine().getStatusCode());

        String json = EntityUtils.toString(response.getEntity());

        Product product = new Gson().fromJson(json, Product.class);

        Assertions.assertEquals(1, product.getId());
        Assertions.assertEquals("Apple", product.getName());
        Assertions.assertEquals(100, product.getPrice());
    }

    @Test
    void testGetMissingProduct() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/product/3");
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    void testHello() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/");
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("Hello, World!", EntityUtils.toString(response.getEntity()));
    }

    @Test
    void testPrivateMethod() {
        HttpResponse response = executeRequest(Method.GET, "/store/empty");
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    void testHeader() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/header", Map.of("Authorization", "AuthorizedByMe"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("AuthorizedByMe", EntityUtils.toString(response.getEntity()));
    }

    @Test
    void testHeader2() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/header2", Map.of("Authorization", "AuthorizedByMe"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("AuthorizedByMe", EntityUtils.toString(response.getEntity()));
    }

    @Test
    void testRequiredQueryParam() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/requiredQueryParam?name=John");
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("John", EntityUtils.toString(response.getEntity()));

        response = executeRequest(Method.GET, "/store/requiredQueryParam");
        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    void testContentType() {
        HttpResponse response = executeRequest(Method.GET, "/store/contentType");
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/x-protobuf", response.getFirstHeader("Content-Type").getValue());
    }

    @Test
    void testComplex() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/complex/Fluffy?age=3");
        assertEquals(200, response.getStatusLine().getStatusCode());
        String json = EntityUtils.toString(response.getEntity());

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Fluffy", 3, "Dog"), pet);
        assertNull(response.getFirstHeader("Authorization"));
    }

    @Test
    void testComplex2() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/complex/Fluffy?age=3", Map.of("Authorization", "AuthorizedByMe"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        String json = EntityUtils.toString(response.getEntity());

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Fluffy", 3, "Dog"), pet);
        assertNotNull(response.getFirstHeader("Authorization"));
        assertEquals(response.getFirstHeader("Authorization").getValue(), "AuthorizedByMe");
    }

    @Test
    void testRequestAsParameter() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/request");
        assertEquals(200, response.getStatusLine().getStatusCode());
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    @Test
    void testComplex3() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/complex/Rocky?type=Cat");
        assertEquals(200, response.getStatusLine().getStatusCode());
        String json = EntityUtils.toString(response.getEntity());

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Rocky", 21, "Cat"), pet);
    }

    @Test
    void testPet() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/pet", Map.of("Accept", "text/plain"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        String body = EntityUtils.toString(response.getEntity());

        assertEquals("text/plain", response.getFirstHeader("Content-Type").getValue());

        Pet e = new Pet("Bodoque", 21, "Dog");

        assertEquals(e.toString(), body);
    }

}
