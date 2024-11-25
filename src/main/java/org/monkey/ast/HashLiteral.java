package org.monkey.ast;

import org.monkey.token.Token;

import java.util.Map;
import java.util.stream.Collectors;

public record HashLiteral(Token token, Map<Expression, Expression> pairs) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append("{");
        sb.append(this.pairs.entrySet()
                            .stream()
                            .map(entry -> entry.getKey().string() + ":" + entry.getValue().string())
                            .collect(Collectors.joining(", ")));
        sb.append("}");
        return sb.toString();
    }
}
