package net.jonathangiles.tools.teenyhttpd;

import com.google.gson.Gson;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.*;

import java.io.BufferedOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
            dataOut.write(gson.toJson(value).getBytes());
        }
    }

    @BeforeEach
    public void setup() {

        System.setProperty("banner", "false");

        httpClient = HttpClients.createDefault();

        TeenyApplication.start()
                .registerMessageConverter(new GsonMessageConverter())
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

        String basePath = "http://localhost:" + TEST_PORT;

        url = basePath + url;

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
    void testComplex() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/complex/Fluffy?age=3");
        assertEquals(200, response.getStatusLine().getStatusCode());
        String json = EntityUtils.toString(response.getEntity());

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Fluffy", 3, "Dog"), pet);
    }

    @Test
    void testComplex2() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/complex/Rocky?type=Cat");
        assertEquals(200, response.getStatusLine().getStatusCode());
        String json = EntityUtils.toString(response.getEntity());

        Pet pet = new Gson().fromJson(json, Pet.class);

        assertEquals(new Pet("Rocky", 21, "Cat"), pet);
    }

}
