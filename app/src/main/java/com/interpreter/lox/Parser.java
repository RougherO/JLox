/*
 * expression   -> conditional ;
 * conditional  -> equality ( "?" expression ":" conditional )? ; (*)  // My thoughts : conditional -> ( equality "?" expression ":" )* conditional ;
 *                                                                                      conditional -> ( equality "?" expression ":" )* equality ;
 *                                                                     // Also why we need condtional instead of simple expression there is because
 *                                                                     // we can chain ternary ops like this E1 ? E2 : E3 ? E4 : E5 else we would have
 *                                                                     // to use grouping to recurse back to conditionals(2nd example) 
 *                                                                     // like E1 ? E2 : (E3 ? E4 : E5) which is probably better imo.
 *                                                                     // Might change it in future releases
 * equality     -> comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison   -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term         -> factor ( ( "-" | "+" ) factor )* ;
 * factor       -> unary ( ( "/" | "*" ) unary )*;
 * postfix      -> primary ( "++" | "--" )* 
 * primary      -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" 
 *                 // Error production
 *                 | ( "!=" | "==" ) equality
 *                 | ( ">" | ">=" | "<" | "<=" ) comparison
 *                 | ( "+" ) term
 *                 | ( "/" | "*" ) factor ;
 * (*) -> these have been taken from https://github.com/munificent/craftinginterpreters/blob/master/note/answers/chapter06_parsing.md
 */

package com.interpreter.lox;

import java.util.List;

public class Parser {
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        try {
            Expr expr = expression();
            if (isAtEnd())
                return expr;
            else
                throw panic("Expected operand here.");
        } catch (ParserError error) {
            return null;
        }
    }

    private Expr expression() {
        return conditional();
    }

    private Expr conditional() {
        Expr expr = equality();

        if (match(TokenType.Q_MARK)) {
            Token operator = peekPrev();
            Expr mid = expression();
            if (match(TokenType.COLON)) {
                Expr right = conditional();
                expr = new Expr.Conditional(operator, expr, mid, right);
            } else {
                throw panic("Expected : after if branch");
            }
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = peekPrev();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL)) {
            Token operator = peekPrev();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = peekPrev();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = peekPrev();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(
                TokenType.BANG, TokenType.MINUS,
                TokenType.MINUS_MINUS, TokenType.PLUS_PLUS)) {
            Token operator = peekPrev();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return postfix();
    }

    private Expr postfix() {
        Expr expr = primary();

        while (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
            Token operator = peekPrev();
            expr = new Expr.PostFix(expr, operator);
        }

        return expr;
    }

    private Expr primary() {
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.NIL))
            return new Expr.Literal(null);
        if (match(TokenType.NUMBER, TokenType.STRING))
            return new Expr.Literal(peekPrev().literal);
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            if (!match(TokenType.RIGHT_PAREN)) {
                throw panic("Expected ')' after expression.");
            }
            return new Expr.Grouping(expr);
        }

        // Error handling
        if (match(
                TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL,
                TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL,
                TokenType.PLUS, TokenType.SLASH, TokenType.STAR)) {
            /*
             * Here we do not throw the error as it is not severe to
             * cause our interpreter to go out of sync
             * We just start reparsing from next available token
             * as a new expression.
             */
            throw panic("Expected left hand side of '" + peekPrev().lexeme + "'");
            // return expression();
        }

        throw panic("Expected expression here.");
    }

    private static class ParserError extends RuntimeException {
    }

    private ParserError panic(String message) {
        Lox.error(peek(), message);
        return new ParserError();
    }

    private void synchronize() {
        while (!isAtEnd()) {
            if (peekPrev().type == TokenType.SEMICOLON)
                return;
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
        }
    }

    private Token consume() {
        return tokens.get(current++);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekPrev() {
        return tokens.get(current - 1);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (!isAtEnd() && peek().type == type) {
                consume();
                return true;
            }
        }
        return false;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private final List<Token> tokens;
    private int current = 0;
}
