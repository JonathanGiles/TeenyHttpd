package net.jonathangiles.tools.teenyhttpd.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import net.jonathangiles.tools.teenyhttpd.Pet;
import net.jonathangiles.tools.teenyhttpd.ProductDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonEncoderTest {

    @Test
    void testSerialization() {
        Person person = new Person()
                .setName("Alex")
                .setAge(25)
                .setLikesIceCream(true)
                .setToolList(List.of(new Tool("Mac", "Laptop"),
                        new Tool("IntelliJ", "IDE"),
                        new Tool("Coffee", "Fuel")
                ))
                .setBirthDate(LocalDate.of(1995, 1, 1))
                .setFavoriteSongs(Set.of("Radio GaGa", "Whatever it takes", "Feel Good Inc"))
                .setPet(new Pet()
                        .setName("Rocky")
                        .setType("Dog")
                        .setAge(1));

        TeenyJson teenyJson = new TeenyJson();

        long init = System.nanoTime();

        String json = teenyJson.writeValueAsString(person);

        System.out.println(json + " " + (System.nanoTime() - init) + " ns");

        init = System.nanoTime();

        json = teenyJson.writeValueAsString(person);

        System.out.println(json + " " + (System.nanoTime() - init) + " ns");


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (j, type, jsonDeserializationContext) ->
                                ZonedDateTime.parse(j.getAsJsonPrimitive().getAsString()).toLocalDateTime()
                )
                .registerTypeAdapter(
                        LocalDate.class,
                        (JsonDeserializer<LocalDate>) (localDate, type, jsonSerializationContext) ->
                                LocalDate.parse(localDate.getAsJsonPrimitive().getAsString()))
                .create();


        Person newPerson = gson.fromJson(json, Person.class);


        if (!person.equals(newPerson)) {

            System.out.println(person);
            System.out.println(newPerson);

            Assertions.fail("Objects are not equal");
        }

        init = System.nanoTime();

        teenyJson.registerSerializer(String.class, (value) -> "\"hi\"");

        json = teenyJson.writeValueAsString(person);

        System.out.println(json + " " + (System.nanoTime() - init) + " ns");

        newPerson = gson.fromJson(json, Person.class);

        Assertions.assertEquals("hi", newPerson.getName());

    }

    @Test
    void parseSimple() {

        Pet pet = new Pet("Rocky", 1, "Dog");
        String json = new TeenyJson().writeValueAsString(pet);

        Pet parsedPet = new TeenyJson().readValue(json, Pet.class);

        Assertions.assertEquals(pet, parsedPet);
    }

    @Test
    void testSerializeProduct() {
        String json = new TeenyJson().writeValueAsString(List.of(
                new ProductDto(1, "MacBook", 2000),
                new ProductDto(2, "iPhone", 1000)
                        .put("colors", Set.of("black", "blue", "pink"))
                        .put("warranty", Map.of("years", 2, "type", "full"))
                        .put("specs", List.of("A14", "5G", "FaceID"))
                        .put("chargerIncluded", true)
        ));

        System.out.println(json);
    }

    @Test
    void testEncodeInteger() {
        String json = new TeenyJson()
                .writeValueAsString(Map.of("value", 1));

        Assertions.assertEquals("{\"value\":1}", json);
    }

    @Test
    void testEncodeString() {
        String json = new TeenyJson()
                .writeValueAsString(Map.of("value", "hello"));

        Assertions.assertEquals("{\"value\":\"hello\"}", json);
    }

    @Test
    void testEncodeBoolean() {
        String json = new TeenyJson()
                .writeValueAsString(Map.of("value", true));

        Assertions.assertEquals("{\"value\":true}", json);

        json = new TeenyJson()
                .writeValueAsString(Map.of("value", false));

        Assertions.assertEquals("{\"value\":false}", json);
    }

    @Test
    void testEncodeNull() {
        Map<String, Object> map = new HashMap<>();

        map.put("value", null);

        String json = new TeenyJson()
                .writeValueAsString(map);

        Assertions.assertEquals("{\"value\":null}", json);
    }

    @Test
    void testEncodeArray() {
        String json = new TeenyJson()
                .writeValueAsString(Map.of("value", new int[]{1, 2, 3}));

        Assertions.assertEquals("{\"value\":[1,2,3]}", json);
    }

    @Test
    void testEncodeList() {
        String json = new TeenyJson()
                .writeValueAsString(Map.of("value", List.of(1, 2, 3)));

        Assertions.assertEquals("{\"value\":[1,2,3]}", json);
    }

    @Test
    void testEncodeMap() {
        String json = new TeenyJson()
                .writeValueAsString(Map.of("value", Map.of("key1", 1, "key2", 2)));

        String case1 = "{\"value\":{\"key2\":2,\"key1\":1}}";
        String case2 = "{\"value\":{\"key1\":1,\"key2\":2}}";

        if (json.equals(case1) || json.equals(case2)) {
            return;
        }

        Assertions.fail("Expected " + case1 + " or " + case2 + " but got " + json);
    }

    @Test
    void testEncodeObject() {
        String json = new TeenyJson()
                .writeValueAsString(Map.of("value", new Person().setName("Alex").setAge(25)));

        Assertions.assertEquals("{\"value\":{\"name\":\"Alex\",\"IceCreamLover\":false,\"age\":25}}", json);
    }

    @Test
    void testJsonRaw() {
        JsonRawObject target = new JsonRawObject()
                .setName("Alex")
                .setAge(30)
                .setSalary(75)
                .setDeveloper(true)
                .setProgrammingLanguages(new TeenyJson()
                        .writeValueAsString(List.of("Java", "Kotlin", "JavaScript")));

        String json = new TeenyJson().writeValueAsString(target);

        System.out.println(json);

        Map<?, ?> map = new TeenyJson()
                .readValue(json, Map.class);

        System.out.println(map);

        Assertions.assertEquals("Alex", map.get("name"));
        Assertions.assertEquals("30", map.get("age"));
        Assertions.assertEquals("75.0", map.get("salary"));
        Assertions.assertEquals(Boolean.TRUE, map.get("developer"));

        List<String> list = (List<String>) map.get("programmingLanguages");

        Assertions.assertEquals("Java", list.get(0));
        Assertions.assertEquals("Kotlin", list.get(1));
        Assertions.assertEquals("JavaScript", list.get(2));

    }

    @Test
    void testJsonIncludeNonNull() {
        JsonRawObject target = new JsonRawObject()
                .setName("Alex")
                .setAge(30)
                .setSalary(75)
                .setDeveloper(true);

        String json = new TeenyJson().writeValueAsString(target);

        System.out.println(json);

        Map<?, ?> map = new TeenyJson()
                .readValue(json, Map.class);

        Assertions.assertNotNull(map.get("name"));
        Assertions.assertNull(map.get("programmingLanguages"));
    }

}
