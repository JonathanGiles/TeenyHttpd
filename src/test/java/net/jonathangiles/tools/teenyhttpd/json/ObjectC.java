package net.jonathangiles.tools.teenyhttpd.json;

public class ObjectC {

    String name;
    int age;

    public ObjectC() {
    }

    public ObjectC(String name, int age) {
        this.name = name;
        this.age = age;
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

    @Override
    public String toString() {
        return "ObjectC{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
