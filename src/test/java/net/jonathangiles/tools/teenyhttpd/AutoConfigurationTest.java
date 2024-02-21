package net.jonathangiles.tools.teenyhttpd;


import net.jonathangiles.tools.teenyhttpd.model.Method;
import net.jonathangiles.tools.teenyhttpd.pack2.ExampleApplication2;
import org.junit.jupiter.api.*;

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
        Response response = executeRequest(Method.GET, "/todos/todos");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Order(4)
    @Test
    void testExampleApplication2() {
        Response response = executeRequest(Method.GET, "/todos2/todos");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Order(5)
    @Test
    void testExampleApplication3() {
        Response response = executeRequest(Method.GET, "/todos3/todos");
        Assertions.assertEquals(204, response.getStatusCode());
    }

    @Order(999)
    @Test
    void stopServer() {
        TeenyApplication.stop();
    }
}
