package org.monkey.ast;

import org.monkey.token.Token;

public record IndexExpression(Token token, Expression left, Expression index) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append("(");
        sb.append(this.left.string());
        sb.append("[");
        sb.append(this.index.string());
        sb.append("])");
        return sb.toString();
    }
}
