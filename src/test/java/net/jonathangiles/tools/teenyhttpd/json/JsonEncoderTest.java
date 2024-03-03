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


}
