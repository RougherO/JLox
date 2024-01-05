package com.interpreter.lox;

import java.util.List;

abstract class Expr {
    interface Visitor<T> {
        T visitLogicalExpr(Logical expr);

        T visitAssignExpr(Assign expr);

        T visitConditionalExpr(Conditional expr);

        T visitBinaryExpr(Binary expr);

        T visitGroupingExpr(Grouping expr);

        T visitLiteralExpr(Literal expr);

        T visitUnaryExpr(Unary expr);

        T visitPrePostExpr(PrePost expr);

        T visitVariableExpr(Variable expr);

    }

    static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssignExpr(this);
        }

        final Token name;
        final Expr value;
    }

    static class Conditional extends Expr {
        Conditional(Token operator, Expr left, Expr mid, Expr right) {
            this.operator = operator;
            this.left = left;
            this.mid = mid;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitConditionalExpr(this);
        }

        final Token operator;
        final Expr left;
        final Expr mid;
        final Expr right;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }

    static class PrePost extends Expr {
        PrePost(Token name, Token operator, boolean post) {
            this.name = name;
            this.operator = operator;
            this.post = post;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrePostExpr(this);
        }

        final Token name;
        final Token operator;
        final boolean post;
    }

    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVariableExpr(this);
        }

        final Token name;
    }

    abstract <T> T accept(Visitor<T> visitor);
}
