package net.jonathangiles.tools.teenyhttpd.json;

import java.util.Objects;

public class Tool {
    private String name;
    private String kind;

    public Tool(String name, String kind) {
        this.name = name;
        this.kind = kind;
    }

    public Tool() {
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