package com.discordbolt.api.commands.exceptions;

import com.discordbolt.api.commands.ExceptionMessages;

/**
 * Created by Tony on 4/19/2017.
 */
public class CommandStateException extends CommandException {

    public CommandStateException() {
        super(ExceptionMessages.BAD_STATE);
    }

    public CommandStateException(String message) {
        super(message);
    }
}
