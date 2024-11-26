package org.monkey.parser;

import org.monkey.ast.ArrayLiteral;
import org.monkey.ast.BlockStatement;
import org.monkey.ast.BooleanLiteral;
import org.monkey.ast.CallExpression;
import org.monkey.ast.Expression;
import org.monkey.ast.ExpressionStatement;
import org.monkey.ast.FunctionLiteral;
import org.monkey.ast.HashLiteral;
import org.monkey.ast.Identifier;
import org.monkey.ast.IfExpression;
import org.monkey.ast.IndexExpression;
import org.monkey.ast.InfixExpression;
import org.monkey.ast.IntegerLiteral;
import org.monkey.ast.LetStatement;
import org.monkey.ast.PrefixExpression;
import org.monkey.ast.Program;
import org.monkey.ast.ReturnStatement;
import org.monkey.ast.Statement;
import org.monkey.ast.StringLiteral;
import org.monkey.lexer.Lexer;
import org.monkey.token.Token;
import org.monkey.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


public class Parser {

    enum Precedence {
        DEFAULT,
        LOWEST,
        EQUALS,
        LESS_GREATER,
        SUM,
        PRODUCT,
        PREFIX,
        CALL,
        INDEX,

    }

    private static final Map<TokenType, Precedence> precedences = Map.ofEntries(
            Map.entry(TokenType.EQ, Precedence.EQUALS),
            Map.entry(TokenType.NOT_EQ, Precedence.EQUALS),
            Map.entry(TokenType.LT, Precedence.LESS_GREATER),
            Map.entry(TokenType.GT, Precedence.LESS_GREATER),
            Map.entry(TokenType.PLUS, Precedence.SUM),
            Map.entry(TokenType.MINUS, Precedence.SUM),
            Map.entry(TokenType.SLASH, Precedence.PRODUCT),
            Map.entry(TokenType.ASTERISK, Precedence.PRODUCT),
            Map.entry(TokenType.LPAREN, Precedence.CALL),
            Map.entry(TokenType.LBRACKET, Precedence.INDEX)
    );

    Supplier<Expression> parseIdentifier = () -> new Identifier(this.curToken, this.curToken.literal());

    Supplier<Expression> parseIntegerLiteral = () -> {
        try {
            return new IntegerLiteral(this.curToken, Integer.parseInt(this.curToken.literal(), 10));
        } catch (NumberFormatException e) {
            this.errors.add(String.format("could not parse %s as integer", this.curToken.literal()));
            return null;
        }
    };

    Supplier<Expression> parseStringLiteral = () -> new StringLiteral(this.curToken, this.curToken.literal());

    Supplier<Expression> parsePrefixExpression = () -> {
        var token = this.curToken;
        var operator = this.curToken.literal();
        this.nextToken();
        var right = this.parseExpression(Precedence.PREFIX);
        return new PrefixExpression(token, operator, right);
    };

    Supplier<Expression> parseBoolean = () -> new BooleanLiteral(this.curToken, this.curTokenIs(TokenType.TRUE));

    Supplier<Expression> parseGroupedExpression = () -> {
        this.nextToken();
        var expression = this.parseExpression(Precedence.LOWEST);
        if (!this.expectPeek(TokenType.RPAREN)) {
            return null;
        }
        return expression;
    };

    Supplier<Expression> parseIfExpression = () -> {
        var token = this.curToken;

        if (!this.expectPeek(TokenType.LPAREN)) {
            return null;
        }

        this.nextToken();
        var condition = this.parseExpression(Precedence.LOWEST);

        if (!this.expectPeek(TokenType.RPAREN)) {
            return null;
        }
        if (!this.expectPeek(TokenType.LBRACE)) {
            return null;
        }

        var consequence = this.parseBlockStatement();

        if (this.peekTokenIs(TokenType.ELSE)) {
            this.nextToken();
            if (!this.expectPeek(TokenType.LBRACE)) {
                return null;
            }
        }
        var alternative = this.parseBlockStatement();


        return new IfExpression(token, condition, consequence, alternative);
    };

    Supplier<Expression> parseFunctionLiteral = () -> {
        var token = this.curToken;

        if (!this.expectPeek(TokenType.LPAREN)) {
            return null;
        }
        var parameters = this.parseFunctionParameters();

        if (!this.expectPeek(TokenType.LBRACE)) {
            return null;
        }
        var body = this.parseBlockStatement();

        return new FunctionLiteral(token, parameters, body);
    };

    Supplier<Expression> parseArrayLiteral =
            () -> new ArrayLiteral(this.curToken, this.parseExpressionList(TokenType.RBRACKET));

    Supplier<Expression> parseHashLiteral = () -> {
        var token = this.curToken;
        var pairs = new HashMap<Expression, Expression>();

        while (!this.peekTokenIs(TokenType.RBRACE)) {
            this.nextToken();
            var key = this.parseExpression(Precedence.LOWEST);

            if (!this.expectPeek(TokenType.COLON)) {
                return null;
            }

            this.nextToken();
            var value = this.parseExpression(Precedence.LOWEST);

            pairs.put(key, value);

            if (!this.peekTokenIs(TokenType.RBRACE) && !this.expectPeek(TokenType.COMMA)) {
                return null;
            }

        }

        if (!this.expectPeek(TokenType.RBRACE)) {
            return null;
        }

        return new HashLiteral(token, pairs);
    };

    Function<Expression, Expression> parseInfixExpression = (left) -> {
        var token = this.curToken;
        var operator = this.curToken.literal();

        var precedence = this.curPrecedence();
        this.nextToken();
        var right = this.parseExpression(precedence);

        return new InfixExpression(token, operator, left, right);
    };

    Function<Expression, Expression> parseCallExpression = (function) -> {
        var token = this.curToken;
        var arguments = this.parseExpressionList(TokenType.RPAREN);
        return new CallExpression(token, function, arguments);
    };

    Function<Expression, Expression> parseIndexExpression = (left) -> {
        var token = this.curToken;

        this.nextToken();
        var index = this.parseExpression(Precedence.LOWEST);

        if (!this.expectPeek(TokenType.RBRACKET)) {
            return null;
        }

        return new IndexExpression(token, left, index);
    };


    private final Lexer lexer;
    private final List<String> errors = new ArrayList<>();
    private Token curToken;
    private Token peekToken;
    private final Map<TokenType, Supplier<Expression>> prefixParseFns;
    private final Map<TokenType, Function<Expression, Expression>> infixParseFns;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        this.prefixParseFns = new HashMap<>();
        this.register(TokenType.IDENT, parseIdentifier);
        this.register(TokenType.INT, parseIntegerLiteral);
        this.register(TokenType.STRING, parseStringLiteral);
        this.register(TokenType.BANG, parsePrefixExpression);
        this.register(TokenType.MINUS, parsePrefixExpression);
        this.register(TokenType.TRUE, parseBoolean);
        this.register(TokenType.FALSE, parseBoolean);
        this.register(TokenType.LPAREN, parseGroupedExpression);
        this.register(TokenType.IF, parseIfExpression);
        this.register(TokenType.FUNCTION, parseFunctionLiteral);
        this.register(TokenType.LBRACKET, parseArrayLiteral);
        this.register(TokenType.LBRACE, parseHashLiteral);

        this.infixParseFns = new HashMap<>();
        this.register(TokenType.PLUS, parseInfixExpression);
        this.register(TokenType.MINUS, parseInfixExpression);
        this.register(TokenType.SLASH, parseInfixExpression);
        this.register(TokenType.ASTERISK, parseInfixExpression);
        this.register(TokenType.EQ, parseInfixExpression);
        this.register(TokenType.NOT_EQ, parseInfixExpression);
        this.register(TokenType.LT, parseInfixExpression);
        this.register(TokenType.GT, parseInfixExpression);

        this.register(TokenType.LPAREN, parseCallExpression);
        this.register(TokenType.LBRACKET, parseIndexExpression);

        this.nextToken();
        this.nextToken();
    }

    public Program parseProgram() {
        var statements = new ArrayList<Statement>();
        while (!this.curTokenIs(TokenType.EOF)) {
            var stmt = this.parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
            this.nextToken();
        }
        return new Program(statements);
    }

    private Statement parseStatement() {
        return switch (this.curToken.type()) {
            case TokenType.LET -> this.parseLetStatement();
            case TokenType.RETURN -> this.parseReturnStatement();
            default -> this.parseExpressionStatement();
        };
    }

    private List<Identifier> parseFunctionParameters() {
        var identifiers = new ArrayList<Identifier>();

        if (this.peekTokenIs(TokenType.RPAREN)) {
            this.nextToken();
            return identifiers;
        }

        this.nextToken();

        identifiers.add(new Identifier(this.curToken, this.curToken.literal()));

        while (this.peekTokenIs(TokenType.COMMA)) {
            this.nextToken();
            this.nextToken();
            identifiers.add(new Identifier(this.curToken, this.curToken.literal()));
        }

        if (!this.expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return identifiers;
    }

    private BlockStatement parseBlockStatement() {
        var token = this.curToken;
        var statements = new ArrayList<Statement>();

        this.nextToken();

        while (!this.curTokenIs(TokenType.RBRACE) && !this.curTokenIs(TokenType.EOF)) {
            var statement = this.parseStatement();
            if (statement != null) {
                statements.add(statement);
            }
            this.nextToken();
        }

        if (statements.isEmpty()) { return null; }
        return new BlockStatement(token, statements);

    }

    private LetStatement parseLetStatement() {
        var token = this.curToken;
        if (!this.expectPeek(TokenType.IDENT)) {
            return null;
        }
        var name = new Identifier(this.curToken, this.curToken.literal());

        if (!this.expectPeek(TokenType.ASSIGN)) {
            return null;
        }

        this.nextToken();

        var value = this.parseExpression(Precedence.LOWEST);
        if (this.peekTokenIs(TokenType.SEMICOLON)) {
            this.nextToken();
        }

        return new LetStatement(token, name, value);
    }

    private List<Expression> parseExpressionList(TokenType end) {
        var list = new ArrayList<Expression>();
        if (this.peekTokenIs(end)) {
            this.nextToken();
            return list;
        }

        this.nextToken();
        list.add(this.parseExpression(Precedence.LOWEST));

        while (this.peekTokenIs(TokenType.COMMA)) {
            this.nextToken();
            this.nextToken();
            list.add(this.parseExpression(Precedence.LOWEST));
        }

        if (!this.expectPeek(end)) {
            return null;
        }

        return list;

    }

    private ReturnStatement parseReturnStatement() {
        var token = this.curToken;
        this.nextToken();
        var returnValue = this.parseExpression(Precedence.LOWEST);
        if (this.peekTokenIs(TokenType.SEMICOLON)) {
            this.nextToken();
        }
        return new ReturnStatement(token, returnValue);
    }

    private ExpressionStatement parseExpressionStatement() {
        var token = this.curToken;
        var expression = this.parseExpression(Precedence.LOWEST);
        if (this.peekTokenIs(TokenType.SEMICOLON)) {
            this.nextToken();
        }
        return new ExpressionStatement(token, expression);
    }

    private Expression parseExpression(Precedence precedence) {
        var prefix = this.prefixParseFns.get(this.curToken.type());
        if (prefix == null) {
            this.noPrefixParseFnError(this.curToken.type());
            return null;
        }
        var leftExpression = prefix.get();
        while (!this.peekTokenIs(TokenType.SEMICOLON) && precedence.ordinal() < this.peekPrecedence().ordinal()) {
            var infix = this.infixParseFns.get(this.peekToken.type());
            if (infix == null) {
                return leftExpression;
            }

            this.nextToken();
            leftExpression = infix.apply(leftExpression);
        }

        return leftExpression;
    }

    private boolean curTokenIs(TokenType t) {
        return this.curToken.type() == t;
    }

    private boolean peekTokenIs(TokenType t) {
        return this.peekToken.type() == t;
    }

    private boolean expectPeek(TokenType t) {
        if (this.peekTokenIs(t)) {
            this.nextToken();
            return true;
        } else {
            this.peekError(t);
            return false;
        }
    }

    public List<String> errors() {
        return this.errors;
    }

    private void peekError(TokenType t) {
        this.errors.add(String.format("expected next token to be %s, got %s instead",
                                      t,
                                      peekToken.type()));
    }

    private void noPrefixParseFnError(TokenType t) {
        this.errors.add(String.format("no prefix parse function for %s found", t));
    }

    private void nextToken() {
        this.curToken = this.peekToken;
        this.peekToken = this.lexer.nextToken();
    }

    private Precedence peekPrecedence() {
        return precedences.getOrDefault(peekToken.type(), Precedence.LOWEST);
    }

    private Precedence curPrecedence() {
        return precedences.getOrDefault(curToken.type(), Precedence.LOWEST);
    }

    private void register(TokenType tokenType, Supplier<Expression> prefixParseFn) {
        this.prefixParseFns.put(tokenType, prefixParseFn);
    }

    private void register(TokenType tokenType, Function<Expression, Expression> infixParseFn) {
        this.infixParseFns.put(tokenType, infixParseFn);
    }
}
