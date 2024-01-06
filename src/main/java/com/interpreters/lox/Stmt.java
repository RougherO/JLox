package com.interpreters.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<T> {
        T visitWhileStmt(While stmt);

        T visitBlockStmt(Block stmt);

        T visitExpressionStmt(Expression stmt);

        T visitIfStmt(If stmt);

        T visitPrintStmt(Print stmt);

        T visitVarStmt(Var stmt);

    }

    static class While extends Stmt {
        While(Token label, Expr condition, Stmt body) {
            this.label = label;
            this.condition = condition;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitWhileStmt(this);
        }

        final Token label;
        final Expr condition;
        final Stmt body;
    }

    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlockStmt(this);
        }

        final List<Stmt> statements;
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }

    static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elsebranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elsebranch = elsebranch;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIfStmt(this);
        }

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elsebranch;
    }

    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final Expr expression;
    }

    static class Var extends Stmt {
        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVarStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    abstract <T> T accept(Visitor<T> visitor);
}
