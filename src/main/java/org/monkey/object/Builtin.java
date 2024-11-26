package org.monkey.object;

public record Builtin() implements Object {
    @Override
    public ObjectType type() {
        return ObjectType.BUILTIN_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        return "builtin function";
    }
}
