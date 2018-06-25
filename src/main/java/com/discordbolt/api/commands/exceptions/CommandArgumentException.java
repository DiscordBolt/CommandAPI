package com.discordbolt.api.commands.exceptions;

public class CommandArgumentException extends CommandException {

    public CommandArgumentException() {
        super(ExceptionMessage.INCORRECT_USAGE);
    }

    public CommandArgumentException(String message) {
        super(message);
    }
}
