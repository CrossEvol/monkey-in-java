package org.monkey.ast;

import org.monkey.token.Token;
import org.monkey.token.TokenType;

public record Identifier(Token token, String value) implements Expression {

    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        return this.value;
    }
}
