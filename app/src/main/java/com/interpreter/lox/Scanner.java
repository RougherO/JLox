package com.interpreter.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// import com.interpreter.lox.Error.ScannerError;

public class Scanner {
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        try {
            while (!isAtEnd()) {
                start = current;
                scanToken();
            }
        } catch (LoxError.ScannerError e) {
            LoxError.panic(e);
            return null;
        }

        // Adding an EOF after entire code is scanned
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = consume();
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
            case '[':
                addToken(TokenType.RIGHT_SQ_BRACE);
                break;
            case ']':
                addToken(TokenType.RIGHT_SQ_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(match('-') ? TokenType.MINUS_MINUS : TokenType.MINUS);
                break;
            case '+':
                addToken(match('+') ? TokenType.PLUS_PLUS : TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '?':
                addToken(TokenType.Q_MARK);
                break;
            case ':':
                addToken(TokenType.COLON);
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
            case '"':
                /*
                 * consume the current quote and parse the rest string
                 */
                string();
                break;

            default:
                /*
                 * Since providing checks for each and every digit is tedious
                 * (might add check for 0 to support hexadecimal and binary
                 * or octal forms later) we are keeping it in default case and
                 * checking if the first character is a digit.
                 * 
                 * The isDigit custom utility function is used instead of default
                 * one to avoid non-ASCII digit handling, to KISS.
                 */
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    /*
                     * Handling unexpected characters not used by Lox
                     * 
                     * Important point to note here:
                     * 
                     * -> the erroneous character is still consumed
                     * by the call to consume().
                     * This way we don’t get stuck in an infinite loop.
                     * -> This is also advantageous as there can be
                     * further errors in the code which can be reported
                     * to the programmer in one go.
                     * -> Also none of the code will be executed as hasError
                     * will be set to true
                     */
                    throw new LoxError.ScannerError(new Token(null, String.valueOf(c), null, line),
                            "Unexpected Character");
                }
                break;
        }
    }

    /*
     * Consumes the current character and advances by one step
     */
    private char consume() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current++);
    }

    /*
     * Peek at current character
     */
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    /*
     * Peek at next character
     */
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
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

    /*
     * Match function tries to match the next charcter
     * with the expected character that should be present
     * next to form a paprticular lexeme/(Token?).
     */
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        ++current;
        return true;
    }

    // Helper function to identify digits
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Helper function to identify characters
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }

    // Helper function to identify alphanumeric chars
    private boolean isAlphaNum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /*
     * For scanning potential string in code
     * Notice carefully as we are allowing mutli-line strings by default
     */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                ++line;
            consume();
        }

        if (isAtEnd()) {
            Lox.error(new Token(null, String.valueOf(peek()), null, line), "Unterminated string.");
        } else {
            consume(); // consume the closing quote if any or return EOF
            // quotes are not part of string
            String str = source.substring(start + 1, current - 1);
            addToken(TokenType.STRING, str);
        }

    }

    /*
     * For scanning numbers, we keep scanning till the we hit
     * non-numeric character. If the character is a '.' we check if there's
     * at least one digit after the '.' which means its a floating point
     * number and we start consuming all digits after '.', then we use
     * Java's own floating point parser to parse the substring scanned till now
     * as a double (I have looked up floating point parsing algorithms, trust me,
     * they are complicated, especially, if we follow IEEE-754 standard).
     */
    private void number() {
        while (isDigit(peek()))
            consume();

        if (peek() == '.' && isDigit(peekNext())) {
            /*
             * We are consuming decimal digit only if the next
             * character is a digit after the decimal point.
             */
            consume();
            while (isDigit(peek()))
                consume();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /*
     * Scanning series of text until we hit a whitespace
     * or non alpha-numeric character(maximal munch) then we see
     * if it exists in a keywords map. If it does then add the
     * associated token, else its and identifier
     */
    private void identifier() {
        while (isAlphaNum(peek()))
            consume();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }

    /*
     * source contains the source code in string format
     * tokens contains all the parsed tokens from the source
     * keywords contain all possible keywords in JLox
     */
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final static Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("let", TokenType.LET);
        keywords.put("while", TokenType.WHILE);
    }

    /*
     * start points to the first character of the token
     * current points to the current character of the token
     * line number for generating error messages
     */
    private int start = 0;
    private int current = 0;
    private int line = 1;
}
