package net.jonathangiles.tools.teenyhttpd.json;

import java.util.List;

public class ObjectD {

    private String name;
    private C c;
    private List<? extends C> list;

    public String getName() {
        return name;
    }

    public ObjectD setName(String name) {
        this.name = name;
        return this;
    }

    public C getC() {
        return c;
    }

    public List<? extends C> getList() {
        return list;
    }

    @JsonDeserialize(contentAs = ObjectC.class)
    public ObjectD setList(List<? extends C> list) {
        this.list = list;
        return this;
    }

    @JsonDeserialize(as = ObjectC.class)
    public ObjectD setC(C c) {
        this.c = c;
        return this;
    }

    @Override
    public String toString() {
        return "ObjectD{" +
                "name='" + name + '\'' +
                ", c=" + c +
                ", list=" + list +
                '}';
    }
}
