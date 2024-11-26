package org.monkey.token;

import java.util.StringJoiner;

public enum TokenType {
    // Special tokens
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),

    // Identifiers + literals
    IDENT("IDENT"),    // add, foobar, x, y, ...
    INT("INT"),        // 1343456
    STRING("STRING"),  // "foobar"

    // Operators
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),

    LT("<"),
    GT(">"),

    EQ("=="),
    NOT_EQ("!="),

    // Delimiters
    COMMA(","),
    SEMICOLON(";"),
    COLON(":"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),

    // Keywords
    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN");

    private final String literal;

    TokenType(String literal) {
        this.literal = literal;
    }

    public String literal() {
        return literal;
    }

    @Override
    public String toString() {
        return this.literal();
    }
}