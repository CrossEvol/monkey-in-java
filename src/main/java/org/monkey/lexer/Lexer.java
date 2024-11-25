package org.monkey.lexer;

import org.monkey.token.Token;
import org.monkey.token.TokenType;
import org.monkey.token.TokenUtil;

public class Lexer {

    private final byte[] input;
    private int position; // current position in input (points to current char)
    private int readPosition; // current reading position in input (after current char)
    byte ch; // current char under examination

    public Lexer(String input) {
        this.input = input.getBytes();
        this.readChar();
    }

    public Token nextToken() {
        Token token;
        this.skipWhitespace();
        switch (this.ch) {
            case '=': {
                if (this.peekChar() == '=') {
                    var b = this.ch;
                    this.readChar();
                    token = new Token(TokenType.EQ, new String(new byte[]{ b, this.ch }));
                } else {
                    token = newToken(TokenType.ASSIGN, this.ch);
                }
                break;
            }
            case '+': {
                token = newToken(TokenType.PLUS, this.ch);
                break;
            }
            case '-': {
                token = newToken(TokenType.MINUS, this.ch);
                break;
            }
            case '!': {
                if (this.peekChar() == '=') {
                    var b = this.ch;
                    this.readChar();
                    token = new Token(TokenType.NOT_EQ, new String(new byte[]{ b, this.ch }));
                } else {
                    token = newToken(TokenType.BANG, this.ch);
                }
                break;
            }
            case '/': {
                token = newToken(TokenType.SLASH, this.ch);
                break;
            }
            case '*': {
                token = newToken(TokenType.ASTERISK, this.ch);
                break;
            }
            case '<': {
                token = newToken(TokenType.LT, this.ch);
                break;
            }
            case '>': {
                token = newToken(TokenType.GT, this.ch);
                break;
            }
            case ';': {
                token = newToken(TokenType.SEMICOLON, this.ch);
                break;
            }
            case ':': {
                token = newToken(TokenType.COLON, this.ch);
                break;
            }
            case ',': {
                token = newToken(TokenType.COMMA, this.ch);
                break;
            }
            case '{': {
                token = newToken(TokenType.LBRACE, this.ch);
                break;
            }
            case '}': {
                token = newToken(TokenType.RBRACE, this.ch);
                break;
            }
            case '(': {
                token = newToken(TokenType.LPAREN, this.ch);
                break;
            }
            case ')': {
                token = newToken(TokenType.RPAREN, this.ch);
                break;
            }
            case '"': {
                token = new Token(TokenType.STRING, this.readString());
                break;
            }
            case '[': {
                token = newToken(TokenType.LBRACKET, this.ch);
                break;
            }
            case ']': {
                token = newToken(TokenType.RBRACKET, this.ch);
                break;
            }
            case 0: {
                token = new Token(TokenType.EOF, "");
                break;
            }
            default: {
                if (isLetter(this.ch)) {
                    var identifier = this.readIdentifier();
                    token = new Token(TokenUtil.lookupIdent(identifier), identifier);
                    return token;
                } else if (isDigit(this.ch)) {
                    token = new Token(TokenType.INT, this.readNumber());
                    return token;
                } else {
                    token = newToken(TokenType.ILLEGAL, this.ch);
                }

            }
        }
        this.readChar();
        return token;
    }

    private void readChar() {
        if (this.readPosition >= this.input.length) {
            this.ch = 0;
        } else {
            this.ch = this.input[this.readPosition];
        }
        this.position = this.readPosition;
        this.readPosition++;
    }

    private void skipWhitespace() {
        while (this.ch == ' ' || this.ch == '\t' || this.ch == '\n' || this.ch == '\r') {
            this.readChar();
        }
    }

    private byte peekChar() {
        return this.readPosition >= this.input.length
                ? 0
                : this.input[this.readPosition];
    }

    private String readIdentifier() {
        var left = this.position;
        while (this.isLetter(this.ch)) {
            this.readChar();
        }
        var bytes = new byte[this.position - left];
        System.arraycopy(this.input, left, bytes, 0, bytes.length);
        return new String(bytes);
    }

    private String readNumber() {
        var left = this.position;
        while (this.isDigit(this.ch)) {
            this.readChar();
        }
        var bytes = new byte[this.position - left];
        System.arraycopy(this.input, left, bytes, 0, bytes.length);
        return new String(bytes);
    }

    private String readString() {
        var left = this.position + 1;
        for (; ; ) {
            this.readChar();
            if (this.ch == '"' || this.ch == 0) {
                break;
            }
        }
        var bytes = new byte[this.position - left];
        System.arraycopy(this.input, left, bytes, 0, bytes.length);
        return new String(bytes);
    }

    private boolean isLetter(byte ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '_';
    }

    private boolean isDigit(byte ch) {
        return '0' <= ch && ch <= '9';
    }

    private Token newToken(TokenType tokenType, byte ch) {
        return new Token(tokenType, new String(new byte[]{ this.ch }));
    }
}
