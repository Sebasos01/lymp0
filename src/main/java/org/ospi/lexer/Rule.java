package org.ospi.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Rule(TokenType type, Pattern rule) {
    public boolean accepts(String candidate) {
        return rule.matcher(candidate).matches();
    }

    public boolean partiallyAccepts(String candidate) {
        Matcher matcher = rule.matcher(candidate);
        if (matcher.matches()) return true;
        return matcher.hitEnd();
    }
}
