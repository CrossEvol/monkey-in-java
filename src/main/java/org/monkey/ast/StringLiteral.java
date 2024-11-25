package org.monkey.ast;

import org.monkey.token.Token;

public record StringLiteral(Token token, String value) implements Expression {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        return this.token.literal();
    }
}
