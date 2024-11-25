package org.monkey.ast;

import org.monkey.token.Token;

public record BooleanLiteral(Token token, Boolean value) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        return this.token.literal();
    }
}
