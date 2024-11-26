package org.monkey.object;

import java.util.List;
import java.util.stream.Collectors;

public record Array(List<Object> elements) implements Object {
    @Override
    public ObjectType type() {
        return ObjectType.ARRAY_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        var sb = new StringBuilder();
        sb.append("[");
        sb.append(this.elements.stream().map(Object::inspect).collect(Collectors.joining(", ")));
        sb.append("]");
        return sb.toString();
    }
}
