package org.monkey.ast;

import org.monkey.token.Token;

public record ExpressionStatement(Token token, Expression expression) implements Statement {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        if (this.expression == null) {
            return "";
        }
        return this.expression.string();
    }
}
