package org.monkey.ast;

import org.monkey.token.Token;

import java.util.List;
import java.util.stream.Collectors;

// function will be Identifier or FunctionLiteral
public record CallExpression(Token token, Expression function, List<Expression> arguments) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append(this.function.string());
        sb.append("(");
        sb.append(this.arguments.stream().map(Node::string).collect(Collectors.joining(", ")));
        sb.append(")");
        return sb.toString();
    }
}
