package org.monkey.object;

public record Error(String message) implements Object {
    @Override
    public ObjectType type() {
        return ObjectType.ERROR_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        return "ERROR: " + this.message;
    }
}
