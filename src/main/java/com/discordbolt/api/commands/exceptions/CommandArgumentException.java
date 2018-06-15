package com.discordbolt.api.commands.exceptions;

import com.discordbolt.api.commands.ExceptionMessages;

/**
 * Created by Tony on 4/19/2017.
 */
public class CommandArgumentException extends CommandException {

    public CommandArgumentException() {
        super(ExceptionMessages.INCORRECT_USAGE);
    }

    public CommandArgumentException(String message) {
        super(message);
    }
}
