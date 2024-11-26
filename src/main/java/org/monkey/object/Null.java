package org.monkey.object;

public record Null() implements Object {
    @Override
    public ObjectType type() {
        return ObjectType.NULL_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        return "null";
    }
}
