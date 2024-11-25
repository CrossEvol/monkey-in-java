package org.monkey.ast;

import org.monkey.token.Token;

public record PrefixExpression(Token token, String operator, Expression right) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append("(");
        sb.append(this.operator);
        sb.append(this.right.string());
        sb.append(")");
        return sb.toString();
    }
}
