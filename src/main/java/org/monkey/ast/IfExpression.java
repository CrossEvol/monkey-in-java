package org.monkey.ast;

import org.monkey.token.Token;

public record IfExpression(Token token,
                           Expression condition,
                           BlockStatement consequence,
                           BlockStatement alternative) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append("if");
        sb.append(this.condition.string());
        sb.append(" ");
        sb.append(this.consequence.string());
        if (this.alternative != null) {
            sb.append("else ");
            sb.append(this.alternative.string());
        }
        return sb.toString();
    }
}
