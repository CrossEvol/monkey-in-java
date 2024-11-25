package org.monkey.ast;

import org.monkey.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public record ArrayLiteral(Token token, List<Expression> elements) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append("[");
        sb.append(this.elements.stream().map(Node::string).collect(Collectors.joining(", ")));
        sb.append("]");
        return sb.toString();
    }
}
