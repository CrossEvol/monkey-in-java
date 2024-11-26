package org.monkey.object;

import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Collectors;

public record Hash(Map<HashKey, HashPair> pairs) implements Object {
    @Override
    public ObjectType type() {
        return ObjectType.HASH_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        var sb = new StringBuilder();
        sb.append("{");
        sb.append(this.pairs.values()
                            .stream()
                            .map(pair -> java.lang.String.format("%s: %s",
                                                                 pair.key().inspect(),
                                                                 pair.value().inspect()))
                            .collect(
                                    Collectors.joining(", ")));
        sb.append("}");
        return sb.toString();
    }
}
