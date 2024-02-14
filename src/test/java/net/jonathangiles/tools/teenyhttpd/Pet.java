package net.jonathangiles.tools.teenyhttpd;

import java.util.Objects;

public class Pet {

    private String name;
    private int age;
    private String type;

    public Pet(String name, int age, String type) {
        this.name = name;
        this.age = age;
        this.type = type;
    }

    public Pet() {
    }

    public String getName() {
        return name;
    }

    public Pet setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public Pet setAge(int age) {
        this.age = age;
        return this;
    }

    public String getType() {
        return type;
    }

    public Pet setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Pet pet = (Pet) object;

        if (age != pet.age) return false;
        if (!Objects.equals(name, pet.name)) return false;
        return Objects.equals(type, pet.type);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + age;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", type='" + type + '\'' +
                '}';
    }
}