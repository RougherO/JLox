package com.interpreter.lox;

import java.util.List;

public class Parser {
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private static class ParserError extends RuntimeException {
    }

    public Expr parse() {
        try {
            return expression();
        } catch (ParserError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG, TokenType.BANG_EQUAL)) {
            Token operator = consume();
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
            Token operator = consume();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = consume();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = consume();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = consume();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.NIL))
            return new Expr.Literal(null);
        if (match(TokenType.NUMBER, TokenType.STRING))
            return new Expr.Literal(consume().literal);
        if (match(TokenType.LEFT_PAREN)) {
            consume();
            Expr expr = expression();
            if (match(TokenType.RIGHT_PAREN)) {
                consume();
            } else {
                throw panic("Expected ')' after expression.");
            }
            return new Expr.Grouping(expr);
        }

        throw panic("Expected expression here.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                return true;
            }
        }
        return false;
    }

    private ParserError panic(String message) {
        Lox.error(peek(), message);
        return new ParserError();
    }

    private void synchronize() {
        while (!isAtEnd()) {
            if (consume().type == TokenType.SEMICOLON)
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
        return tokens.get(curernt++);
    }

    private Token peek() {
        return tokens.get(curernt);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private final List<Token> tokens;
    private int curernt = 0;
}
