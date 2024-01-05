/*
 * program      -> declaration* EOF ;
 * declaration  -> varDecl | statement ;
 * varDecl      -> "let" IDENTIFIER ( "=" expression )? ";" ;
 * label        -> IDENTIFIER ":" ;
 * statement    -> exprStmt | ifStmt | ( label? forStmt | whileStmt ) | block | printStmt | jumpStmt ) ;
 * forStmt      -> "for" "(" ( varDecl | exprStmt ) expression? ";" expression? ")" statement ;
 * whileStmt    -> "while" "(" expression ")" statement ;
 * ifStmt       -> "if" "(" expression ")" statement ( "else" statement )? ;
 * exprStmt     -> expression ";" ;
 * printStmt    -> "print" "(" expression ")" ";" ;
 * block        -> "{" declaration* "}" ;
 * jumpStmt     -> ( "break" | "continue" ) ";" ;
 * expression   -> assignment ;
 * assignment   -> IDENTIFIER "=" assignment | conditional ;
 * conditional  -> logical_or ( "?" expression ":" conditional )? ; (*)  
 * logical_or   -> logical_and ( "or" logical_and )* ;
 * logical_and  -> equality ( "and" equality )* ;
 * equality     -> comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison   -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term         -> factor ( ( "-" | "+" ) factor )* ;
 * factor       -> unary ( ( "/" | "*" ) unary )*;
 * unary        -> ( "-" | "!" ) unary | postfix ;
 * postfix      -> IDENTIFIER ( "++" | "--" )? | primary ;
 * primary      -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | prefix
 *                 // Error production
 *                 | ( "!=" | "==" ) equality
 *                 | ( ">" | ">=" | "<" | "<=" ) comparison
 *                 | "+" term
 *                 | ( "/" | "*" ) factor
 *                 | "=" assignment
 *                 | "let" "=" declaration ;
 * prefix       -> ( "++" | "--" )? IDENTIFIER ;
 * (*) -> these have been taken from https://github.com/munificent/craftinginterpreters/blob/master/note/answers/chapter06_parsing.md
 */

package com.interpreter.lox;

import java.util.ArrayList;
import java.util.Arrays;
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
            throw new LoxError.ParserError(peek(), "Expected an identifier.");
        Token name = peekPrev();

        Expr initializer = null;
        if (match(TokenType.EQUAL))
            initializer = expression();

        if (!match(TokenType.SEMICOLON))
            throw new LoxError.ParserError(peek(), "Expected ';' after variable declaration.");

        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        Token label = null;
        if (match(TokenType.LABEL)) {
            label = peekPrev();
        }
        if (match(TokenType.FOR))
            return forStatement(label);
        if (match(TokenType.WHILE))
            return whileStatement(label);
        if (match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.PRINT))
            return printStatement(); // Essentially we are baking print into the language itself
        if (match(TokenType.LEFT_BRACE))
            return block();
        return expressionStatement();
    }

    /*
     * " The parentheses around the condition are
     * only half useful. You need some kind of delimiter
     * between the condition and the then statement,
     * otherwise the parser can’t tell when it has reached
     * the end of the condition expression. But the opening
     * parenthesis after if doesn’t do anything useful.
     * Dennis Ritchie put it there so he could use ')' as
     * the ending delimiter without having unbalanced parentheses.
     * 
     * Other languages like Lua and some BASICs use a keyword
     * like then as the ending delimiter and don’t have anything
     * before the condition. Go and Swift instead require the
     * statement to be a braced block. That lets them use the {
     * at the beginning of the statement to tell when the
     * condition is done. " - Nystrom
     */
    private Stmt ifStatement() {
        if (!match(TokenType.LEFT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected '(' after if.");
        }
        Expr condition = expression();

        if (!match(TokenType.RIGHT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected ')' after if condition.");
        }
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt forStatement(Token label) {
        if (!match(TokenType.LEFT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected '(' after while.");
        }

        Stmt initializer = null;
        if (!match(TokenType.SEMICOLON)) {
            if (match(TokenType.LET))
                initializer = varDeclaration();
            else
                initializer = expressionStatement();
        }

        Expr condition = null;
        if (peek().type != TokenType.SEMICOLON) {
            condition = expression();
        }
        if (!match(TokenType.SEMICOLON)) {
            throw new LoxError.ParserError(peek(), "Expected ';' after loop condition.");
        }

        Expr increment = null;
        if (peek().type != (TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        if (!match(TokenType.RIGHT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected ')' after for clauses.");
        }

        Stmt body = statement();

        // For loop Desugaring -> Converting for loop to a simple while loop
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)));
        }

        if (condition == null)
            condition = new Expr.Literal(true);
        body = new Stmt.While(label, condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(
                    initializer,
                    body));
        }

        return body;
    }

    private Stmt whileStatement(Token label) {
        if (!match(TokenType.LEFT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected '(' after while.");
        }
        Expr condition = expression();
        if (!match(TokenType.RIGHT_PAREN)) {
            throw new LoxError.ParserError(peek(), "Expected ')' after while condtition.");
        }
        Stmt body = statement();

        return new Stmt.While(label, condition, body);
    }

    private Stmt block() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd() && !match(TokenType.RIGHT_BRACE)) {
            statements.add(declaration());
        }

        if (peekPrev().type != TokenType.RIGHT_BRACE) {
            throw new LoxError.ParserError(peekPrev(), "Expected '}' after block.");
        }

        return new Stmt.Block(statements);
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
        Expr expr = logical_or();

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

    private Expr logical_or() {
        Expr expr = logical_and();

        while (match(TokenType.OR)) {
            Token operator = peekPrev();
            Expr right = logical_and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr logical_and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = peekPrev();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
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
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = peekPrev();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return postfix();
    }

    private Expr postfix() {
        if (match(TokenType.IDENTIFIER)) {
            Token name = peekPrev();
            if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
                Token operator = peekPrev();
                return new Expr.PrePost(name, operator, true);
            }

            return new Expr.Variable(name);
        }

        Expr expr = primary();
        if (peek().type == TokenType.PLUS_PLUS || peek().type == TokenType.MINUS_MINUS) {
            throw new LoxError.ParserError(peekPrev(), "Invalid postfix target.");
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
        if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS))
            return prefix();
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
                TokenType.PLUS, TokenType.SLASH, TokenType.STAR, TokenType.EQUAL)) {

            throw new LoxError.ParserError(peekPrev(), "Expected left hand side of '" + peekPrev().lexeme + "'");
        }

        throw new LoxError.ParserError(peek(), "Expected expression here.");
    }

    private Expr prefix() {
        Token operator = peekPrev();
        if (!match(TokenType.IDENTIFIER))
            throw new LoxError.ParserError(peek(), "Invalid prefix target");

        Token name = peekPrev();

        return new Expr.PrePost(name, operator, false);
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
