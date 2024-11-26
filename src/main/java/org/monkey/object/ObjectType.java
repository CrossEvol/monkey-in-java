package org.monkey.object;

import java.util.StringJoiner;

public enum ObjectType {
    NULL_OBJ("NULL"),
    ERROR_OBJ("ERROR"),
    INTEGER_OBJ("INTEGER"),
    BOOLEAN_OBJ("BOOLEAN"),
    STRING_OBJ("STRING"),
    RETURN_VALUE_OBJ("RETURN_VALUE"),
    FUNCTION_OBJ("FUNCTION"),
    BUILTIN_OBJ("BUILTIN"),
    ARRAY_OBJ("ARRAY"),
    HASH_OBJ("HASH");

    private final java.lang.String value;

    ObjectType(java.lang.String value) {
        this.value = value;
    }

    public java.lang.String value() {
        return value;
    }

}
