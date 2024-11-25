package org.monkey.ast;

import org.monkey.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public record FunctionalLiteral(Token token, List<Identifier> parameters, BlockStatement body) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append(this.tokenLiteral());
        sb.append("(");
        sb.append(this.parameters.stream().map(Identifier::string).collect(Collectors.joining(", ")));
        sb.append(")");
        sb.append(this.body.string());
        return sb.toString();
    }
}
