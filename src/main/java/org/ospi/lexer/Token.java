package org.ospi.lexer;

import java.util.function.BiPredicate;

public class Token {
    private final TokenType type;
    private final String value;
    private final BiPredicate<Token, Token> equals;
    public Token(TokenType type, String value, boolean vm) {
        this.type = type;
        this.value = value;
        BiPredicate<Token, Token> equals;
        if (vm) equals = (t1, t2) ->
                t1.getType() == t2.getType() && t1.getValue().equalsIgnoreCase(t2.getValue());
        else equals = (t1, t2) -> t1.getType() == t2.getType();
        this.equals = equals;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return equals.test(this, token);
    }

    @Override
    public String toString() {
        return String.format("Token[type=%s, value=%s]", type.toString(), value);
    }
}
