package org.monkey.token;

import java.util.Map;
import java.util.Optional;

public class TokenUtil {

    private static final Map<String, TokenType> keyword = Map.ofEntries(
            Map.entry("fn", TokenType.FUNCTION),
            Map.entry("let", TokenType.LET),
            Map.entry("true", TokenType.TRUE),
            Map.entry("false", TokenType.FALSE),
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("return", TokenType.RETURN)
    );

    public static TokenType lookupIdent(String ident) {
        return keyword.getOrDefault(ident, TokenType.IDENT);
    }

}
