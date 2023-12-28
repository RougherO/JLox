package com.interpreter.lox;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    LEFT_SQ_BRACE, RIGHT_SQ_BRACE,
    COMMA, DOT, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    PLUS, PLUS_PLUS,
    MINUS, MINUS_MINUS,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    Q_MARK, COLON,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}
