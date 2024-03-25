package net.jonathangiles.tools.teenyhttpd.json;

import java.util.List;

public class ObjectA {

    String name;
    int age;
    ObjectB objectB;
    boolean developer;
    List<Object> hobbies;

    public ObjectA() {
    }

    public ObjectA(String name, int age, boolean developer, List<Object> hobbies, ObjectB objectB) {
        this.name = name;
        this.age = age;
        this.objectB = objectB;
        this.hobbies = hobbies;
        this.developer = developer;
    }

    public List<Object> getHobbies() {
        return hobbies;
    }

    public boolean isDeveloper() {
        return developer;
    }

    public void setHobbies(List<Object> hobbies) {
        this.hobbies = hobbies;
    }

    public void setDeveloper(boolean developer) {
        this.developer = developer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public ObjectB getObjectB() {
        return objectB;
    }

    public void setObjectB(ObjectB objectB) {
        this.objectB = objectB;
    }

    @Override
    public String toString() {
        return "ObjectA{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", objectB=" + objectB +
                ", developer=" + developer +
                ", hobbies=" + hobbies +
                '}';
    }
}
