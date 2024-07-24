package net.jonathangiles.tools.teenyhttpd.json;

public class ObjectC implements C {
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ObjectC objectC = (ObjectC) object;

        if (getAge() != objectC.getAge()) return false;
        return getName().equals(objectC.getName());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getAge();
        return result;
    }

    @Override
    public String toString() {
        return "ObjectC{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
