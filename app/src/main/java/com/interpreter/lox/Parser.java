/*
 * program      -> declaration* EOF ;
 * declaration  -> varDecl | statement ;
 * varDecl      -> "let" IDENTIFIER ( "=" expression )? ";" ;
 * statement    -> exprStmt | printStmt | block ;
 * exprStmt     -> expression ";" ;
 * printStmt    -> "print(" expression ");" ;
 * block        -> "{" declaration* "}" ;
 * expression   -> assignment ;
 * assignment   -> IDENTIFIER "=" assignment | conditional ;
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
 * unary        -> ( "-" | "!" | "++" | "--" ) unary | postfix ;
 * postfix      -> primary ( "++" | "--" )* 
 * primary      -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER 
 *                 // Error production
 *                 | ( "!=" | "==" ) equality
 *                 | ( ">" | ">=" | "<" | "<=" ) comparison
 *                 | ( "+" ) term
 *                 | ( "/" | "*" ) factor ;
 * (*) -> these have been taken from https://github.com/munificent/craftinginterpreters/blob/master/note/answers/chapter06_parsing.md
 */

package com.interpreter.lox;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        try {
            while (!isAtEnd()) {
                statements.add(declaration());
            }
        } catch (LoxError.ParserError error) {
            LoxError.panic(error);
            return null;
        }

        return statements;
    }

    private Stmt declaration() {
        if (match(TokenType.LET))
            return varDeclaration();

        return statement();
    }

    private Stmt varDeclaration() {
        if (!match(TokenType.IDENTIFIER))
            throw new LoxError.ParserError(peek(), "Invalid variable name.");
        Token name = peekPrev();

        Expr initializer = null;
        if (match(TokenType.EQUAL))
            initializer = expression();

        if (!match(TokenType.SEMICOLON))
            throw new LoxError.ParserError(peek(), "Expected ';' after variable declaration.");

        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT))
            return printStatement(); // Essentially we are baking print into the language itself

        return expressionStatement();
    }

    private Stmt printStatement() {
        if (!match(TokenType.LEFT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected '(' after print.");
        }
        Expr expr = expression();
        if (!match(TokenType.RIGHT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected ')' after print expression.");
        }
        if (!match(TokenType.SEMICOLON))
            throw new LoxError.ParserError(peek(), "Expected ';' after expression.");

        return new Stmt.Print(expr);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (!match(TokenType.SEMICOLON))
            throw new LoxError.ParserError(peek(), "Expected ';' after expression.");

        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    /*
     * Notice how we evaluate the lhs as if it
     * were an individual expression.
     * 
     * " This conversion works because it turns
     * out that every valid assignment target
     * happens to also be valid syntax as a
     * normal expression. " - Nystrom
     * 
     * Eg.
     * 
     * ```
     * newPoint(x + 2, 0).y = 3;
     * ```
     * " The left-hand side of that assignment
     * could also work as a valid expression. "
     * 
     * ```
     * newPoint(x + 2, 0).y;
     * ```
     * 
     * This means we can parse the left-hand side
     * as if it were an expression and then after
     * the fact produce a syntax tree that turns
     * it into an assignment target. If the left-hand
     * side expression isn’t a valid assignment target,
     * we fail with a syntax error. That ensures we
     * report an error on code like this:
     * ```
     * a + b = c;
     * ```
     */
    private Expr assignment() {
        Expr expr = conditional();

        if (match(TokenType.EQUAL)) {
            Token operator = peekPrev();
            /*
             * As assignment is right associative instead of looping
             * through all assignment expression we simply evaluate rhs
             * through recursion.
             */
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            throw new LoxError.RuntimeError(operator, "Invalid assignment target.");
        }

        return expr;
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
                throw new LoxError.ParserError(peek(), "Expected : after if branch");
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
        if (match(TokenType.IDENTIFIER))
            return new Expr.Variable(peekPrev());
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            if (!match(TokenType.RIGHT_PAREN)) {
                throw new LoxError.ParserError(peek(), "Expected ')' after expression.");
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
            throw new LoxError.ParserError(peek(), "Expected left hand side of '" + peekPrev().lexeme + "'");
            // return expression();
        }

        throw new LoxError.ParserError(peek(), "Expected expression here.");
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
