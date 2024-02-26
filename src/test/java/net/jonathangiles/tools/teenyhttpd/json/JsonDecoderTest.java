package net.jonathangiles.tools.teenyhttpd.json;

import com.google.gson.Gson;
import net.jonathangiles.tools.teenyhttpd.ProductDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class JsonDecoderTest {

    @Test
    void testSimple() {

        String json = new Gson()
                .toJson(new ProductDto(1, "Product 1", 100));

        Object value = new JsonDecoder(json)
                .read();

        Assertions.assertInstanceOf(Map.class, value);

        Map<?, ?> map = (Map<?, ?>) value;

        Assertions.assertEquals(3, map.size());

        System.out.println(value);
    }

    @Test
    void parseProduct() {

        String json = new Gson()
                .toJson(new ProductDto(1, "Product 1", 100));

        ProductDto product = new TeenyJson()
                .readValue(json, ProductDto.class);

        System.out.println(product);

        Assertions.assertEquals(1, product.getId());
        Assertions.assertEquals("Product 1", product.getName());
        Assertions.assertEquals(100, product.getPrice());
    }

    @Test
    void testArray() {
        String json = new Gson()
                .toJson(List.of(new ProductDto(1, "Product 1", 100),
                        new ProductDto(1, "Product 1", 100),
                        new ProductDto(1, "Product 1", 100)));

        Object value = new JsonDecoder(json)
                .read();

        Assertions.assertInstanceOf(List.class, value);

        List<?> list = (List<?>) value;

        Assertions.assertEquals(3, list.size());

        System.out.println(value);

        for (Object o : list) {
            Assertions.assertInstanceOf(Map.class, o);

            Map<?, ?> map = (Map<?, ?>) o;

            Assertions.assertEquals(3, map.size());
        }
    }

    @Test
    void parseArray() {
        String json = new Gson()
                .toJson(List.of(new ProductDto(1, "Product 1", 100),
                        new ProductDto(1, "Product 1", 100),
                        new ProductDto(1, "Product 1", 100)));

        List<ProductDto> products = new TeenyJson()
                .readList(json, ProductDto.class);

        System.out.println(products);

        Assertions.assertEquals(3, products.size());

        for (ProductDto product : products) {
            Assertions.assertEquals(1, product.getId());
            Assertions.assertEquals("Product 1", product.getName());
            Assertions.assertEquals(100, product.getPrice());
        }
    }

    @Test
    void testArray2() {


        String json = new Gson()
                .toJson(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        Object value = new JsonDecoder(json)
                .read();

        Assertions.assertInstanceOf(List.class, value);

        List<?> list = (List<?>) value;

        Assertions.assertEquals(10, list.size());

        Assertions.assertEquals("1", list.get(0));

        json = new Gson()
                .toJson(List.of("A", "B", "C", "D"));


        value = new JsonDecoder(json)
                .read();

        Assertions.assertInstanceOf(List.class, value);

        list = (List<?>) value;

        Assertions.assertEquals(4, list.size());

        Assertions.assertEquals("A", list.get(0));

        System.out.println(value);
    }

    @Test
    void testDeepObject() {

        String json = new Gson()
                .toJson(new ObjectA("Alex", 25,
                        true, List.of("Code", "Travel"),
                        new ObjectB("Jonathan", 30,
                                new ObjectC("John Doe", 23))));

        Object value = new JsonDecoder(json)
                .read();

        System.out.println(value);

        Assertions.assertInstanceOf(Map.class, value);

        Map<?, ?> map = (Map<?, ?>) value;

        Assertions.assertEquals(5, map.size());

        Assertions.assertInstanceOf(Map.class, map.get("objectB"));

        Map<?, ?> objectB = (Map<?, ?>) map.get("objectB");

        Assertions.assertEquals(3, objectB.size());

        Assertions.assertInstanceOf(Map.class, objectB.get("objectC"));

        Map<?, ?> objectC = (Map<?, ?>) objectB.get("objectC");

        Assertions.assertEquals(2, objectC.size());

    }

    @Test
    void parseDeepObject() {

        String json = new Gson()
                .toJson(new ObjectA("Alex", 25,
                        true, null,
                        new ObjectB("Jonathan", 30,
                                new ObjectC("John Doe", 23))));

        ObjectA objectA = new TeenyJson()
                .readValue(json, ObjectA.class);

        System.out.println(objectA);

        Assertions.assertEquals("Alex", objectA.getName());
        Assertions.assertEquals(25, objectA.getAge());
        Assertions.assertTrue(objectA.isDeveloper());

        ObjectB objectB = objectA.getObjectB();

        Assertions.assertEquals("Jonathan", objectB.getName());
        Assertions.assertEquals(30, objectB.getAge());

        ObjectC objectC = objectB.getObjectC();

        Assertions.assertEquals("John Doe", objectC.getName());
        Assertions.assertEquals(23, objectC.getAge());

    }


}
