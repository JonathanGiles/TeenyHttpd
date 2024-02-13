package net.jonathangiles.tools.teenyhttpd.util;

public class ParameterType {

    private final Class<?> type;
    private final Class<?> parentType;//nullable

    public ParameterType(Class<?> type, Class<?> parentType) {
        this.type = type;
        this.parentType = parentType;
    }

    public Class<?> getType() {
        return type;
    }

    public Class<?> getParentType() {
        return parentType;
    }
}
