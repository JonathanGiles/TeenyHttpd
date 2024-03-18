package net.jonathangiles.tools.teenyhttpd.json;

public class ObjectB {
    String name;
    int age;
    ObjectC objectC;

    public ObjectB() {
    }

    public ObjectB(String name, int age, ObjectC objectC) {
        this.name = name;
        this.age = age;
        this.objectC = objectC;
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

    public ObjectC getObjectC() {
        return objectC;
    }

    public void setObjectC(ObjectC objectC) {
        this.objectC = objectC;
    }

    @Override
    public String toString() {
        return "ObjectB{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", objectC=" + objectC +
                '}';
    }
}
