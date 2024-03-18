package net.jonathangiles.tools.teenyhttpd.json;

public class JsonRawObject {
    private String name;
    private int age;
    private boolean isDeveloper;
    private double salary;
    private String programmingLanguages;

    public String getName() {
        return name;
    }

    public JsonRawObject setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public JsonRawObject setAge(int age) {
        this.age = age;
        return this;
    }

    public boolean isDeveloper() {
        return isDeveloper;
    }

    public JsonRawObject setDeveloper(boolean developer) {
        isDeveloper = developer;
        return this;
    }

    public double getSalary() {
        return salary;
    }

    public JsonRawObject setSalary(double salary) {
        this.salary = salary;
        return this;
    }

    @JsonIncludeNonNull
    @JsonRaw(includeKey = true)
    public String getProgrammingLanguages() {
        return programmingLanguages;
    }

    public JsonRawObject setProgrammingLanguages(String programmingLanguages) {
        this.programmingLanguages = programmingLanguages;
        return this;
    }
}
