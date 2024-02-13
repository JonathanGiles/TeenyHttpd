package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.json.TeenyJson;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.winter.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WinterTest {


    private static final int TEST_PORT = 8080;

    private CloseableHttpClient httpClient;

    @BeforeEach
    public void setup() {

        System.setProperty("banner", "false");

        httpClient = HttpClients.createDefault();

        Winter.bootstrap()
                .add(new StoreController())
                .start();
    }

    @AfterEach
    public void tearDown() {
        Winter.stop();

        try {
            httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private HttpResponse executeRequest(Method method, String url) {

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

    final String basePath = "http://localhost:" + TEST_PORT;

    @RepeatedTest(10)
    void testGetStaticStringRouteRequest() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/products");
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("[{\"name\": \"Apple\", \"id\": 1, \"price\": 100}, {\"name\": \"Banana\", \"id\": 2, \"price\": 200}]", EntityUtils.toString(response.getEntity()));
    }


    @Test
    void testGetProductById() throws Exception {
        HttpResponse response = executeRequest(Method.GET, "/store/product/1");
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("{\"name\": \"Apple\", \"id\": 1, \"price\": 100}", EntityUtils.toString(response.getEntity()));
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

        Pet pet = TeenyJson.read(json, Pet.class);

        assertEquals(new Pet("Fluffy", 3, "Dog"), pet);
    }

}
