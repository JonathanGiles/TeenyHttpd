package net.jonathangiles.tools.teenyhttpd;

import com.google.gson.Gson;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;
import org.junit.jupiter.api.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class TeenyApplicationTest extends TeenyTest {

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
