package org.monkey.object;

import java.math.BigInteger;

public record String(java.lang.String value) implements Object, Hashable {
    @Override
    public ObjectType type() {
        return ObjectType.STRING_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        return this.value;
    }

    @Override
    public HashKey hashKey() {
        BigInteger FNV_OFFSET_BASIS = new BigInteger("14695981039346656037");
        BigInteger FNV_PRIME = new BigInteger("1099511628211");

        BigInteger hash = FNV_OFFSET_BASIS;
        for (byte b : value.getBytes()) {
            hash = hash.xor(BigInteger.valueOf(b & 0xff));
            hash = hash.multiply(FNV_PRIME);
        }

        return new HashKey(type(), hash.longValue());
    }
}
