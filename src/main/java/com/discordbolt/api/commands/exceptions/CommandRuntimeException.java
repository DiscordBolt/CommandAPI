package com.discordbolt.api.commands.exceptions;

public class CommandRuntimeException extends RuntimeException {

    public CommandRuntimeException() {
        super(ExceptionMessage.COMMAND_PROCESS_EXCEPTION);
    }

    public CommandRuntimeException(String message) {
        super(message);
    }
}
