package org.monkey.ast;

import org.monkey.token.Token;

public record LetStatement(Token token, Identifier name, Expression value) implements Statement {
    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        sb.append(this.tokenLiteral()).append(" ");
        sb.append(this.name.string());
        sb.append(" = ");
        if (this.value != null) {
            sb.append(this.value.string());
        }
        sb.append(";");
        return sb.toString();
    }
}
