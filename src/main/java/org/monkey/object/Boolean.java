package org.monkey.object;

public record Boolean(boolean value) implements Object, Hashable {

    @Override
    public HashKey hashKey() {
        return new HashKey(this.type(),
                           this.value
                                   ? 1L
                                   : 0L);
    }

    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        return this.value
                ? "true"
                : "false";
    }
}
