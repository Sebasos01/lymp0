package org.ospi.lexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Lexer {
    private final List<Rule> tokenRules;
    private final Rule separatorRule;

    private enum State {
        START,
        MATCH,
        PARTIAL_MATCH
    }

    public Lexer() {
        tokenRules = new ArrayList<>();
        addTokenRule(TokenType.DEF_VAR_KW, "defVar");
        addTokenRule(TokenType.DEF_PROC_KW, "defProc");
        addTokenRule(TokenType.LEFT_P, "\\(");
        addTokenRule(TokenType.RIGHT_P, "\\)");
        addTokenRule(TokenType.COMMA, ",");
        addTokenRule(TokenType.CMD_SEPARATOR, ";");
        addTokenRule(TokenType.LEFT_CB, "\\{");
        addTokenRule(TokenType.RIGHT_CB, "\\}");
        addTokenRule(TokenType.ASSIGN_OP, "=");
        addTokenRule(TokenType.JUMP_CMD, "jump");
        addTokenRule(TokenType.WALK_CMD, "walk");
        addTokenRule(TokenType.LEAP_CMD, "leap");
        addTokenRule(TokenType.TURN_CMD, "turn");
        addTokenRule(TokenType.TURNTO_CMD, "turnto");
        addTokenRule(TokenType.DROP_CMD, "drop");
        addTokenRule(TokenType.GET_CMD, "get");
        addTokenRule(TokenType.GRAB_CMD, "grab");
        addTokenRule(TokenType.LETGO_CMD, "letGo");
        addTokenRule(TokenType.NOP_CMD, "nop");
        addTokenRule(TokenType.DIRECTION, "front|right|left|back|around");
        addTokenRule(TokenType.CARDINAL, "north|south|west|east");
        addTokenRule(TokenType.IF_KW, "if");
        addTokenRule(TokenType.ELSE_KW, "else");
        addTokenRule(TokenType.WHILE_KW, "while");
        addTokenRule(TokenType.REPEAT_KW, "repeat");
        addTokenRule(TokenType.TIMES_KW, "times");
        addTokenRule(TokenType.FACING_CON, "facing");
        addTokenRule(TokenType.CAN_CON, "can");
        addTokenRule(TokenType.NOT_CON, "not");
        addTokenRule(TokenType.COLON, ":");
        addTokenRule(TokenType.NAME, "[a-zA-Z_][a-zA-Z0-9_]*");
        addTokenRule(TokenType.NUMBER, "([1-9]\\d*)|0");
        separatorRule = new Rule(TokenType.SEPARATOR,
                Pattern.compile("\\s+"));
    }

    private void addTokenRule(TokenType type, String regex) {
        tokenRules.add(new Rule(type, Pattern.compile(regex, Pattern.CASE_INSENSITIVE)));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public List<Token> getTokens(String source) throws Exception {
        List<Token> tokens = new ArrayList<>();
        Iterator<Character> characters = characterIterator(source + " ");
        State state = State.START;
        StringBuilder prevCandidateSB = new StringBuilder();
        StringBuilder currentCandidateSB;
        Optional<Token> prevToken = Optional.empty();
        Optional<Token> currentToken;
        boolean shouldAdvance = true;
        String currentCharacter = "";
        while (characters.hasNext()) {
            if (shouldAdvance) currentCharacter = String.valueOf(characters.next());
            (currentCandidateSB = new StringBuilder(prevCandidateSB)).append(currentCharacter);
            String currentCandidate = currentCandidateSB.toString();
            currentToken = isToken(currentCandidate).map(n -> new Token(n, currentCandidate, false));
            switch (state) {
                case START -> {
                    if (currentToken.isPresent()) state = State.MATCH;
                    else if (isPartialToken(currentCandidate)) state = State.PARTIAL_MATCH;
                    else if (isSeparator(currentCharacter)) currentCandidateSB.setLength(0);
                    else throw new Exception("Invalid token: " + currentCandidate);
                    if (!shouldAdvance) shouldAdvance = true;
                }
                case MATCH -> {
                    if (currentToken.isPresent());
                    else if (isPartialToken(currentCandidate)) state = State.PARTIAL_MATCH;
                    else if (isPartialToken(currentCharacter)) {
                        state = State.START;
                        currentCandidateSB.setLength(0);
                        shouldAdvance = false;
                        tokens.add(prevToken.orElseThrow(() -> new Exception("Impossible state, the previous token should exist.")));
                    } else if (isSeparator(currentCharacter)) {
                        state = State.START;
                        currentCandidateSB.setLength(0);
                        tokens.add(prevToken.orElseThrow(() -> new Exception("Impossible state, the previous token should exist.")));
                    } else throw new Exception("Invalid token: " + currentCandidate);
                }
                case PARTIAL_MATCH -> {
                    if (currentToken.isPresent()) state = State.MATCH;
                    else if (isPartialToken(currentCandidate));
                    else throw new Exception("Invalid token: " + currentCandidate);
                }
            }
            prevCandidateSB = currentCandidateSB;
            prevToken = currentToken;
        }
        tokens.add(new Token(TokenType.END_SYMBOL, "$", false));
        return tokens;
    }

    private Optional<TokenType> isToken(String candidate) {
        return tokenRules.stream().filter(r -> r.accepts(candidate)).findFirst().map(Rule::type);
    }

    private boolean isPartialToken(String candidate) {
        return tokenRules.stream().anyMatch(r -> r.partiallyAccepts(candidate));
    }

    private boolean isSeparator(String candidate) {
        return separatorRule.accepts(candidate);
    }

    private Iterator<Character> characterIterator(String input) {
        return input.chars().mapToObj(c -> (char) c).iterator();
    }

}
