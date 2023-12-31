package com.interpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Jlox. For documentation refer to https://github.com/RougherO/JLox/docs");
        if (args.length > 1) {
            System.out.println("Usage: Lox [file]");
            System.exit(ExitCode.INCORRECT_CMD_USAGE.exitCode);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hasError)
            System.exit(ExitCode.INCORRECT_CODE_ERR.exitCode);
        if (hasRuntimeError)
            System.exit(ExitCode.INTERNAL_ERR.exitCode);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("=> ");
            String line = reader.readLine();
            if (line == null)
                break; // readLine receives null on encountering EOF(Ctrl + D)
            run(line);
            hasError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        if (hasError)
            return;

        Parser parser = new Parser(tokens);
        List<Stmt> statments = parser.parse();

        if (hasError)
            return;

        Interpreter interpreter = new Interpreter();
        interpreter.interprete(statments);

        if (hasError)
            return;
    }

    public static void error(Token token, String message) {
        hasError = true;
        if (token.type == TokenType.EOF)
            report(token.line, "at end", message);
        else
            report(token.line, "at '" + token.lexeme + "' ", message);
    }

    public static void runtimeError(Token token, String message) {
        hasRuntimeError = true;
        if (token.type == TokenType.EOF)
            report(token.line, "at end", message);
        else
            report(token.line, "at '" + token.lexeme + "' ", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
    }

    private static boolean hasError = false;
    private static boolean hasRuntimeError = false;
}
