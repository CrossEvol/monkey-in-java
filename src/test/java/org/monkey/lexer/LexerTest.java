package org.monkey.lexer;

import com.sun.jdi.ArrayReference;
import org.junit.jupiter.api.Test;
import org.monkey.token.Token;
import org.monkey.token.TokenType;

import java.io.BufferedReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {

    record Pair(TokenType expectedType, String expectedLiteral) { }

    @Test
    void test_nextToken() {
        var input = """
                    let five = 5;
                    let ten = 10;

                    let add = fn(x, y) {
                      x + y;
                    };

                    let result = add(five, ten);
                    !-/*5;
                    5 < 10 > 5;

                    if (5 < 10) {
                    	return true;
                    } else {
                    	return false;
                    }

                    10 == 10;
                    10 != 9;
                    "foobar"
                    "foo bar"
                    [1, 2];
                    {"foo": "bar"}
                           \s""";

        var tests = List.of(
            new Pair(TokenType.LET, "let"),
            new Pair(TokenType.IDENT, "five"),
            new Pair(TokenType.ASSIGN, "="),
            new Pair(TokenType.INT, "5"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.LET, "let"),
            new Pair(TokenType.IDENT, "ten"),
            new Pair(TokenType.ASSIGN, "="),
            new Pair(TokenType.INT, "10"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.LET, "let"),
            new Pair(TokenType.IDENT, "add"),
            new Pair(TokenType.ASSIGN, "="),
            new Pair(TokenType.FUNCTION, "fn"),
            new Pair(TokenType.LPAREN, "("),
            new Pair(TokenType.IDENT, "x"),
            new Pair(TokenType.COMMA, ","),
            new Pair(TokenType.IDENT, "y"),
            new Pair(TokenType.RPAREN, ")"),
            new Pair(TokenType.LBRACE, "{"),
            new Pair(TokenType.IDENT, "x"),
            new Pair(TokenType.PLUS, "+"),
            new Pair(TokenType.IDENT, "y"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.RBRACE, "}"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.LET, "let"),
            new Pair(TokenType.IDENT, "result"),
            new Pair(TokenType.ASSIGN, "="),
            new Pair(TokenType.IDENT, "add"),
            new Pair(TokenType.LPAREN, "("),
            new Pair(TokenType.IDENT, "five"),
            new Pair(TokenType.COMMA, ","),
            new Pair(TokenType.IDENT, "ten"),
            new Pair(TokenType.RPAREN, ")"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.BANG, "!"),
            new Pair(TokenType.MINUS, "-"),
            new Pair(TokenType.SLASH, "/"),
            new Pair(TokenType.ASTERISK, "*"),
            new Pair(TokenType.INT, "5"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.INT, "5"),
            new Pair(TokenType.LT, "<"),
            new Pair(TokenType.INT, "10"),
            new Pair(TokenType.GT, ">"),
            new Pair(TokenType.INT, "5"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.IF, "if"),
            new Pair(TokenType.LPAREN, "("),
            new Pair(TokenType.INT, "5"),
            new Pair(TokenType.LT, "<"),
            new Pair(TokenType.INT, "10"),
            new Pair(TokenType.RPAREN, ")"),
            new Pair(TokenType.LBRACE, "{"),
            new Pair(TokenType.RETURN, "return"),
            new Pair(TokenType.TRUE, "true"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.RBRACE, "}"),
            new Pair(TokenType.ELSE, "else"),
            new Pair(TokenType.LBRACE, "{"),
            new Pair(TokenType.RETURN, "return"),
            new Pair(TokenType.FALSE, "false"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.RBRACE, "}"),
            new Pair(TokenType.INT, "10"),
            new Pair(TokenType.EQ, "=="),
            new Pair(TokenType.INT, "10"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.INT, "10"),
            new Pair(TokenType.NOT_EQ, "!="),
            new Pair(TokenType.INT, "9"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.STRING, "foobar"),
            new Pair(TokenType.STRING, "foo bar"),
            new Pair(TokenType.LBRACKET, "["),
            new Pair(TokenType.INT, "1"),
            new Pair(TokenType.COMMA, ","),
            new Pair(TokenType.INT, "2"),
            new Pair(TokenType.RBRACKET, "]"),
            new Pair(TokenType.SEMICOLON, ";"),
            new Pair(TokenType.LBRACE, "{"),
            new Pair(TokenType.STRING, "foo"),
            new Pair(TokenType.COLON, ":"),
            new Pair(TokenType.STRING, "bar"),
            new Pair(TokenType.RBRACE, "}"),
            new Pair(TokenType.EOF, "")
        );

        var lexer = new Lexer(input);

        for (int i = 0; i < tests.size(); i++) {
            var token = lexer.nextToken();
            var test = tests.get(i);
            
            assertEquals(test.expectedType(), token.type(),
                String.format("tests[%d] - tokentype wrong. expected=%s, got=%s",
                    i, test.expectedType(), token.type()));
                    
            assertEquals(test.expectedLiteral(), token.literal(),
                String.format("tests[%d] - literal wrong. expected=%s, got=%s",
                    i, test.expectedLiteral(), token.literal()));
        }
    }
}