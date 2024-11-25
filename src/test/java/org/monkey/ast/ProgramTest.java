package org.monkey.ast;

import org.junit.jupiter.api.Test;
import org.monkey.token.Token;
import org.monkey.token.TokenType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgramTest {

    @Test
    void test_string() {
        var program = new Program(List.of(new LetStatement(new Token(TokenType.LET, "let"),
                                                           new Identifier(new Token(TokenType.IDENT, "myVar"), "myVar"),
                                                           new Identifier(new Token(TokenType.IDENT, "anotherVar"),
                                                                          "anotherVar"))));
        assertEquals("let myVar = anotherVar;",
                     program.string(),
                     String.format("program.String() wrong. got=%s", program.string()));
    }
}