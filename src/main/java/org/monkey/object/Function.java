package org.monkey.object;

import org.monkey.ast.BlockStatement;
import org.monkey.ast.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public record Function(List<Identifier> parameters, BlockStatement body, Environment env) implements Object {
    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION_OBJ;
    }

    @Override
    public java.lang.String inspect() {
        var sb = new StringBuilder();
        sb.append("fn");
        sb.append("(");
        sb.append(parameters.stream().map(Identifier::string).collect(Collectors.joining(", ")));
        sb.append(") {\n");
        sb.append(body.string());
        sb.append("\n}");
        return sb.toString();
    }
}
