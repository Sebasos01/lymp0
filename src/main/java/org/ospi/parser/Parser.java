package org.ospi.parser;

import org.ospi.lexer.Token;
import org.ospi.lexer.TokenType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {

    private final List<Token> tokens;
    private int currentIndex;
    private final Set<String> variables;
    private final Set<String> procedures;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentIndex = 0;
        this.variables = new HashSet<>();
        this.procedures = new HashSet<>();
    }

    public void parseP() throws ParseException {
        Token currentToken = peekToken();
        if (isCMDs(currentToken)) {
            parseCMDs(); // Procesar CMDs
            parseS1();   // Continuar con S1
        } else if (isD(currentToken)) {
            parseD();   // Procesar D
            parseS2();  // Continuar con S2
        } else {
            throw new ParseException("Token inesperado en P: " + currentToken);
        }
        System.out.println("YES");
    }

    private void parseS1() throws ParseException {
        // Verificar si el token actual pertenece a D
        if (isD(peekToken())) {
            parseD(); // Analizar D
            parseS2(); // Continuar con S2
        }
        // Como es un componente opcional, no hay acción si no pertenece a D
    }

    private void parseS2() throws ParseException { // Reemplazar 'Something' con el nombre apropiado
        // Repetir D mientras sea posible
        while (isD(peekToken())) {
            parseD();
        }
        // Opcional: CMDs S1
        if (isCMDs(peekToken())) {
            parseCMDs();
            parseS1();
        }
        // Si CMDs no está presente, no hay acción adicional, ya que es opcional
    }

    private void parseD() throws ParseException {
        Token currentToken = peekToken();
        switch (currentToken.getType()) {
            case DEF_VAR_KW:
                accept(TokenType.DEF_VAR_KW);
                variables.add(peekToken().getValue());
                accept(TokenType.NAME);
                parseV(); // Procesar V
                break;
            case DEF_PROC_KW:
                accept(TokenType.DEF_PROC_KW);
                procedures.add(peekToken().getValue());
                accept(TokenType.NAME);
                accept(TokenType.LEFT_P);
                if (peekToken().getType() == TokenType.NAME) {
                    variables.add(peekToken().getValue());
                    accept(TokenType.NAME);
                    while (peekToken().getType() == TokenType.COMMA) {
                        accept(TokenType.COMMA);
                        variables.add(peekToken().getValue());
                        accept(TokenType.NAME); // Procesar los nombres adicionales
                    }
                }
                accept(TokenType.RIGHT_P);
                parseB(); // Procesar B
                break;
            default:
                throw new ParseException("Token inesperado en D: " + currentToken);
        }
    }

    private void parseV() throws ParseException {
        Token currentToken = peekToken(); // Suponiendo que peekToken() retorna el token actual sin consumirlo
        switch (currentToken.getType()) {
            case NUMBER:
                accept(TokenType.NUMBER); // Suponiendo que accept() consume el token si coincide
                break;
            case NAME:
                if (!variables.contains(currentToken.getValue())) {
                    throw new ParseException("Variable no definida: " + currentToken.getValue());
                }
                accept(TokenType.NAME);
                break;
            default:
                throw new ParseException("Se esperaba un número o nombre, encontrado: " + currentToken);
        }
    }

    private void parseB() throws ParseException {
        accept(TokenType.LEFT_CB); // Aceptar '{'
        parseCMDs();               // Analizar CMDs
        accept(TokenType.RIGHT_CB); // Aceptar '}'
    }

    private void parseCMDs() throws ParseException {
        Token currentToken = peekToken();
        if (isBC(currentToken)) {
            parseBC(); // Procesar BC
            parseS3(); // Sigue con S3
        } else if (isNC(currentToken)) {
            parseNC(); // Procesar NC
            parseS4(); // Sigue con S4
        } else {
            throw new ParseException("Token inesperado en CMDs: " + currentToken);
        }
    }

    private void parseS3() throws ParseException {
        // Mientras haya comandos de control (BC)
        while (isBC(peekToken())) {
            parseBC(); // Procesar BC
        }
        // Opcional: NC S4 | ; S5
        Token currentToken = peekToken();
        if (isNC(currentToken)) {
            parseNC(); // Procesar NC
            parseS4(); // Continuar con S4
        } else if (currentToken.getType() == TokenType.CMD_SEPARATOR) {
            accept(TokenType.CMD_SEPARATOR); // Procesar el separador de comando ';'
            parseS5(); // Continuar con S5
        }
    }

    private void parseS4() throws ParseException {
        // Verificar si el token actual es un separador de comandos ';'
        if (peekToken().getType() == TokenType.CMD_SEPARATOR) {
            accept(TokenType.CMD_SEPARATOR); // Consumir el ';'
            parseS5(); // Continuar con S5
        }
        // No es necesario un 'else' ya que el componente es opcional
    }

    private void parseS5() throws ParseException {
        Token currentToken = peekToken();
        // Si el token actual pertenece a BC
        if (isBC(currentToken)) {
            parseBC(); // Analizar BC
            parseS3(); // Continuar con S3
        }
        // Si el token actual pertenece a NC
        else if (isNC(currentToken)) {
            parseNC(); // Analizar NC
            parseS4(); // Continuar con S4
        }
        // El componente es opcional, así que no se necesita una acción por defecto
    }

    private void parseBC() throws ParseException {
        Token currentToken = peekToken();
        switch (currentToken.getType()) {
            case IF_KW:
                accept(TokenType.IF_KW);
                parseCND();
                parseB();
                accept(TokenType.ELSE_KW);
                break;
            case WHILE_KW:
                accept(TokenType.WHILE_KW);
                parseCND();
                break;
            case REPEAT_KW:
                accept(TokenType.REPEAT_KW);
                parseV();
                accept(TokenType.TIMES_KW);
                break;
            default:
                // Si no es ninguno de los anteriores, no hace nada y procede al siguiente paso
                break;
        }
        // Parse del bloque B o SC que está fuera del opcional
        currentToken = peekToken();
        if (currentToken.getType() == TokenType.LEFT_CB) {
            parseB();
        } else if (isSC(currentToken)) {
            parseSC();
        } else {
            throw new ParseException("Se esperaba '{' o comando simple, encontrado: " + currentToken);
        }
    }

    private void parseNC() throws ParseException {
        Token currentToken = peekToken();
        if (isSC(currentToken)) {
            parseSC(); // Procesar como un comando simple (SC)
        } else if (currentToken.getType() == TokenType.NAME) {
            if (!procedures.contains(currentToken.getValue())) {
                throw new ParseException("Procedimiento no definido: " + currentToken.getValue());
            }
            accept(TokenType.NAME);
            Token nextToken = peekToken();
            switch (nextToken.getType()) {
                case LEFT_P:
                    // Procesar como llamada a función o procedimiento
                    accept(TokenType.LEFT_P);
                    if (isA(peekToken())) {
                        parseA(); // Primer argumento
                        while (peekToken().getType() == TokenType.COMMA) {
                            accept(TokenType.COMMA);
                            parseA(); // Argumentos siguientes
                        }
                    }
                    accept(TokenType.RIGHT_P);
                    break;
                case ASSIGN_OP:
                    // Procesar como asignación
                    accept(TokenType.ASSIGN_OP);
                    parseV();
                    break;
                default:
                    throw new ParseException("Se esperaba '(' o '=' después del nombre, encontrado: " + nextToken);
            }
        } else {
            throw new ParseException("Token inesperado en NC: " + currentToken);
        }
    }

    private void parseCND() throws ParseException {
        // Manejar la repetición de 'not :'
        while (peekToken().getType() == TokenType.NOT_CON) {
            accept(TokenType.NOT_CON);
            accept(TokenType.COLON);
            // Lógica para aplicar la negación. Esto podría ser, por ejemplo, alternar un flag booleano.
        }
        Token currentToken = peekToken();
        switch (currentToken.getType()) {
            case FACING_CON:
                accept(TokenType.FACING_CON);
                accept(TokenType.LEFT_P);
                accept(TokenType.CARDINAL); // Suponiendo que Car se mapea a CARDINAL
                break;
            case CAN_CON:
                accept(TokenType.CAN_CON);
                accept(TokenType.LEFT_P);
                parseSC(); // Procesar como SC
                break;
            default:
                throw new ParseException("Token inesperado en CND: " + currentToken);
        }
        accept(TokenType.RIGHT_P);
    }

    private void parseSC() throws ParseException {
        Token currentToken = peekToken();
        switch (currentToken.getType()) {
            case JUMP_CMD:
                accept(TokenType.JUMP_CMD);
                accept(TokenType.LEFT_P);
                parseV();
                accept(TokenType.COMMA);
                parseV();
                break;
            case WALK_CMD:
            case LEAP_CMD:
                accept(currentToken.getType());
                accept(TokenType.LEFT_P);
                parseV();
                if (peekToken().getType() == TokenType.COMMA) {
                    accept(TokenType.COMMA);
                    if (peekToken().getType() == TokenType.DIRECTION || peekToken().getType() == TokenType.CARDINAL) {
                        accept(peekToken().getType());
                    } else {
                        throw new ParseException("Se esperaba una dirección o un cardinal");
                    }
                }
                break;
            case TURN_CMD:
                accept(TokenType.TURN_CMD);
                accept(TokenType.LEFT_P);
                accept(TokenType.DIRECTION);
                break;
            case TURNTO_CMD:
                accept(TokenType.TURNTO_CMD);
                accept(TokenType.LEFT_P);
                accept(TokenType.CARDINAL);
                break;
            case DROP_CMD:
                accept(TokenType.DROP_CMD);
                accept(TokenType.LEFT_P);
                parseV();
                break;
            case GET_CMD:
                accept(TokenType.GET_CMD);
                accept(TokenType.LEFT_P);
                parseV();
                break;
            case GRAB_CMD:
                accept(TokenType.GRAB_CMD);
                accept(TokenType.LEFT_P);
                parseV();
                break;
            case LETGO_CMD:
                accept(TokenType.LETGO_CMD);
                accept(TokenType.LEFT_P);
                parseV();
                break;
            case NOP_CMD:
                accept(TokenType.NOP_CMD);
                accept(TokenType.LEFT_P);
                break;
            default:
                throw new ParseException("Comando no reconocido: " + currentToken);
        }
        accept(TokenType.RIGHT_P);
    }


    private void parseA() throws ParseException {
        Token currentToken = peekToken();
        switch (currentToken.getType()) {
            case NUMBER:
            case NAME:
                parseV(); // Utilizamos el método existente para V
                break;
            case DIRECTION: // Corresponde a Dir como terminal
                accept(TokenType.DIRECTION);
                break;
            case CARDINAL: // Corresponde a Car como terminal
                accept(TokenType.CARDINAL);
                break;
            default:
                throw new ParseException("Se esperaba un valor numérico, nombre, dirección o cardinal, encontrado: " + currentToken);
        }
    }

    // Métodos auxiliares para conjuntos de selección
    private boolean isSC(Token token) {
        TokenType type = token.getType();
        return type == TokenType.JUMP_CMD ||
                type == TokenType.WALK_CMD ||
                type == TokenType.LEAP_CMD ||
                type == TokenType.TURN_CMD ||
                type == TokenType.TURNTO_CMD ||
                type == TokenType.DROP_CMD ||
                type == TokenType.GET_CMD ||
                type == TokenType.GRAB_CMD ||
                type == TokenType.LETGO_CMD ||
                type == TokenType.NOP_CMD;
    }

    private boolean isBC(Token token) {
        TokenType type = token.getType();
        return type == TokenType.IF_KW ||
                type == TokenType.WHILE_KW ||
                type == TokenType.REPEAT_KW ||
                type == TokenType.LEFT_CB;
    }

    private boolean isNC(Token token) {
        TokenType type = token.getType();
        return type == TokenType.NAME ||
                type == TokenType.JUMP_CMD ||
                type == TokenType.WALK_CMD ||
                type == TokenType.LEAP_CMD ||
                type == TokenType.TURN_CMD ||
                type == TokenType.TURNTO_CMD ||
                type == TokenType.DROP_CMD ||
                type == TokenType.GET_CMD ||
                type == TokenType.GRAB_CMD ||
                type == TokenType.LETGO_CMD ||
                type == TokenType.NOP_CMD;
    }

    private boolean isA(Token token) {
        TokenType type = token.getType();
        return type == TokenType.NUMBER ||
                type == TokenType.NAME ||
                type == TokenType.DIRECTION ||
                type == TokenType.CARDINAL;
    }

    // Método auxiliar para verificar si el token pertenece a D
    private boolean isD(Token token) {
        return token.getType() == TokenType.DEF_VAR_KW ||
                token.getType() == TokenType.DEF_PROC_KW;
    }

    private boolean isCMDs(Token token) {
        return token.getType() == TokenType.IF_KW ||
                token.getType() == TokenType.WHILE_KW ||
                token.getType() == TokenType.REPEAT_KW ||
                token.getType() == TokenType.LEFT_CB ||
                token.getType() == TokenType.NAME ||
                token.getType() == TokenType.JUMP_CMD ||
                token.getType() == TokenType.WALK_CMD ||
                token.getType() == TokenType.LEAP_CMD ||
                token.getType() == TokenType.TURN_CMD ||
                token.getType() == TokenType.TURNTO_CMD ||
                token.getType() == TokenType.DROP_CMD ||
                token.getType() == TokenType.GET_CMD ||
                token.getType() == TokenType.GRAB_CMD ||
                token.getType() == TokenType.LETGO_CMD ||
                token.getType() == TokenType.NOP_CMD;
    }


    void accept(TokenType expectedType) throws ParseException {
        Token currentToken = getNextToken(); // Suponiendo que getNextToken() consume y retorna el token actual
        if (currentToken.getType() != expectedType) {
            throw new ParseException("Token inesperado: se esperaba " + expectedType + ", encontrado: " + currentToken);
        }
    }

    private Token peekToken() throws ParseException {
        // Asume que hay una variable global o de instancia 'tokens' que es una lista de tokens
        // y un índice 'currentIndex' que indica el token actual en la lista
        if (currentIndex < tokens.size()) {
            return tokens.get(currentIndex);
        } else {
            throw new ParseException("No hay más tokens disponibles");
        }
    }

    // Método para obtener el siguiente token y avanzar el índice
    private Token getNextToken() throws ParseException {
        if (currentIndex < tokens.size()) {
            return tokens.get(currentIndex++);
        } else {
            throw new ParseException("No hay más tokens disponibles");
        }
    }

    // Clase para manejar excepciones en el análisis sintáctico
    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}

