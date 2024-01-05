package com.interpreter.lox;

import java.util.List;

/* 
 * Notice we use Expr.Visitor<Void> as Statements have 
 * no return values -> statments don't produce any values.
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    void interprete(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (LoxError.RuntimeError error) {
            Lox.runtimeError(error.token, error.message);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);

        /*
         * For postfix expression we store the variables
         * in a stack in the current environment for the
         * current statement. When the current statement
         * execution is over we pop all the postfix variables
         * and increment/decrement their values.
         * 
         * All postfix expressions are modified as soon as
         * current statement execution is over, i.e., we
         * find a ';'.
         */
        clearStack();
    }

    private void clearStack() {
        while (!this.environment.memstack.empty()) {
            Expr expr = this.environment.memstack.pop();
            if (expr instanceof Expr.PrePost) {
                Token name = ((Expr.PrePost) expr).name;
                Token operator = ((Expr.PrePost) expr).operator;
                Object value = this.environment.fetch(name);
                if (!(value instanceof Double)) {
                    throw new LoxError.RuntimeError(
                            name,
                            "Invalid value of '" + name.lexeme
                                    + "' for post fix expression."
                                    + " Expected Number type.");
                }
                if (operator.type == TokenType.PLUS_PLUS) {
                    this.environment.assign(name, (Double) value + 1);
                } else if (operator.type == TokenType.MINUS_MINUS) {
                    this.environment.assign(name, (Double) value - 1);
                }
            }
        }
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment outer = this.environment;

        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (LoxError.RuntimeError error) {
            throw error;
        } finally {
            this.environment = outer;
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        if (stmt.label != null) {
            environment.define(stmt.label, stmt);
        }

        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elsebranch != null) {
            execute(stmt.elsebranch);
        }

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name, value);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    // P.S.- Here logical expression return the expression values
    // actually used instead of true or false
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            // Notice how we simply return the first
            // true value found
            if (isTruthy(left))
                return left;
        } else {
            // Notice how we simply return the first
            // false value found
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.fetch(expr.name);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        Object value = evaluate(expr.expression);
        return value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        /*
         * Note that here every binary expression is
         * evaluated in *left to right* order so any side
         * effects related to associativity is totally on user
         * 
         * Also note that left and right operands have been
         * evaluated first before checking the type of either
         */
        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                /*
                 * If either of the operands is String
                 * we convert both to string and concatenate them
                 */
                if (left instanceof String || right instanceof String)
                    return stringify(left) + stringify(right);
                throw new LoxError.RuntimeError(
                        expr.operator,
                        "Either operands must be string or both numbers.");
            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;

            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double) left * (double) right;

            case SLASH:
                checkNumberOperand(expr.operator, left, right);
                return (double) left / (double) right;

            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;

            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left >= (double) right;

            case LESS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left < (double) right;

            case LESS_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left <= (double) right;

            case BANG_EQUAL:
                return !isEqual(left, right);

            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        Object left = evaluate(expr.left);
        Object mid = evaluate(expr.mid);
        Object right = evaluate(expr.right);

        if (isTruthy(left)) {
            return mid;
        }
        return right;
    }

    @Override
    public Object visitPrePostExpr(Expr.PrePost expr) {
        if (!expr.post) {
            Object value = this.environment.fetch(expr.name);
            if (!(value instanceof Double)) {
                throw new LoxError.RuntimeError(
                        expr.name,
                        "Invalid value of '" + expr.name.lexeme
                                + "' for pre fix expression."
                                + " Expected Number type");
            }

            Object newValue = null;
            if (expr.operator.type == TokenType.PLUS_PLUS) {
                newValue = (Double) value + 1;
            }
            if (expr.operator.type == TokenType.MINUS_MINUS) {
                newValue = (Double) value - 1;
            }
            this.environment.assign(expr.name, newValue);

            return newValue;
        }
        this.environment.memstack.add(expr);
        return this.environment.fetch(expr.name);
    }

    // To convert all Java objects to appropriate Lox strings
    private String stringify(Object obj) {
        if (obj == null)
            return "nil";

        if (obj instanceof Double) {
            String text = obj.toString();
            if (text.endsWith(".0"))
                return text.substring(0, text.length() - 2);
        }

        return obj.toString();
    }

    /*
     * This function is used to define values in Lox
     * which are considered true and which are false
     * for example "nil" is false value and any number
     * expect 0 is true and "false" is also falsy value
     * rest all are true
     */
    private boolean isTruthy(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Boolean)
            return (boolean) obj;
        if (obj instanceof Double)
            return (Double) obj != 0.0;
        return true;
    }

    // Custom comparison for Lox based on their truthy values
    private boolean isEqual(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null)
            return true;
        if (obj1 == null)
            return false;

        return obj1.equals(obj2);
    }

    // Checks whether all operands are number types (double)
    private void checkNumberOperand(Token operator, Object right) {
        if (right instanceof Double)
            return;
        throw new LoxError.RuntimeError(
                operator,
                "Expected operands to be numbers");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new LoxError.RuntimeError(
                operator,
                "Expected operands to be numbers");
    }

    /*
     * This is our global namespace environment containing all
     * global variables purposefully declared as an interpreter
     * field so that the global environment stays in memory as
     * long as the interpreter stays alive.
     */
    private Environment environment = new Environment();
}
