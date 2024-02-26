package net.jonathangiles.tools.teenyhttpd.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import net.jonathangiles.tools.teenyhttpd.Pet;
import net.jonathangiles.tools.teenyhttpd.ProductDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        teenyJson.setSerializer(String.class, (value) -> "\"hi\"");

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

    private static class Tool {
        private String name;
        private String kind;

        public Tool(String name, String kind) {
            this.name = name;
            this.kind = kind;
        }

        public String getName() {
            return name;
        }

        public Tool setName(String name) {
            this.name = name;
            return this;
        }

        public String getKind() {
            return kind;
        }

        public Tool setKind(String kind) {
            this.kind = kind;
            return this;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            Tool tool = (Tool) object;

            if (!Objects.equals(name, tool.name)) return false;
            return Objects.equals(kind, tool.kind);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (kind != null ? kind.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Tool{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    '}';
        }
    }


    @JsonIncludeNonNull
    private static class Person {
        private String name;
        private int age;
        private LocalDate birthDate;
        private LocalDateTime lastUpdated;
        @SerializedName("IceCreamLover")
        private boolean likesIceCream;
        private Pet pet;
        @SerializedName("bestSongs")
        private Set<String> favoriteSongs;
        private List<Tool> toolList;

        public List<Tool> getToolList() {
            return toolList;
        }

        public Person setToolList(List<Tool> toolList) {
            this.toolList = toolList;
            return this;
        }

        public Person setFavoriteSongs(Set<String> favoriteSongs) {
            this.favoriteSongs = favoriteSongs;
            return this;
        }

        @JsonAlias("bestSongs")
        public Set<String> getFavoriteSongs() {
            return favoriteSongs;
        }

        public Person setPet(Pet pet) {
            this.pet = pet;
            return this;
        }

        public Pet getPet() {
            return pet;
        }

        public String getName() {
            return name;
        }

        public Person setName(String name) {
            this.name = name;
            return this;
        }

        public int getAge() {
            return age;
        }

        public Person setAge(int age) {
            this.age = age;
            return this;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public Person setBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }

        public Person setLastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        @JsonAlias("IceCreamLover")
        public boolean isLikesIceCream() {
            return likesIceCream;
        }

        public Person setLikesIceCream(boolean likesIceCream) {
            this.likesIceCream = likesIceCream;
            return this;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            Person person = (Person) object;

            if (age != person.age) return false;
            if (likesIceCream != person.likesIceCream) return false;
            if (!Objects.equals(name, person.name)) return false;
            if (!Objects.equals(birthDate, person.birthDate)) return false;
            if (!Objects.equals(lastUpdated, person.lastUpdated))
                return false;
            if (!Objects.equals(pet, person.pet)) return false;
            if (!Objects.equals(favoriteSongs, person.favoriteSongs))
                return false;
            return Objects.equals(toolList, person.toolList);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + age;
            result = 31 * result + (birthDate != null ? birthDate.hashCode() : 0);
            result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
            result = 31 * result + (likesIceCream ? 1 : 0);
            result = 31 * result + (pet != null ? pet.hashCode() : 0);
            result = 31 * result + (favoriteSongs != null ? favoriteSongs.hashCode() : 0);
            result = 31 * result + (toolList != null ? toolList.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", birthDate=" + birthDate +
                    ", lastUpdated=" + lastUpdated +
                    ", likesIceCream=" + likesIceCream +
                    ", pet=" + pet +
                    ", favoriteSongs=" + favoriteSongs +
                    ", toolList=" + toolList +
                    '}';
        }
    }


}
