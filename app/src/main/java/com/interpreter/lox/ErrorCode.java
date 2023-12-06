package com.interpreter.lox;

enum ExitCode {
    INCORRECT_CMD_USAGE(64),
    INCORRECT_CODE_ERR(65);

    ExitCode(int code) {
        this.exitCode = code;
    }

    final int exitCode;
}