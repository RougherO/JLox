package com.interpreter.lox;

import java.util.Map;
import java.util.HashMap;

public class Environment {

    Environment() {
        outer = null;
    }

    Environment(Environment enclosing) {
        this.outer = enclosing;
    }

    void define(Token var, Object value) {
        if (values.get(var.lexeme) != null) {
            throw new LoxError.RuntimeError(var, "Variable redefinition '" + var.lexeme + "'' in current scope.");
        }
        values.put(var.lexeme, value);
    }

    Object fetch(Token var) {
        // We check if variable is present in current scope
        if (values.containsKey(var.lexeme)) {
            Object value = values.get(var.lexeme);
            // If variable is unassigned and used we report it as an error
            if (value == null) {
                throw new LoxError.RuntimeError(var, "Unassigned variable '" + var.lexeme + "' is used.");
            }
            return value;
        }
        // If current scope didn't have the variable we search in
        // outer scope. If it fails we report it as an error
        if (outer == null)
            throw new LoxError.RuntimeError(var, "Undefined variable '" + var.lexeme + "'.");

        return outer.fetch(var);
    }

    void assign(Token var, Object value) {
        if (values.containsKey(var.lexeme)) {
            values.put(var.lexeme, value);
        } else if (outer != null) {
            outer.assign(var, value);
        } else {
            throw new LoxError.RuntimeError(var, "Undefined variable '" + var.lexeme + "'.");
        }
    }

    private final Map<String, Object> values = new HashMap<>();
    final Environment outer;
}
