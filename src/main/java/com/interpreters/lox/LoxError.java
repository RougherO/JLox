package com.interpreters.lox;

public abstract class LoxError extends Error {
    public static class ScannerError extends LoxError {
        ScannerError(Token token, String message) {
            this.token = token;
            this.message = message;
        }
    }

    public static class ParserError extends LoxError {
        ParserError(Token token, String message) {
            this.token = token;
            this.message = message;
        }
    }

    public static class RuntimeError extends LoxError {
        RuntimeError(Token token, String message) {
            this.token = token;
            this.message = message;
        }
    }

    public static void panic(LoxError err) {
        Lox.error(err.token, err.message);
    }

    public Token token;
    public String message;
}
