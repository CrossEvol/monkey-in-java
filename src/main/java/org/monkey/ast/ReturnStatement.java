package org.monkey.ast;

import org.monkey.token.Token;

public record ReturnStatement(Token token, Expression returnValue) implements Statement {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append(this.tokenLiteral());
        if (this.returnValue != null) {
            sb.append(this.returnValue.toString());
        }
        sb.append(";");
        return sb.toString();
    }
}
