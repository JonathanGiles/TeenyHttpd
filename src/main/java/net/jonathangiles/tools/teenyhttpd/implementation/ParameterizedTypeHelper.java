package net.jonathangiles.tools.teenyhttpd.implementation;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

/** A helper class to hold the type arguments of a parameterized type and provide some utility methods. */
public final class ParameterizedTypeHelper implements Type {
    private final Class<?> firstType;
    private final Class<?> parentType;//nullable
    private final Type[] typeArguments;

    public ParameterizedTypeHelper(Class<?> firstType, Class<?> parentType) {
        this.firstType = firstType;
        this.parentType = parentType;
        this.typeArguments = null;
    }

    ParameterizedTypeHelper(Class<?> parentType, Type[] arguments) {
        this.firstType = getRealClass(arguments[0]);
        this.parentType = parentType;
        this.typeArguments = arguments;
    }

    /**
     * Returns the real class of the given type.
     *
     * @param type the type to get the real class of
     * @return the real class of the given type
     */
    private Class<?> getRealClass(Type type) {
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return (Class<?>) wildcardType.getUpperBounds()[0];
        }

        return (Class<?>) type;
    }

    /**
     * Returns a new instance of ParameterizedTypeHelper with the given first type.
     *
     * @param firstType the first type
     * @return a new instance of ParameterizedTypeHelper
     */
    public ParameterizedTypeHelper withFirstType(Class<?> firstType) {
        if (typeArguments == null) {
            return new ParameterizedTypeHelper(firstType, parentType);
        }

        Type[] args = new Type[typeArguments.length];

        args[0] = firstType;
        System.arraycopy(typeArguments, 1, args, 1, typeArguments.length - 1);

        return new ParameterizedTypeHelper(parentType, args);
    }

    @Override
    public String getTypeName() {
        return getClass().getSimpleName();
    }

    public boolean isParentTypeOf(Class<?> type) {
        return parentType != null && type.isAssignableFrom(parentType);
    }

    public Type[] getTypeArguments() {
        return typeArguments;
    }

    public Class<?> getFirstType() {
        return firstType;
    }

    /**
     * @return the second type of the parameterized type
     * @throws NullPointerException if the type is not a ParameterizedType
     */
    public Class<?> getSecondType() {
        Objects.requireNonNull(typeArguments, "Type is not a ParameterizedType");
        return getRealClass(typeArguments[1]);
    }

    public Class<?> getParentType() {
        return parentType;
    }

    @Override
    public String toString() {
        return "ParameterizedTypeHelper{" +
                "firstType=" + firstType +
                ", parentType=" + parentType +
                ", typeArguments=" + Arrays.toString(typeArguments) +
                '}';
    }
}
