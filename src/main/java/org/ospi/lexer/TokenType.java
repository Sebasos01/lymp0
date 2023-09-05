package org.ospi.lexer;

public enum TokenType {
    DEF_VAR_KW, // defVar
    DEF_PROC_KW, // defProc
    LEFT_P, // Paréntesis izquierdo o '(' (las comillas son para indicar que corresponde al símbolo terminal)
    RIGHT_P, // Paréntesis derecho o ')' (las comillas son para indicar que corresponde al símbolo terminal)
    COMMA, // ,
    CMD_SEPARATOR, // ;
    LEFT_CB, // Left curly brace o '{' (las comillas son para indicar que corresponde al símbolo terminal)
    RIGHT_CB, // Right curly brace o '}' (las comillas son para indicar que corresponde al símbolo terminal)
    ASSIGN_OP, //
    JUMP_CMD,
    WALK_CMD,
    LEAP_CMD,
    TURN_CMD,
    TURNTO_CMD,
    DROP_CMD,
    GET_CMD,
    GRAB_CMD,
    LETGO_CMD,
    NOP_CMD,
    DIRECTION,
    CARDINAL,
    IF_KW,
    ELSE_KW,
    WHILE_KW,
    REPEAT_KW,
    TIMES_KW,
    FACING_CON,
    CAN_CON,
    NOT_CON,
    COLON,
    NAME,
    NUMBER,
    SEPARATOR,
    END_SYMBOL
}

