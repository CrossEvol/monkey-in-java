package org.monkey.object;

public record Integer(java.lang.Integer value) implements Object, Hashable {

    @Override
    public HashKey hashKey() {
        return new HashKey(this.type(), (long)this.value);
    }

    @Override
    public ObjectType type() {
        return ObjectType.INTEGER_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        return java.lang.String.format("%d", this.value);
    }
}
