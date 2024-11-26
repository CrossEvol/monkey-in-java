package org.monkey.object;

import org.monkey.common.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Environment {
    private final Map<java.lang.String, Object> store;
    private final Environment outer;

    public Environment() {
        this.store = new HashMap<>();
        this.outer = null;
    }

    public Environment(Environment outer) {
        this.store = new HashMap<>();
        this.outer = outer;
    }

    public Tuple<Object, java.lang.Boolean> get(java.lang.String name) {
        var object = this.store.get(name);
        if (object == null && outer != null) {
            var tuple = this.outer.get(name);
            if (tuple.value() != null) {
                return new Tuple<>(tuple.value(), true);
            }
            return new Tuple<>(null, false);
        }
        return new Tuple<>(object, !Objects.isNull(object));
    }


    public Object set(java.lang.String name, Object value) {
        this.store.put(name, value);
        return value;
    }
}
