package org.monkey.object;

public record ReturnValue(Object value) implements Object {
    @Override
    public ObjectType type() {
        return ObjectType.RETURN_VALUE_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        return this.value.inspect();
    }
}
