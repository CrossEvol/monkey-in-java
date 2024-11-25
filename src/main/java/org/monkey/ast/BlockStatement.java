package org.monkey.ast;

import org.monkey.token.Token;

import java.util.List;

public record BlockStatement(Token token, List<Statement> statements) implements Statement {

    @Override
    public String tokenLiteral() {
        return this.token.literal();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        for (Statement stmt : statements) {
            sb.append(stmt.string());
        }
        return sb.toString();
    }
}
