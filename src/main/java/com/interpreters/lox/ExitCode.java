package com.interpreters.lox;

public enum ExitCode {
    INCORRECT_CMD_USAGE(64),
    INCORRECT_CODE_ERR(65),
    INTERNAL_ERR(70);

    ExitCode(int code) {
        this.exitCode = code;
    }

    public final int exitCode;
}