package com.discordbolt.api.commands.exceptions;

public class CommandStateException extends CommandException {

    public CommandStateException() {
        super(ExceptionMessage.BAD_STATE);
    }

    public CommandStateException(String message) {
        super(message);
    }
}
