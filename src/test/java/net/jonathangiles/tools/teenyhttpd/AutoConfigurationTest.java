package net.jonathangiles.tools.teenyhttpd;


import net.jonathangiles.tools.teenyhttpd.annotations.EnableOpenApi;
import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.pack1.ExampleApplication1;
import net.jonathangiles.tools.teenyhttpd.pack2.ExampleApplication2;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@EnableOpenApi
public class AutoConfigurationTest extends TeenyTest {

    @BeforeEach
    public void setup() {
        System.setProperty("banner", "false");

        TeenyApplication.start(AutoConfigurationTest.class);
    }

    @AfterEach
    public void tearDown() {
        TeenyApplication.stop();
    }

    @Order(2)
    @Test
    void testPostConstruct() {
        ExampleApplication2.postConstructHeader = "testPostConstruct";
    }

    @Order(3)
    @Test
    void testExampleApplication1() {
        TeenyApplicationTest.Response response = executeRequest(Method.GET, "/todos/todos");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Order(4)
    @Test
    void testExampleApplication2() {
        TeenyApplicationTest.Response response = executeRequest(Method.GET, "/todos2/todos");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Order(5)
    @Test
    void testExampleApplication3() {
        TeenyApplicationTest.Response response = executeRequest(Method.GET, "/todos3/todos");
        Assertions.assertEquals(204, response.getStatusCode());
    }

    @Order(6)
    @Test
    void testValueInjection() {
        ExampleApplication1 exampleApplication2 = TeenyApplication.getResource(ExampleApplication1.class);
        Assertions.assertEquals(3000, exampleApplication2.getServerPort());
        Assertions.assertEquals("localhost", exampleApplication2.getServerHost());
    }

    @Order(7)
    @Test
    void testOpenApi() throws IOException {
        TeenyApplicationTest.Response response = executeRequest(Method.GET, "/docs/openapi");
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        File file = new File("openapi.json");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(response.getBody());
        fileWriter.close();
    }

    @Order(999)
    @Test
    void stopServer() {
        TeenyApplication.stop();
    }
}
