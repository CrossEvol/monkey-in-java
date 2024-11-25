package org.monkey.ast;

import org.monkey.token.Token;

public record InfixExpression(Token token, String operator, Expression left, Expression right) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append("(");
        sb.append(this.left.string());
        sb.append(" ").append(this.operator).append(" ");
        sb.append(this.right.string());
        sb.append(")");
        return sb.toString();
    }
}
