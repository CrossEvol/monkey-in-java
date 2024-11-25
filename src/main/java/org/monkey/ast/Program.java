package org.monkey.ast;

import java.util.ArrayList;
import java.util.List;

public class Program implements Node {

    public List<Statement> statements = new ArrayList<>();

    public Program() {
    }

    public Program(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String tokenLiteral() {
        if (statements.isEmpty()) {
            return "";
        }
        return statements.getFirst().tokenLiteral();
    }

    @Override
    public String string() {
        var sb = new StringBuilder();
        for (Statement s : statements) {
            sb.append(s.string());
        }
        return sb.toString();
    }
}
