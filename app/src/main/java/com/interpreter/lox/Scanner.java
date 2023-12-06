package com.interpreter.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // Adding an EOF after entire code is scanned
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = peekAndAdvance();
        switch (c) {
            // Single Character tokens
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            // Single or double character token
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/')) { // Indicates a comment
                    while (peek() != '\n' && !isAtEnd())
                        ++current;
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            // WhiteSpaces
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                ++line;
                break;

            default:
                /*
                 * Handling unexpected characters not used by Lox
                 * 
                 * Important point to note here:
                 * 
                 * -> the erroneous character is still consumed
                 * by the call to peekAndAdvance().
                 * This way we don’t get stuck in an infinite loop.
                 * -> This is also advantageous as there can be
                 * further errors in the code which can be reported
                 * to the programmer in one go.
                 * -> Also none of the code will be executed as hasError
                 * will be set to true
                 */
                Lox.error(line, "Unexpected Character");
                break;
        }
    }

    /*
     * Consumes the current character and advances by one step
     */
    private char peek() {
        return source.charAt(current);
    }

    private char peekAndAdvance() {
        return source.charAt(current++);
    }

    /*
     * Here we have two overloads of addToken
     * One for adding tokens which dont have literal values
     * Second for adding tokens which have literal values
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        ++current;
        return true;
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    /*
     * start points to the first character of the token
     * current points to the current character of the token
     * ine number for generating error messages
     */
    private int start = 0;
    private int current = 0;
    private int line = 1;
}
