package net.jonathangiles.tools.teenyhttpd.implementation;

public final class ParameterType {

    private final Class<?> type;
    private final Class<?> parentType;//nullable

    ParameterType(Class<?> type, Class<?> parentType) {
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
